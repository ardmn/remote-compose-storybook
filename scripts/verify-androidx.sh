#!/usr/bin/env bash
# Compiles the stories, as the Code tab shows them (androidx.compose.remote.*), against AndroidX's
# own artifacts: the release in reference/androidx-api/RELEASE, or else the androidx.dev snapshot
# pinned in reference/androidx-api/SNAPSHOT_BUILD.
# Needs an Android SDK (ANDROID_HOME, or verify-androidx/local.properties).
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/storybook_tools.py sources --stories storybook/src/wasmJsMain/kotlin/rcstorybook/stories \
  --api reference/androidx-api --out verify-androidx/build/sources.json \
  --verify-out verify-androidx/build/generated/stories \
  --snippets-out verify-androidx/build/generated/snippets
cd verify-androidx
./gradlew --console=plain -q compileReleaseKotlin
if [[ -f ../reference/androidx-api/RELEASE ]]; then
  AGAINST="AndroidX $(tr '\n' ' ' <../reference/androidx-api/RELEASE)"
else
  AGAINST="androidx.dev build $(cat ../reference/androidx-api/SNAPSHOT_BUILD)"
fi
echo "verify-androidx: every story file and every Code tab snippet compiles against $AGAINST"
