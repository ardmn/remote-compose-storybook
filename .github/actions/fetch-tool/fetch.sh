#!/usr/bin/env bash
# The fetch-tool action's step (action.yml passes its inputs as NAME, URL, SHA256, ARCHIVE_PATH).
# A file of its own so the lint job's shellcheck covers it.
set -euo pipefail
[[ "$NAME" =~ ^[A-Za-z0-9._-]+$ ]] || { echo "::error::name must be a plain command name: '$NAME'"; exit 1; }
[[ "$SHA256" =~ ^[0-9a-f]{64}$ ]] || { echo "::error::sha256 must be 64 lowercase hex digits"; exit 1; }
dir="$RUNNER_TEMP/tools/$NAME"
rm -rf "$dir"
mkdir -p "$dir/bin"
download="$dir/download"
curl -sSfL --retry 3 --retry-all-errors -o "$download" "$URL"
echo "$SHA256  $download" | sha256sum -c -
if [[ -n "$ARCHIVE_PATH" ]]; then
  tar -xf "$download" -C "$dir" "$ARCHIVE_PATH"
  mv "$dir/$ARCHIVE_PATH" "$dir/bin/$NAME"
else
  mv "$download" "$dir/bin/$NAME"
fi
chmod +x "$dir/bin/$NAME"
echo "$dir/bin" >> "$GITHUB_PATH"
