#!/usr/bin/env bash
# Refreshes reference/androidx-api from androidx-main (or the commit given as $1).
#
#   scripts/update-androidx-api.sh                          androidx-main + newest androidx.dev snapshot
#   scripts/update-androidx-api.sh <sha> <release>          an AndroidX release, e.g.
#   scripts/update-androidx-api.sh a9dc564e55c 1.0.0-alpha24  (the commit the release was cut from)
#
# With a release, verify-androidx compiles against the released artifacts on Google Maven
# (RELEASE: one `artifact=version` per line) instead of a snapshot (SNAPSHOT_BUILD). The versions
# come from libraryversions.toml at <sha>, which must name <release> for remote-creation-compose.
# Downloads into a temporary directory first, so a failed download leaves the reference untouched.
set -euo pipefail
cd "$(dirname "$0")/.."
REF="${1:-androidx-main}"
RELEASE="${2:-}"
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
if [[ -n "$RELEASE" ]]; then
  curl -sf "https://raw.githubusercontent.com/androidx/androidx/$SHA/libraryversions.toml" |
    python3 -c '
import re, sys
text, release = sys.stdin.read(), sys.argv[1]
def version(key):
    return re.search(r"^" + key + r" = \"([^\"]+)\"", text, re.M).group(1)
compose, foundation = version("REMOTECOMPOSE"), version("COMPOSE_REMOTE_FOUNDATION")
if compose != release:
    sys.exit(f"libraryversions.toml at this commit names remote-creation-compose {compose}, not {release}")
print(f"remote-creation-compose={compose}\nfoundation={foundation}")' "$RELEASE" >"$TMP/RELEASE"
  while IFS='=' read -r artifact version; do
    group=androidx/compose/remote
    [[ "$artifact" == foundation ]] && group=androidx/compose/remote/foundation
    curl -sfI "https://dl.google.com/android/maven2/$group/$artifact/$version/$artifact-$version.pom" >/dev/null ||
      { echo "$group/$artifact:$version is not on Google Maven" >&2; exit 1; }
  done <"$TMP/RELEASE"
  mv "$TMP"/remote-creation-compose.txt "$TMP"/remote-creation-expression.txt "$TMP"/foundation.txt \
    "$TMP"/COMMIT "$TMP"/RELEASE "$OUT"/
  rm -f "$OUT/SNAPSHOT_BUILD"
  echo "reference/androidx-api now at $RELEASE (androidx $SHA): $(tr '\n' ' ' <"$OUT/RELEASE")"
  exit 0
fi
# The newest androidx.dev snapshot build: what verify-androidx compiles the stories against.
curl -sfL -o /dev/null -w "%{url_effective}" https://androidx.dev/snapshots/latest/artifacts/repository/ |
  sed -nE 's#.*/builds/([0-9]+)/.*#\1#p' >"$TMP/SNAPSHOT_BUILD"
[[ -s "$TMP/SNAPSHOT_BUILD" ]] || { echo "could not resolve the latest androidx.dev snapshot build" >&2; exit 1; }
mv "$TMP"/remote-creation-compose.txt "$TMP"/remote-creation-expression.txt "$TMP"/foundation.txt \
  "$TMP"/COMMIT "$TMP"/SNAPSHOT_BUILD "$OUT"/
rm -f "$OUT/RELEASE"
echo "reference/androidx-api now at androidx-main $SHA, snapshot build $(cat "$OUT/SNAPSHOT_BUILD")"
