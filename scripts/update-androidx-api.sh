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
# Downloads into a staging directory next to the reference, checks it, then swaps the whole
# directory in (two renames, never a mix of old and new files): the API files and their version
# marker (RELEASE or SNAPSHOT_BUILD) are published together or not at all, so a failed or
# interrupted refresh leaves the old baseline in place.
set -euo pipefail
cd "$(dirname "$0")/.."
REF="${1:-androidx-main}"
RELEASE="${2:-}"
SHA=$(curl -sf "https://api.github.com/repos/androidx/androidx/commits/$REF" |
  python3 -c 'import json,sys; print(json.load(sys.stdin)["sha"])')
BASE="https://raw.githubusercontent.com/androidx/androidx/$SHA/compose/remote"
OUT=reference/androidx-api
TMP=$(mktemp -d "$OUT.new.XXXXXX")
trap 'rm -rf "$TMP"' EXIT
API_FILES=(remote-creation-compose.txt remote-creation-expression.txt foundation.txt remote-material3.txt)

# Publishes $TMP as $OUT once every API file, COMMIT and the version marker $1 are there and not
# empty (a RELEASE must pin all three artifacts, Wear's included).
publish() {
  local marker=$1 f
  for f in "${API_FILES[@]}" COMMIT "$marker"; do
    [[ -s "$TMP/$f" ]] || { echo "refusing to publish: $f is missing or empty" >&2; exit 1; }
  done
  if [[ "$marker" == RELEASE ]]; then
    for f in remote-creation-compose foundation remote-material3; do
      grep -q "^$f=." "$TMP/RELEASE" || { echo "refusing to publish: RELEASE does not pin $f" >&2; exit 1; }
    done
  fi
  cp "$OUT/README.md" "$TMP/"
  chmod 755 "$TMP"
  rm -rf "$OUT.old"
  mv "$OUT" "$OUT.old"
  mv "$TMP" "$OUT" || { mv "$OUT.old" "$OUT"; exit 1; }
  rm -rf "$OUT.old"
}
curl -sf "$BASE/remote-creation-compose/api/current.txt" -o "$TMP/remote-creation-compose.txt"
curl -sf "$BASE/remote-creation-expression/api/current.txt" -o "$TMP/remote-creation-expression.txt"
curl -sf "$BASE/foundation/foundation/api/current.txt" -o "$TMP/foundation.txt"
curl -sf "https://raw.githubusercontent.com/androidx/androidx/$SHA/wear/compose/remote/remote-material3/api/current.txt" \
  -o "$TMP/remote-material3.txt"
echo "$SHA" >"$TMP/COMMIT"
if [[ -n "$RELEASE" ]]; then
  curl -sf "https://raw.githubusercontent.com/androidx/androidx/$SHA/libraryversions.toml" |
    python3 -c '
import re, sys
text, release = sys.stdin.read(), sys.argv[1]
def version(key):
    return re.search(r"^" + key + r" = \"([^\"]+)\"", text, re.M).group(1)
compose, foundation = version("REMOTECOMPOSE"), version("COMPOSE_REMOTE_FOUNDATION")
wear = version("REMOTECOMPOSE_WEAR")
if compose != release:
    sys.exit(f"libraryversions.toml at this commit names remote-creation-compose {compose}, not {release}")
print(f"remote-creation-compose={compose}\nfoundation={foundation}\nremote-material3={wear}")' "$RELEASE" >"$TMP/RELEASE"
  while IFS='=' read -r artifact version; do
    group=androidx/compose/remote
    [[ "$artifact" == foundation ]] && group=androidx/compose/remote/foundation
    [[ "$artifact" == remote-material3 ]] && group=androidx/wear/compose/remote
    curl -sfI "https://dl.google.com/android/maven2/$group/$artifact/$version/$artifact-$version.pom" >/dev/null ||
      { echo "$group/$artifact:$version is not on Google Maven" >&2; exit 1; }
  done <"$TMP/RELEASE"
  publish RELEASE
  echo "reference/androidx-api now at $RELEASE (androidx $SHA): $(tr '\n' ' ' <"$OUT/RELEASE")"
  exit 0
fi
# The newest androidx.dev snapshot build: what verify-androidx compiles the stories against.
# Only the redirect matters (it names the build); the redirect target itself can 404, so no -f here.
curl -sL -o /dev/null -w "%{url_effective}" https://androidx.dev/snapshots/latest/artifacts/repository/ |
  sed -nE 's#.*/builds/([0-9]+)/.*#\1#p' >"$TMP/SNAPSHOT_BUILD" || true
[[ -s "$TMP/SNAPSHOT_BUILD" ]] || { echo "could not resolve the latest androidx.dev snapshot build" >&2; exit 1; }
curl -sfI "https://androidx.dev/snapshots/builds/$(cat "$TMP/SNAPSHOT_BUILD")/artifacts/repository/androidx/compose/remote/remote-creation-compose/maven-metadata.xml" >/dev/null ||
  { echo "androidx.dev build $(cat "$TMP/SNAPSHOT_BUILD") has no remote-creation-compose" >&2; exit 1; }
curl -sfI "https://androidx.dev/snapshots/builds/$(cat "$TMP/SNAPSHOT_BUILD")/artifacts/repository/androidx/wear/compose/remote/remote-material3/maven-metadata.xml" >/dev/null ||
  { echo "androidx.dev build $(cat "$TMP/SNAPSHOT_BUILD") has no remote-material3" >&2; exit 1; }
publish SNAPSHOT_BUILD
echo "reference/androidx-api now at androidx-main $SHA, snapshot build $(cat "$OUT/SNAPSHOT_BUILD")"
