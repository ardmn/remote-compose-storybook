# Fonts for the web player

Copied from rc-players (`rc-player/wasm/dist-assets/fonts`), all SIL OFL-1.1 — see the `*-OFL.txt`
files. The web player has no system fonts, so without these every family drew in one face.

| Family the document asks for | Face                      | Note                                                                                   |
| ---------------------------- | ------------------------- | -------------------------------------------------------------------------------------- |
| default, sans-serif          | Roboto Flex (variable)    | Android's default face; weights and width/slant axes work                              |
| serif                        | Noto Serif (variable)     | what Android maps `serif` to                                                           |
| monospace                    | Noto Sans Mono (variable) |                                                                                        |
| cursive                      | Lobster Two               | a script face standing in for Android's Dancing Script, which rc-players does not ship |

`fonts.json` is the manifest `RcManifestTypefaceLoader` reads.
