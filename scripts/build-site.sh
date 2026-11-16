#!/usr/bin/env bash
# Builds the static site into build/site: the optimized Wasm storybook, the page shell and the
# data the page reads (story sources rewritten to AndroidX, API signatures, API coverage).
#
#   scripts/build-site.sh            production build (what GitHub Pages serves)
#   scripts/build-site.sh --dev      development build (faster, unoptimized)
set -euo pipefail
cd "$(dirname "$0")/.."

MODE=production
[[ "${1:-}" == "--dev" ]] && MODE=development

OUT=build/site
STORIES=storybook/src/wasmJsMain/kotlin/rcstorybook/stories
API=reference/androidx-api

rm -rf "$OUT"
mkdir -p "$OUT/data"

python3 tools/storybook_tools.py sources --stories "$STORIES" --api "$API" --out "$OUT/data/sources.json"
python3 tools/storybook_tools.py api --api "$API" --out "$OUT/data/api.json"
python3 tools/storybook_tools.py coverage --api "$API" --sources "$OUT/data/sources.json" \
  --catalog storybook/src/wasmJsMain/kotlin/rcstorybook/catalog --out "$OUT/data/coverage.json" \
  ${COVERAGE_REPORT_ONLY:+--report-only}

if [[ "$MODE" == production ]]; then
  ./gradlew --console=plain -q :storybook:wasmJsBrowserDistribution
  DIST=storybook/build/dist/wasmJs/productionExecutable
else
  ./gradlew --console=plain -q :storybook:wasmJsBrowserDevelopmentExecutableDistribution
  DIST=storybook/build/dist/wasmJs/developmentExecutable
fi
cp -R "$DIST"/. "$OUT"/
cp -R web/. "$OUT"/
# GitHub Pages: serve files and directories that start with an underscore as they are.
touch "$OUT/.nojekyll"

# What the page shows about versions. Written with json.dump, so nothing here needs escaping.
REPO_URL="${STORYBOOK_REPOSITORY:-$(git remote get-url origin 2>/dev/null | sed -E 's#git@github.com:#https://github.com/#; s#\.git$##' || true)}"
REPO_URL="$REPO_URL" BUILT_FROM="$(git rev-parse --short HEAD 2>/dev/null || echo local)" python3 - "$API/COMMIT" "$OUT/data/meta.json" <<'PY'
import json, os, re, sys, urllib.request

commit_file, out = sys.argv[1:3]
release = ""
try:
    index = urllib.request.urlopen(
        "https://dl.google.com/android/maven2/androidx/compose/remote/group-index.xml", timeout=20
    ).read().decode()
    versions = re.search(r'<remote-creation-compose versions="([^"]*)"', index)
    if versions:
        release = versions.group(1).split(",")[-1]
except OSError as error:
    print(f"meta: could not read the latest AndroidX release ({error})", file=sys.stderr)
with open(commit_file, encoding="utf-8") as fh:
    commit = fh.read().strip()
with open("gradle/libs.versions.toml", encoding="utf-8") as fh:
    rc_players = re.search(r'^rc-players = "([^"]+)"', fh.read(), re.M).group(1)
meta = {
    "androidxCommit": commit,
    "androidxRelease": release,
    "rcPlayersVersion": rc_players,
    "repository": os.environ.get("REPO_URL", ""),
    "builtFrom": os.environ.get("BUILT_FROM", ""),
}
with open(out, "w", encoding="utf-8") as fh:
    json.dump(meta, fh, indent=2)
PY
du -sh "$OUT" | awk '{print "site: " $1 " in build/site"}'
