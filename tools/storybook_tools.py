#!/usr/bin/env python3
"""Build-time tooling for the Remote Compose storybook.

Subcommands:
  sources   Extract every story function's source, rewritten to AndroidX's packages, into
            sources.json for the Code tab; fail when a story imports anything from
            androidx.compose.remote that AndroidX's api/current.txt does not declare public.
  api       Extract the public signatures of each component's symbols from AndroidX's
            api/current.txt files into api.json for the Docs tab.
  coverage  List every public building block in AndroidX's current.txt and which stories use it;
            write coverage.json and fail when a building block is used by no story.

The stories are written against rc-players' multiplatform fork of the creation API, which is
AndroidX's source in another package. The only rewrite is the package: anything else from
rc-players in a story file is an error, so the code users copy is AndroidX's public API.
"""

import argparse
import json
import os
import re
import sys

PACKAGE_REWRITES = [
    ("ee.schimke.composeai.rccreation.compose.", "androidx.compose.remote.creation.compose."),
    ("ee.schimke.composeai.rccreation.foundation.", "androidx.compose.remote.foundation."),
]
ALLOWED_IMPORT_PREFIXES = (
    "ee.schimke.composeai.rccreation.compose.",
    "ee.schimke.composeai.rccreation.foundation.",
    "androidx.compose.runtime.",
    "androidx.compose.ui.graphics.",
    "androidx.compose.ui.geometry.",
    "androidx.compose.ui.unit.",
    "kotlin.",
)
API_FILES = ["remote-creation-compose.txt", "remote-creation-expression.txt", "foundation.txt"]


def read_text(path):
    with open(path, encoding="utf-8") as fh:
        return fh.read()


def read_json(path):
    with open(path, encoding="utf-8") as fh:
        return json.load(fh)


def write_json(value, path, indent=1, sort_keys=False):
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(value, fh, indent=indent, sort_keys=sort_keys)


def rewrite(text):
    for src, dst in PACKAGE_REWRITES:
        text = text.replace(src, dst)
    return text


# ---------------------------------------------------------------------------------------------
# Story sources


class Decl:
    def __init__(self, name, lines, composable):
        self.name = name
        self.lines = lines
        self.composable = composable

    @property
    def text(self):
        return "\n".join(self.lines)


# A top-level declaration's first line: modifiers, the keyword, type parameters, an extension
# receiver (`fun RemoteModifier.x`, whose name is `x`), then the name.
DECL_RE = re.compile(
    r"^(?:@\w+(?:\([^)]*\))?\s+)*"
    r"(?:(?:private|internal|public|inline|suspend|sealed|abstract|open|data|enum|const)\s+)*"
    r"(?:class|interface|object|fun|val|var)\s+"
    r"(?:<[^>]+>\s+)?"
    r"(?:[\w.<>?, ]+\.)?"
    r"([A-Za-z_]\w*)"
)


def parse_kotlin(path):
    lines = read_text(path).split("\n")
    imports = []
    decls = []
    i = 0
    while i < len(lines):
        line = lines[i]
        if line.startswith("import "):
            imports.append(line[len("import ") :].strip())
            i += 1
            continue
        if line.startswith(("@", "/**", "//")) or DECL_RE.match(line):
            start = i
            # Leading comments / annotations.
            while i < len(lines) and not DECL_RE.match(lines[i]):
                if lines[i].strip() == "" and not lines[i].startswith(" "):
                    break
                i += 1
            m = DECL_RE.match(lines[i]) if i < len(lines) else None
            if m is None:
                i = start + 1
                continue
            name = m.group(1)
            # The sources are ktfmt-formatted, so a top-level declaration runs until the next line
            # that starts in column 0 with anything but a closing bracket: the next declaration.
            # (Counting braces ended an expression-bodied `fun f() = a { … }.b()` at the first `}`.)
            j = i + 1
            while j < len(lines):
                nxt = lines[j]
                if nxt and not nxt[0].isspace() and not nxt.startswith(("}", ")", "]")):
                    break
                j += 1
            end = j - 1
            while end > i and lines[end].strip() == "":
                end -= 1
            block = lines[start : end + 1]
            composable = any("@RemoteComposable" in line for line in lines[start : i + 1])
            decls.append(Decl(name, block, composable))
            i = end + 1
            continue
        i += 1
    return imports, decls


def block_used(name, text):
    """Whether `text` uses a building block: a lowercase name (a function) only counts when
    called, `name(` or `name {`, so a parameter or variable called `image` is not RemoteBrush.image;
    a capitalised name (a class, a composable, a constant) counts wherever it appears."""
    if name[:1].islower():
        return re.search(r"(?<![\w])" + re.escape(name) + r"\s*[({]", text) is not None
    return word_used(name, text)


def word_used(name, text):
    return (
        re.search(r"(?<![\w.])" + re.escape(name) + r"(?!\w)", text) is not None
        or re.search(r"\." + re.escape(name) + r"(?!\w)", text) is not None
    )


def import_used(name, text):
    """Whether `text` uses the imported `name`; a named argument (`f(name = …)`) is not a use."""
    for m in re.finditer(r"(?<![\w])" + re.escape(name) + r"(?!\w)", text):
        after = text[m.end() :]
        if re.match(r"\s*=(?!=)", after) and innermost_opener(text, m.start()) == "(":
            continue
        return True
    return False


def innermost_opener(text, index):
    """The bracket that encloses `index`, ignoring string literals: "(", "{", "[" or None."""
    stripped = re.sub(r'"(?:\\.|[^"\\])*"', lambda m: '"' + " " * (len(m.group(0)) - 2) + '"', text[:index])
    stack = []
    for c in stripped:
        if c in "({[":
            stack.append(c)
        elif c in ")}]" and stack:
            stack.pop()
    return stack[-1] if stack else None


def story_files(stories_dir):
    return sorted(os.path.join(stories_dir, f) for f in os.listdir(stories_dir) if f.endswith(".kt"))


def cmd_sources(args):
    out = {}
    errors = []
    public = public_names(parse_api(args.api)) if args.api else None
    all_decls = {}
    parsed = {}
    for path in story_files(args.stories):
        imports, decls = parse_kotlin(path)
        parsed[path] = (imports, decls)
        for imp in imports:
            if not imp.startswith(ALLOWED_IMPORT_PREFIXES):
                errors.append(f"{os.path.basename(path)}: import {imp} is not AndroidX's public API")
            elif (
                public is not None
                and rewrite(imp).startswith("androidx.compose.remote.")
                and rewrite(imp) not in public
            ):
                errors.append(
                    f"{os.path.basename(path)}: {rewrite(imp)} is not in AndroidX's api/current.txt"
                )
        for d in decls:
            if d.name in all_decls:
                errors.append(f"duplicate top-level declaration {d.name}")
            all_decls[d.name] = (path, d)
    for path, (imports, decls) in parsed.items():
        for d in decls:
            if not d.composable:
                continue
            # Helper declarations (enums, constants) a story reads are shown with it.
            helpers = []
            seen = {d.name}
            queue = [d]
            while queue:
                cur = queue.pop()
                for name, (_path, h) in all_decls.items():
                    # Every declaration the story reads comes with it: enums, constants, plain helpers,
                    # and other @RemoteComposable functions it calls, so the snippet compiles alone.
                    if name in seen:
                        continue
                    if word_used(name, cur.text):
                        seen.add(name)
                        helpers.append(h)
                        queue.append(h)
            body = "\n\n".join([h.text for h in helpers] + [d.text])
            # Imports from every file a declaration came from: a helper in another file brings its own.
            pool = set(imports)
            for h in helpers:
                pool.update(parsed[all_decls[h.name][0]][0])
            used_imports = sorted({rewrite(imp) for imp in pool if import_used(imp.rsplit(".", 1)[1], body)})
            code = "\n".join("import " + i for i in used_imports) + "\n\n" + rewrite(body)
            out[d.name] = {
                "file": os.path.basename(path),
                "code": code.strip() + "\n",
                "imports": used_imports,
            }
    if args.verify_out:
        os.makedirs(args.verify_out, exist_ok=True)
        for f in os.listdir(args.verify_out):
            if f.endswith(".kt"):
                os.remove(os.path.join(args.verify_out, f))
        for path in parsed:
            text = rewrite(read_text(path)).replace(
                "package rcstorybook.stories", "package rcstorybook.verify"
            )
            with open(os.path.join(args.verify_out, os.path.basename(path)), "w", encoding="utf-8") as fh:
                fh.write(text)
    if args.snippets_out:
        os.makedirs(args.snippets_out, exist_ok=True)
        for f in os.listdir(args.snippets_out):
            if f.endswith(".kt"):
                os.remove(os.path.join(args.snippets_out, f))
        # Each snippet in a package of its own: exactly what a user copies, compiled with nothing else.
        for fn, entry in out.items():
            with open(os.path.join(args.snippets_out, fn + ".kt"), "w", encoding="utf-8") as fh:
                fh.write(f"package rcstorybook.snippets.{fn.lower()}\n\n" + entry["code"])
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1
    os.makedirs(os.path.dirname(args.out), exist_ok=True)
    with open(args.out, "w", encoding="utf-8") as fh:
        json.dump(out, fh, indent=1, sort_keys=True)
    print(f"sources: {len(out)} story functions -> {args.out}")
    return 0


# ---------------------------------------------------------------------------------------------
# AndroidX api/current.txt


def public_names(entries):
    """`package.Name` for every public class and top-level function or property."""
    names = set()
    for e in entries:
        if e["kind"] == "class":
            names.add(e["package"] + "." + e["name"].split(".")[0])
        elif (
            e["owner"]
            and (e["owner"].endswith("Kt") or e.get("static"))
            and e["kind"] in ("method", "property")
            or e["owner"]
            and e["owner"].endswith("Kt")
            and e["kind"] == "field"
        ):
            names.add(e["package"] + "." + e["name"])
    return names


ANNOTATION_RE = re.compile(r"@[\w.]+(?:\([^)]*\))?\s*")


def clean_signature(s):
    s = ANNOTATION_RE.sub("", s)
    s = s.replace("public ", "").replace("static ", "").replace("final ", "").replace("abstract ", "")
    s = re.sub(r"\bandroidx\.compose\.remote\.creation\.compose\.(\w+\.)", "", s)
    s = re.sub(r"\bandroidx\.compose\.remote\.foundation\.(\w+\.)", "", s)
    s = re.sub(r"\bandroidx\.compose\.ui\.(\w+\.)+", "", s)
    s = re.sub(r"\bandroidx\.compose\.runtime\.", "", s)
    s = s.replace("kotlin.jvm.functions.Function0<kotlin.Unit>", "() -> Unit")
    s = re.sub(
        r"kotlin\.jvm\.functions\.Function1<\??\s*(?:super\s+)?([\w.<>?]+),kotlin\.Unit>", r"\1.() -> Unit", s
    )
    s = s.replace("java.lang.String", "String").replace("java.util.List", "List").replace("kotlin.", "")
    s = re.sub(r"\s+", " ", s).strip().rstrip(";")
    return s


def parse_api(api_dir):
    """Returns entries: dicts with package, owner (class), kind, name, signature, kotlin_only."""
    entries = []
    for f in API_FILES:
        pkg = None
        owner = None
        for raw in read_text(os.path.join(api_dir, f)).splitlines():
            s = raw.strip()
            m = re.match(r"package (\S+) \{", s)
            if m:
                pkg = m.group(1)
                continue
            if not s.startswith(("method", "ctor", "property", "field", "enum_constant")):
                m = re.search(
                    r"\b(?:class|interface|enum|@interface)\s+([\w.<>,? ]+?)\s*(?:extends|implements|\{)", s
                )
                if m:
                    owner = m.group(1).split("<")[0].strip()
                    entries.append(
                        {
                            "package": pkg,
                            "owner": None,
                            "kind": "class",
                            "name": owner,
                            "signature": clean_signature(s.rstrip("{").strip()),
                            "file": f,
                        }
                    )
                continue
            if "@BytecodeOnly" in s or "@InaccessibleFromKotlin" in s:
                continue
            kind = s.split()[0]
            body = s[len(kind) :]
            if kind in ("method", "ctor"):
                m = re.search(r"([\w-]+)\s*\(", body)
            else:
                m = re.search(r"[\s.]([\w]+)\s*;", body)
            if not m:
                continue
            entries.append(
                {
                    "package": pkg,
                    "owner": owner,
                    "kind": kind,
                    "name": m.group(1),
                    "signature": clean_signature(body),
                    "file": f,
                    "static": " static " in s,
                }
            )
    return entries


def cmd_api(args):
    """Every class and top-level function name, keyed `package.Name`, with its signatures."""
    entries = parse_api(args.api)
    out: dict[str, list[str]] = {}
    for e in entries:
        pkg = e["package"]
        if e["kind"] == "class":
            out.setdefault(pkg + "." + e["name"], []).append(e["signature"])
    for e in entries:
        if e["kind"] == "class" or not e["owner"]:
            continue
        pkg = e["package"]
        owner = e["owner"]
        base = owner.split(".")[0]
        if owner.endswith("Kt") and e["kind"] == "method":
            out.setdefault(pkg + "." + e["name"], []).append(e["signature"])
        key = pkg + "." + base
        if key in out and not owner.endswith("Kt"):
            out[key].append("  " + e["signature"])
    write_json(out, args.out, indent=0, sort_keys=True)
    print(f"api: {len(out)} symbols -> {args.out}")
    return 0


# Public API that is not a building block a storybook can show: capture plumbing, Android-only
# resources and intents, and members reached through their owner.
NOT_BLOCKS = {
    "captureRemoteDocument",
    "captureSingleRemoteDocument",
    "Profile",
    "createCreationDisplayInfo",
    "rememberRemoteCreationDisplayInfo",
    "RemoteCreationDisplayInfo",
    "currentProfileSupports",
    "currentProfileSupportsPathClip",
    "currentProfileSupportsThemedColors",
    "currentProfileSupportsTouchModifiers",
    "rememberPendingIntentAction",
    "vectorResource",
    "createThemedRemoteColor",
    "toRemoteString",
    "fromTextStyle",
}


# Public API no story can demonstrate yet, each with the reason (checked in AndroidX's and
# rc-players' sources). The coverage page lists them as pending, not missing. Re-check on every bump.
CANNOT_DEMONSTRATE = {
    "drawTextOnCircle": "AndroidX's own capture profiles do not list its operation (57), so a "
    "capture refuses it (remote-core Operations.java)",
    "rememberNamedRemoteLong": "AndroidX cannot yet turn a dynamic RemoteLong into anything a story "
    "can show (RemoteLong.kt)",
    "MutableRemoteLong": "AndroidX's valueChangeAction handles Int, Float and String state only "
    "(ValueChangeAction.kt), and a dynamic long cannot be shown",
}


def building_blocks(entries):
    """The public names a storybook user builds with, grouped as the Coverage page lists them."""
    blocks: dict[tuple[str, str], set[str]] = {}

    def add(group, name, pkg):
        if name in NOT_BLOCKS:
            return
        blocks.setdefault((group, name), set()).add(pkg)

    for e in entries:
        pkg = e["package"]
        short = pkg.replace("androidx.compose.remote.creation.compose", "creation.compose").replace(
            "androidx.compose.remote.foundation", "foundation"
        )
        sig = e["signature"]
        if (
            e["kind"] == "method"
            and e["owner"]
            and e["owner"].endswith("Kt")
            and e["name"][0].isupper()
            and "void " in sig
            and pkg.endswith((".layout", ".icon", ".image", ".text"))
        ):
            add("Composables", e["name"], short)
        elif (
            e["kind"] == "method"
            and e["owner"]
            and e["owner"].endswith("Kt")
            and pkg.endswith(".modifier")
            and sig.startswith("RemoteModifier ")
            or e["kind"] == "method"
            and e["owner"]
            in (
                "RemoteRowScope",
                "RemoteColumnScope",
                "RemoteCollapsibleRowScope",
                "RemoteCollapsibleColumnScope",
            )
        ):
            add("Modifiers", e["name"], short)
        elif e["kind"] == "method" and pkg.endswith(".action") and e["owner"].endswith("Kt"):
            add("Actions", e["name"], short)
        elif e["kind"] == "method" and pkg.endswith(".shaders") and e["owner"].endswith("Kt"):
            add("Brushes", e["name"], short)
        elif e["kind"] == "method" and pkg.endswith(".painter") and e["owner"].endswith("Kt"):
            add("Painters", e["name"], short)
        elif e["kind"] in ("method", "property") and pkg.endswith(".shapes") and e["owner"].endswith("Kt"):
            add("Shapes", e["name"], short)
        elif (
            e["kind"] == "method"
            and pkg.endswith(".state")
            and e["owner"]
            and e["owner"].endswith("Kt")
            and re.match(r"(remember|animate|remote(Tween|Spring)|Mutable|lerp|RemoteColor$)", e["name"])
        ):
            add("State & animation", e["name"], short)
        elif e["kind"] == "method" and e["owner"] == "RemoteDrawScope":
            add("Canvas drawing", e["name"], short)
        elif e["kind"] == "method" and e["owner"] == "RemoteTextMeasurer":
            add("Text measuring", e["name"], short)
        elif e["kind"] == "method" and e["owner"] == "RemoteComponentComposeKt":
            add("Composables", e["name"], short)
        elif e["kind"] == "method" and e["owner"] == "RemoteTextMeasureKt":
            add("Text measuring", e["name"], short)
        elif e["kind"] == "property" and e["owner"] in ("RemoteTimeVariables", "RemoteConfiguration"):
            add("Environment", e["owner"] + "." + e["name"], short)
        elif e["kind"] == "property" and e["owner"] in (
            "RemoteEnterTransition.Companion",
            "RemoteExitTransition.Companion",
        ):
            add("Transitions", e["owner"].split(".")[0] + "." + e["name"], short)
        elif e["kind"] == "property" and e["owner"] == "RemoteEasing.Companion":
            add("Easing", "RemoteEasing." + e["name"], short)
        elif e["kind"] == "property" and e["owner"] == "RemoteContentScale.Companion":
            add("Content scale", "RemoteContentScale." + e["name"], short)
        elif e["kind"] == "method" and e["owner"] == "RemoteFloatOperations":
            add("Float operations", e["name"], short)
        elif (
            e["kind"] == "class"
            and pkg.endswith(".text")
            and e["name"].startswith("Remote")
            and "." not in e["name"]
            and not e["name"].endswith("Kt")
        ):
            add("Text", e["name"], short)
    return blocks


def cmd_coverage(args):
    entries = parse_api(args.api)
    blocks = building_blocks(entries)
    sources = read_json(args.sources)
    # Option codes the catalog offers in its controls (`option("Label", "Code", value)`): a value a
    # user can pick and see printed in the Code tab counts as covered too.
    option_codes = []
    if args.catalog:
        for f in sorted(os.listdir(args.catalog)):
            if f.endswith(".kt"):
                text = read_text(os.path.join(args.catalog, f))
                option_codes += re.findall(r'option\(\s*"(?:\\.|[^"\\])*"\s*,\s*"((?:\\.|[^"\\])*)"', text)
    options_text = "\n".join(c.replace('\\"', '"') for c in option_codes)
    uses = []
    missing = []
    for (group, name), pkgs in sorted(blocks.items()):
        member = name.split(".")[-1]
        qualified = name if "." in name else None
        used_by = []
        for fn, src in sorted(sources.items()):
            code = src["code"]
            if qualified:
                owner = qualified.split(".")[0]
                hit = qualified in code or (
                    word_used(owner, code)
                    and re.search(r"\." + re.escape(member) + r"(?!\w)", code) is not None
                )
            else:
                hit = block_used(member, code)
            if hit:
                used_by.append(fn)
        in_options = bool(
            (qualified and qualified in options_text) or (not qualified and block_used(member, options_text))
        )
        pending = CANNOT_DEMONSTRATE.get(name)
        uses.append(
            {
                "group": group,
                "name": name,
                "packages": sorted(pkgs),
                "stories": used_by,
                "inOptions": in_options,
                "pending": pending,
            }
        )
        if not used_by and not in_options and not pending:
            missing.append(f"{group}: {name}")
    commit = read_text(os.path.join(args.api, "COMMIT")).strip()
    write_json({"commit": commit, "blocks": uses}, args.out)
    pending_count = sum(1 for u in uses if u["pending"] and not u["stories"])
    covered = len(uses) - len(missing) - pending_count
    print(
        f"coverage: {covered}/{len(uses)} public building blocks used by a story, "
        f"{pending_count} that cannot be demonstrated yet (reasons in coverage.json) -> {args.out}"
    )
    if missing:
        print("not covered by any story:\n  " + "\n  ".join(missing), file=sys.stderr)
        return 0 if args.report_only else 1
    return 0


def main():
    p = argparse.ArgumentParser()
    sub = p.add_subparsers(dest="cmd", required=True)
    s = sub.add_parser("sources")
    s.add_argument("--stories", required=True)
    s.add_argument("--out", required=True)
    s.add_argument("--api")
    s.add_argument("--verify-out", help="also write each story file, rewritten, into this directory")
    s.add_argument(
        "--snippets-out",
        help="also write each story's Code tab snippet, alone in its own package, into this directory",
    )
    a = sub.add_parser("api")
    a.add_argument("--api", required=True)
    a.add_argument("--out", required=True)
    c = sub.add_parser("coverage")
    c.add_argument("--api", required=True)
    c.add_argument("--sources", required=True)
    c.add_argument("--out", required=True)
    c.add_argument("--catalog")
    c.add_argument("--report-only", action="store_true")
    args = p.parse_args()
    return {"sources": cmd_sources, "api": cmd_api, "coverage": cmd_coverage}[args.cmd](args)


if __name__ == "__main__":
    sys.exit(main())
