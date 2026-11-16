#!/usr/bin/env bash
# Refreshes reference/androidx-api from androidx-main (or the commit given as $1).
set -euo pipefail
cd "$(dirname "$0")/.."
REF="${1:-androidx-main}"
SHA=$(curl -sf "https://api.github.com/repos/androidx/androidx/commits/$REF" | python3 -c 'import json,sys; print(json.load(sys.stdin)["sha"])')
BASE="https://raw.githubusercontent.com/androidx/androidx/$SHA/compose/remote"
OUT=reference/androidx-api
curl -sf "$BASE/remote-creation-compose/api/current.txt" -o "$OUT/remote-creation-compose.txt"
curl -sf "$BASE/remote-creation-expression/api/current.txt" -o "$OUT/remote-creation-expression.txt"
curl -sf "$BASE/foundation/foundation/api/current.txt" -o "$OUT/foundation.txt"
echo "$SHA" > "$OUT/COMMIT"
echo "reference/androidx-api now at androidx-main $SHA"
