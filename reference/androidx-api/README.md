# AndroidX public API reference

`api/current.txt` of the Remote Compose creation modules (`remote-creation-compose`,
`remote-creation-expression`, `foundation/foundation`) at the androidx-main commit in `COMMIT`.

The Docs tab's signatures and the API coverage page are built from these files. The stories run on
rc-players' Compose Multiplatform fork of the same API (package `ee.schimke.composeai.rccreation`),
whose pin is a few days behind androidx-main; anything public here that the fork does not have yet
is listed on the coverage page as not yet runnable in the browser.

Refresh with `scripts/update-androidx-api.sh` (latest androidx-main).
