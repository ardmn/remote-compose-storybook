# AndroidX public API reference

`api/current.txt` of the Remote Compose creation modules (`remote-creation-compose`,
`remote-creation-expression`, `foundation/foundation`) at the androidx commit in `COMMIT`.

`COMMIT` is the commit AndroidX `1.0.0-alpha24` was cut from; `RELEASE` names the released versions
(`remote-creation-compose` 1.0.0-alpha24, `foundation` 1.0.0-alpha07) that `scripts/verify-androidx.sh`
compiles the stories against. rc-players' Compose Multiplatform fork of the same API (package
`ee.schimke.composeai.rccreation`, rc-creation-* 3.10.0) is pinned to the same tree, so every block
here can run in the browser unless `CANNOT_DEMONSTRATE` in `tools/storybook_tools.py` says why not.

The Docs tab's signatures and the API coverage page are built from these files.

Refresh with `scripts/update-androidx-api.sh <sha> <release>` for a release, or with no arguments for
androidx-main and the newest androidx.dev snapshot (`SNAPSHOT_BUILD` instead of `RELEASE`).
