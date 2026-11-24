#!/usr/bin/env python3
"""Build-time tooling for the Remote Compose storybook.

Subcommands:
  sources   Extract every story function's source, rewritten to AndroidX's packages, into
            sources.json for the Code tab; fail when a story imports anything from
            androidx.compose.remote that AndroidX's api/current.txt does not declare public.
  api       Extract the public signatures of each component's symbols from AndroidX's
            api/current.txt files into api.json for the Docs tab.
  coverage  List every public building block in AndroidX's current.txt, the stories and control
            options that use it on its receiver, and its status (covered, pending, missing);
            write coverage.json, which the site only displays, and fail when a building block is
            missing or a CANNOT_DEMONSTRATE entry is stale.
  uses      Print the stories that use given API members (`Owner.member`), by the same rule.

The stories are written against rc-players' multiplatform fork of the creation API, which is
AndroidX's source in another package. The only rewrite is the package: anything else from
rc-players in a story file is an error, so the code users copy is AndroidX's public API.
"""

import argparse
import json
import os
import re
import sys
from typing import Any

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
    blocks: dict[tuple[str, str], list[dict[str, Any]]] = {}

    def add(group, name, entry):
        if name in NOT_BLOCKS:
            return
        blocks.setdefault((group, name), []).append(entry)

    for e in entries:
        pkg = e["package"]
        sig = e["signature"]
        if (
            e["kind"] == "method"
            and e["owner"]
            and e["owner"].endswith("Kt")
            and e["name"][0].isupper()
            and "void " in sig
            and pkg.endswith((".layout", ".icon", ".image", ".text"))
        ):
            add("Composables", e["name"], e)
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
            add("Modifiers", e["name"], e)
        elif e["kind"] == "method" and pkg.endswith(".action") and e["owner"].endswith("Kt"):
            add("Actions", e["name"], e)
        elif e["kind"] == "method" and pkg.endswith(".shaders") and e["owner"].endswith("Kt"):
            add("Brushes", e["name"], e)
        elif e["kind"] == "method" and pkg.endswith(".painter") and e["owner"].endswith("Kt"):
            add("Painters", e["name"], e)
        elif e["kind"] in ("method", "property") and pkg.endswith(".shapes") and e["owner"].endswith("Kt"):
            add("Shapes", e["name"], e)
        elif (
            e["kind"] == "method"
            and pkg.endswith(".state")
            and e["owner"]
            and e["owner"].endswith("Kt")
            and re.match(r"(remember|animate|remote(Tween|Spring)|Mutable|lerp|RemoteColor$)", e["name"])
        ):
            add("State & animation", e["name"], e)
        elif e["kind"] == "method" and e["owner"] == "RemoteDrawScope":
            add("Canvas drawing", e["name"], e)
        elif e["kind"] == "method" and e["owner"] == "RemoteTextMeasurer":
            add("Text measuring", e["name"], e)
        elif e["kind"] == "method" and e["owner"] == "RemoteComponentComposeKt":
            add("Composables", e["name"], e)
        elif e["kind"] == "method" and e["owner"] == "RemoteTextMeasureKt":
            add("Text measuring", e["name"], e)
        elif e["kind"] == "property" and e["owner"] in ("RemoteTimeVariables", "RemoteConfiguration"):
            add("Environment", e["owner"] + "." + e["name"], e)
        elif e["kind"] == "property" and e["owner"] in (
            "RemoteEnterTransition.Companion",
            "RemoteExitTransition.Companion",
        ):
            add("Transitions", e["owner"].split(".")[0] + "." + e["name"], e)
        elif e["kind"] == "property" and e["owner"] == "RemoteEasing.Companion":
            add("Easing", "RemoteEasing." + e["name"], e)
        elif e["kind"] == "property" and e["owner"] == "RemoteContentScale.Companion":
            add("Content scale", "RemoteContentScale." + e["name"], e)
        elif e["kind"] == "method" and e["owner"] == "RemoteFloatOperations":
            add("Float operations", e["name"], e)
        elif (
            e["kind"] == "class"
            and pkg.endswith(".text")
            and e["name"].startswith("Remote")
            and "." not in e["name"]
            and not e["name"].endswith("Kt")
        ):
            add("Text", e["name"], e)
    return blocks


def short_package(pkg):
    return pkg.replace("androidx.compose.remote.creation.compose", "creation.compose").replace(
        "androidx.compose.remote.foundation", "foundation"
    )


# ---------------------------------------------------------------------------------------------
# Where a story uses an API member: the one rule the coverage check, coverage.json and the
# Coverage page share. A member counts only where the code uses it on its receiver, as the API
# file declares it: `RemoteBrush.image(` for an extension on RemoteBrush.Companion, `.size(` for a
# RemoteModifier extension, `text = …` inside `semantics { }` for a RemoteSemanticsPropertyReceiver
# property, `drawCircle(` inside a RemoteDrawScope lambda. A top-level declaration also needs its
# import (`androidx.compose.remote…modifier.text`), so a same-named member of another receiver or
# package does not count. Overloads with the same package and name (`lerp` for RemoteColor and for
# RemoteFloat) cannot be told apart without types: both count where either is called.


def strip_code(text):
    """`text` with comments and the literal parts of strings blanked (same length), so a name in a
    comment or a string is not a use; a string template's expression (`"${x.y()}"`) is kept."""
    out = list(text)
    i = 0
    n = len(text)
    # Each entry is an open string (its quote) or a template expression ("{" with its depth).
    stack: list[list[Any]] = []

    def blank(start, end):
        for k in range(start, end):
            if out[k] != "\n":
                out[k] = " "

    while i < n:
        top = stack[-1] if stack else None
        if top is not None and top[0] in ('"', '"""'):
            quote = top[0]
            if text.startswith(quote, i):
                stack.pop()
                i += len(quote)
            elif text.startswith("${", i):
                stack.append(["{", 1])
                i += 2
            elif text[i] == "\\" and quote == '"':
                blank(i, min(i + 2, n))
                i += 2
            else:
                blank(i, i + 1)
                i += 1
            continue
        if text.startswith("//", i):
            end = text.find("\n", i)
            end = n if end < 0 else end
            blank(i, end)
            i = end
        elif text.startswith("/*", i):
            end = text.find("*/", i + 2)
            end = n if end < 0 else end + 2
            blank(i, end)
            i = end
        elif text.startswith('"""', i):
            stack.append(['"""'])
            i += 3
        elif text[i] == '"':
            stack.append(['"'])
            i += 1
        elif text[i] == "'":
            end = text.find("'", i + (3 if text[i + 1 : i + 2] == "\\" else 2))
            end = n if end < 0 else end + 1
            blank(i + 1, end - 1)
            i = end
        else:
            if top is not None and top[0] == "{":
                if text[i] == "{":
                    top[1] += 1
                elif text[i] == "}":
                    top[1] -= 1
                    if top[1] == 0:
                        stack.pop()
            i += 1
    return "".join(out)


def matching_bracket(text, index):
    """The index of the bracket that closes the one at `index`, or len(text)."""
    pairs = {"(": ")", "{": "}", "[": "]"}
    stack = []
    for i in range(index, len(text)):
        c = text[i]
        if c in pairs:
            stack.append(pairs[c])
        elif stack and c == stack[-1]:
            stack.pop()
            if not stack:
                return i
    return len(text)


def matching_angle(text, index):
    depth = 0
    for i in range(index, len(text)):
        if text[i] == "<":
            depth += 1
        elif text[i] == ">":
            depth -= 1
            if depth == 0:
                return i + 1
    return len(text)


def split_params(params):
    """The top-level comma-separated parts of a parameter list."""
    parts = []
    depth = 0
    start = 0
    for i, c in enumerate(params):
        if c in "(<":
            depth += 1
        elif c in ")>" and params[i - 1 : i + 1] != "->":
            depth -= 1
        elif c == "," and depth == 0:
            parts.append(params[start:i].strip())
            start = i + 1
    last = params[start:].strip()
    return parts + [last] if last else parts


def signature_params(e):
    """The parameters of a method or constructor entry, as written in its signature."""
    m = re.search(re.escape(e["name"]) + r"\(", e["signature"])
    if not m:
        return None
    open_index = m.end() - 1
    return split_params(e["signature"][open_index + 1 : matching_bracket(e["signature"], open_index)])


def lambda_params(e):
    """The function-typed parameters of `e`, in order: (name, the lambda's receiver or None)."""
    out = []
    for p in signature_params(e) or []:
        m = re.fullmatch(r"(?:optional\s+)?(?:([\w.]+)\.)?\(\)\s*->\s*Unit\s+(\w+)", p)
        if m:
            out.append((m.group(2), m.group(1)))
        elif re.fullmatch(r"(?:optional\s+)?kotlin\.jvm\.functions\.Function\d+<.*>\s+\w+", p):
            out.append((p.split()[-1], None))
    return out


class ApiIndex:
    """What the API file says about receivers: the lambdas each function takes and their receivers
    (`semantics { }` → RemoteSemanticsPropertyReceiver), what each type declares (members, and
    extensions on it), its supertypes, and which classes are objects (`RemoteTimeVariables.year`)."""

    def __init__(self, entries):
        # Opener name → the lambda parameters of each overload.
        self.openers: dict[str, list[list[tuple[str, str | None]]]] = {}
        self.lambda_receivers: set[str] = set()
        self.supertypes: dict[str, set[str]] = {}
        self.declared: dict[str, set[str]] = {}
        self.objects: set[str] = set()
        for e in entries:
            if e["kind"] == "class":
                m = re.match(
                    r".*?\b(?:class|interface)\s+([\w.]+)(?:<[^>]*>)?\s+(?:extends|implements)\s+(.+)$",
                    e["signature"],
                )
                if m:
                    supers = {s.strip().split("<")[0] for s in re.split(r",|\bimplements\b", m.group(2))}
                    self.supertypes.setdefault(m.group(1), set()).update(s for s in supers if s)
                continue
            owner = e["owner"] or ""
            if e["kind"] == "field" and e["name"] == "INSTANCE":
                self.objects.add(owner)
            facade = owner.endswith("Kt") or e.get("static")
            declaring = receiver_of(e) if facade else owner
            if declaring:
                self.declared.setdefault(declaring, set()).add(e["name"])
            if e["kind"] not in ("method", "ctor"):
                continue
            lambdas = lambda_params(e)
            if not any(receiver for _, receiver in lambdas):
                continue
            # `RemotePaint { … }` calls RemotePaint.Companion.invoke.
            name = (
                owner.split(".")[-2] if e["name"] == "invoke" and owner.endswith(".Companion") else e["name"]
            )
            if re.fullmatch(r"\w+", name):
                self.openers.setdefault(name, []).append(lambdas)
                self.lambda_receivers.update(receiver for _, receiver in lambdas if receiver)

    def is_subtype(self, sub, sup):
        seen = set()
        queue = [sub]
        while queue:
            t = queue.pop()
            if t == sup:
                return True
            if t not in seen:
                seen.add(t)
                queue.extend(self.supertypes.get(t, ()))
        return False

    def is_lambda_receiver(self, receiver):
        return any(self.is_subtype(t, receiver) for t in self.lambda_receivers)

    def declares(self, receiver, name):
        """Whether `receiver` or a supertype declares `name` (a member or an extension on it)."""
        return any(name in self.declared.get(t, ()) and self.is_subtype(receiver, t) for t in self.declared)


def call_lambdas(code, end):
    """The lambdas passed to a call whose name ends at `end`: [(start, end, parameter name or
    None)], the trailing lambda last with the name "<trailing>"."""
    n = len(code)
    i = end
    while i < n and code[i].isspace():
        i += 1
    if i < n and code[i] == "<":
        i = matching_angle(code, i)
        while i < n and code[i].isspace():
            i += 1
    out = []
    if i < n and code[i] == "(":
        close = matching_bracket(code, i)
        j = i + 1
        while j < close:
            c = code[j]
            if c in "([":
                j = matching_bracket(code, j) + 1
                continue
            if c == "{":
                before = code[i + 1 : j].rstrip()
                named = re.search(r"(?:^|[(,])\s*(\w+)\s*=$", before)
                if named or before == "" or before.endswith(","):
                    out.append((j, matching_bracket(code, j), named.group(1) if named else None))
                j = matching_bracket(code, j) + 1
                continue
            j += 1
        i = close + 1
        while i < n and code[i] in " \t":
            i += 1
    if i < n and code[i] == "{":
        out.append((i, matching_bracket(code, i), "<trailing>"))
    return out


def receiver_regions(code, index):
    """Spans of `code` with the implicit receiver each one adds: [(start, end, receiver)]. They are
    the lambdas passed to a function that gives its lambda a receiver (`semantics { … }`,
    `withTransform({ transform }) { draw }`), matched to its parameters by name, position, or as
    the trailing lambda, and the bodies of a story's own extensions (`fun RemoteRowScope.x() { … }`)."""
    regions = []
    if index.openers:
        names = "|".join(sorted((re.escape(n) for n in index.openers), key=len, reverse=True))
        for m in re.finditer(r"(?<![\w])(" + names + r")(?!\w)", code):
            lambdas = call_lambdas(code, m.end())
            if not lambdas:
                continue
            # The overload with as many lambda parameters as the call passes, or the first one.
            overloads = index.openers[m.group(1)]
            params = next((p for p in overloads if len(p) == len(lambdas)), overloads[0])
            positional = list(params)
            for start, end, name in lambdas:
                if name == "<trailing>":
                    param = params[-1] if params else None
                elif name is not None:
                    param = next((p for p in params if p[0] == name), None)
                else:
                    param = positional[0] if positional else None
                if param is not None and param in positional:
                    positional.remove(param)
                if param is not None and param[1]:
                    regions.append((start, end, param[1]))
    for m in re.finditer(r"\bfun\s+(?:<[^>]*>\s*)?([\w.]+)\.\w+\s*\(", code):
        brace = code.find("{", matching_bracket(code, m.end() - 1))
        if brace >= 0:
            regions.append((brace, matching_bracket(code, brace), m.group(1)))
    return regions


def implicit_used(code, pattern, name, receiver, index):
    """Whether `pattern` (a use of `name`) occurs where `receiver` is the implicit receiver that
    provides `name`: the innermost enclosing lambda whose receiver declares `name`, as Kotlin
    resolves it (inside `withTransform({ rotate() })`, `rotate` is RemoteDrawTransform's)."""
    regions = receiver_regions(code, index)
    for m in re.finditer(pattern, code):
        enclosing = sorted((r for r in regions if r[0] < m.start() < r[1]), key=lambda r: -r[0])
        resolved = next((r[2] for r in enclosing if index.declares(r[2], name)), None)
        if resolved is not None and index.is_subtype(resolved, receiver):
            return True
    return False


def receiver_of(e):
    """The extension receiver of a function or property, from its API signature: the unnamed
    first parameter (`image(RemoteBrush.Companion, …)`) or the `Type.` of `Type.name`."""
    if e["kind"] == "method":
        params = signature_params(e)
        first = params[0] if params else ""
        return first.rstrip("?") if re.fullmatch(r"[\w.]+(?:<.*>)?\??", first) else None
    if e["kind"] == "property":
        m = re.search(r"([\w.]+)\." + re.escape(e["name"]) + r"$", e["signature"])
        return m.group(1) if m else None
    return None


def use_specs(e):
    """How code uses the API entry `e`: a dict naming the import it needs (None for a member), its
    receiver (from receiver_of, or the owning class) and the shape of a use. See member_used."""
    name = e["name"]
    owner = e["owner"]
    fq = e["package"] + "." + name
    if e["kind"] == "class":
        return {"shape": "word", "import": fq, "name": name}
    if e["kind"] in ("ctor", "field", "enum_constant"):
        return {"shape": "word", "import": e["package"] + "." + owner.split(".")[0], "name": owner}
    call = e["kind"] == "method"
    if owner.endswith("Kt") or e.get("static"):
        receiver = receiver_of(e)
        return {"shape": "toplevel", "import": fq, "name": name, "call": call, "receiver": receiver}
    # A member extension (`fun RemoteModifier.weight()` inside RemoteRowScope) has its own receiver.
    extension = receiver_of(e) if call else None
    return {
        "shape": "member",
        "import": None,
        "name": name,
        "call": call,
        "receiver": owner,
        "extension": extension,
    }


def member_used(spec, code, imports, index):
    """Whether `code` uses the API member described by `spec` on its receiver. `imports` are the
    story's imports (rewritten to AndroidX), or None for a control option's code, which is printed
    into a story that has them."""
    if imports is not None and spec["import"] is not None and spec["import"] not in imports:
        return False
    name = re.escape(spec["name"])
    tail = r"\s*(?:<[^<>]*>\s*)?[({]" if spec.get("call") else r"(?!\w)"
    bare = r"(?<![\w.])" + name + tail
    dotted = r"\.\s*" + name + tail
    if spec["shape"] == "word":
        return re.search(r"(?<![\w.])" + name + r"(?!\w)", code) is not None
    receiver = spec["receiver"]
    if receiver is None:
        return re.search(bare, code) is not None
    if receiver.endswith(".Companion"):
        owner = re.escape(receiver[: -len(".Companion")])
        return re.search(r"(?<![\w.])" + owner + r"\s*\." + r"\s*" + name + tail, code) is not None
    if receiver in index.objects:
        owner = re.escape(receiver)
        aliases = [owner] + [
            re.escape(a) for a in re.findall(r"\bva[lr]\s+(\w+)\s*=\s*" + owner + r"(?![\w.(])", code)
        ]
        return re.search(r"(?<![\w.])(?:" + "|".join(aliases) + r")\s*\.\s*" + name + tail, code) is not None
    if index.is_lambda_receiver(receiver):
        # A lambda receiver (RemoteDrawScope, RemoteSemanticsPropertyReceiver, RemoteRowScope): its
        # members and extensions are used implicitly inside that lambda; a member extension
        # (`RemoteRowScope.weight` on RemoteModifier) is called there on its own receiver: `.weight(`.
        return implicit_used(code, dotted if spec.get("extension") else bare, spec["name"], receiver, index)
    return re.search(dotted, code) is not None


def block_specs(entries):
    return [use_specs(e) for e in entries]


def find_members(entries, query):
    """The API entries `query` names: `Owner.member` (a member of Owner or of its companion, or an
    extension on either), `package.name`, or a bare name."""
    owner, _, name = query.rpartition(".")
    return [
        e
        for e in entries
        if e["kind"] != "class"
        and e["name"] == name
        and (
            not owner
            or owner in (e["owner"], e["package"], receiver_of(e))
            or owner + ".Companion" in (e["owner"], receiver_of(e))
        )
    ]


def prepare_sources(sources):
    """{story function: (its code with strings and comments blanked, its imports)}."""
    return {fn: (strip_code(src["code"]), set(src["imports"])) for fn, src in sorted(sources.items())}


def stories_using(specs, prepared, index):
    return [
        fn
        for fn, (code, imports) in prepared.items()
        if any(member_used(s, code, imports, index) for s in specs)
    ]


def catalog_option_codes(catalog_dir):
    """Option codes the catalog offers in its controls (`option("Label", "Code", value)`): a value
    a user can pick and see printed in the Code tab counts as covered too."""
    codes = []
    if catalog_dir:
        for f in sorted(os.listdir(catalog_dir)):
            if f.endswith(".kt"):
                text = read_text(os.path.join(catalog_dir, f))
                codes += re.findall(r'option\(\s*"(?:\\.|[^"\\])*"\s*,\s*"((?:\\.|[^"\\])*)"', text)
    return sorted({c.replace('\\"', '"') for c in codes})


def coverage_report(entries, sources, option_codes):
    """Every building block with the stories and control options that use it and its status:
    covered (a story or an option uses it), pending (CANNOT_DEMONSTRATE names a reason) or missing.
    The only place coverage is decided: the CLI's check and the site both read this."""
    index = ApiIndex(entries)
    prepared = prepare_sources(sources)
    options = {c: strip_code(c) for c in option_codes}
    blocks = []
    for (group, name), block_entries in sorted(building_blocks(entries).items()):
        specs = block_specs(block_entries)
        stories = stories_using(specs, prepared, index)
        codes = [c for c, code in options.items() if any(member_used(s, code, None, index) for s in specs)]
        reason = CANNOT_DEMONSTRATE.get(name)
        status = "covered" if stories or codes else "pending" if reason else "missing"
        blocks.append(
            {
                "group": group,
                "name": name,
                "packages": sorted({short_package(e["package"]) for e in block_entries}),
                "status": status,
                "stories": stories,
                "optionCodes": codes,
                "reason": reason,
            }
        )
    summary = {s: sum(1 for b in blocks if b["status"] == s) for s in ("covered", "pending", "missing")}
    summary["total"] = len(blocks)
    return {"summary": summary, "blocks": blocks}


def cmd_coverage(args):
    entries = parse_api(args.api)
    report = coverage_report(entries, read_json(args.sources), catalog_option_codes(args.catalog))
    commit = read_text(os.path.join(args.api, "COMMIT")).strip()
    write_json({"commit": commit, **report}, args.out)
    s = report["summary"]
    print(
        f"coverage: {s['covered']}/{s['total']} public building blocks used by a story, "
        f"{s['pending']} that cannot be demonstrated yet (reasons in coverage.json) -> {args.out}"
    )
    errors = [
        f"not covered by any story: {b['group']}: {b['name']}"
        for b in report["blocks"]
        if b["status"] == "missing"
    ]
    # A reason for a block a story now covers is stale: the page would never show it.
    errors += [
        f"CANNOT_DEMONSTRATE lists {b['name']}, but it is covered: remove the entry"
        for b in report["blocks"]
        if b["status"] == "covered" and b["reason"]
    ]
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 0 if args.report_only else 1
    return 0


def cmd_uses(args):
    """The stories that use an API member, by the coverage rule: `Owner.member` (a member, or an
    extension on Owner), `package.name` or a bare name. For checking docs such as the API diff."""
    entries = parse_api(args.api)
    index = ApiIndex(entries)
    prepared = prepare_sources(read_json(args.sources))
    status = 0
    for query in args.names:
        matched = find_members(entries, query)
        if not matched:
            print(f"{query}: no such member in the API files", file=sys.stderr)
            status = 1
            continue
        stories = stories_using(block_specs(matched), prepared, index)
        print(f"{query}: {', '.join(stories) if stories else '(no story)'}")
    return status


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
    u = sub.add_parser("uses", help="list the stories that use API members, by the coverage rule")
    u.add_argument("--api", required=True)
    u.add_argument("--sources", required=True)
    u.add_argument("names", nargs="+", help="Owner.member, package.name or name")
    args = p.parse_args()
    return {"sources": cmd_sources, "api": cmd_api, "coverage": cmd_coverage, "uses": cmd_uses}[args.cmd](
        args
    )


if __name__ == "__main__":
    sys.exit(main())
