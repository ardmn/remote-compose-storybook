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

REPO_URL="${STORYBOOK_REPOSITORY:-$(git remote get-url origin 2>/dev/null | sed -E 's#git@github.com:#https://github.com/#; s#\.git$##' || true)}"
cat > "$OUT/data/meta.json" <<JSON
{
  "androidxCommit": "$(cat "$API/COMMIT")",
  "androidxRelease": "$(curl -sf https://dl.google.com/android/maven2/androidx/compose/remote/group-index.xml | sed -n 's/.*<remote-creation-compose versions="[^"]*,\([^,"]*\)".*/\1/p')",
  "rcPlayersVersion": "$(sed -n 's/^rc-players = "\(.*\)"/\1/p' gradle/libs.versions.toml)",
  "repository": "${REPO_URL}",
  "builtFrom": "$(git rev-parse --short HEAD 2>/dev/null || echo local)"
}
JSON
du -sh "$OUT" | awk '{print "site: " $1 " in build/site"}'
