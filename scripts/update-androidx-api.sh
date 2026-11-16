#!/usr/bin/env bash
# Refreshes reference/androidx-api from androidx-main (or the commit given as $1).
# Downloads into a temporary directory first, so a failed download leaves the reference untouched.
set -euo pipefail
cd "$(dirname "$0")/.."
REF="${1:-androidx-main}"
SHA=$(curl -sf "https://api.github.com/repos/androidx/androidx/commits/$REF" |
  python3 -c 'import json,sys; print(json.load(sys.stdin)["sha"])')
BASE="https://raw.githubusercontent.com/androidx/androidx/$SHA/compose/remote"
OUT=reference/androidx-api
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
curl -sf "$BASE/remote-creation-compose/api/current.txt" -o "$TMP/remote-creation-compose.txt"
curl -sf "$BASE/remote-creation-expression/api/current.txt" -o "$TMP/remote-creation-expression.txt"
curl -sf "$BASE/foundation/foundation/api/current.txt" -o "$TMP/foundation.txt"
echo "$SHA" >"$TMP/COMMIT"
# The newest androidx.dev snapshot build: what verify-androidx compiles the stories against.
curl -sfL -o /dev/null -w "%{url_effective}" https://androidx.dev/snapshots/latest/artifacts/repository/ |
  sed -nE 's#.*/builds/([0-9]+)/.*#\1#p' >"$TMP/SNAPSHOT_BUILD"
[[ -s "$TMP/SNAPSHOT_BUILD" ]] || { echo "could not resolve the latest androidx.dev snapshot build" >&2; exit 1; }
mv "$TMP"/remote-creation-compose.txt "$TMP"/remote-creation-expression.txt "$TMP"/foundation.txt \
  "$TMP"/COMMIT "$TMP"/SNAPSHOT_BUILD "$OUT"/
echo "reference/androidx-api now at androidx-main $SHA, snapshot build $(cat "$OUT/SNAPSHOT_BUILD")"
