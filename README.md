# Remote Compose Storybook

[![Build, test and deploy](https://github.com/ardmn/remote-compose-storybook/actions/workflows/pages.yml/badge.svg)](https://github.com/ardmn/remote-compose-storybook/actions/workflows/pages.yml)
[![Live site](https://img.shields.io/badge/live-ardmn.github.io%2Fremote--compose--storybook-2a5bc4)](https://ardmn.github.io/remote-compose-storybook/)
[![License](https://img.shields.io/github/license/ardmn/remote-compose-storybook)](LICENSE)

**Every Jetpack Remote Compose component, live in your browser: an interactive demo, the Kotlin code and the API for each one.**

**Live site: https://ardmn.github.io/remote-compose-storybook/**

[![Remote Compose Storybook: Jetpack Remote Compose components with live demos](docs/preview.jpg)](https://ardmn.github.io/remote-compose-storybook/)

Remote Compose (`androidx.compose.remote`) is Jetpack's server-driven UI: a server describes a piece of Android UI as a small document and the app plays it, with no app release. This catalog shows what you can build with it, for developers, designers and product managers.

Playing Remote Compose in a React Native app? See [react-native-remote-compose](https://github.com/ardmn/react-native-remote-compose).

A Storybook-like static site for the public `@RemoteComposable` creation API of Jetpack Remote Compose (`androidx.compose.remote.*`). Every building block has one or more stories: Kotlin `@RemoteComposable` functions compiled to Kotlin/Wasm against [rc-players](https://github.com/yschimke/rc-players) (a Compose Multiplatform port of AndroidX's creation API, package `ee.schimke.composeai.rccreation`) and played live in the browser by rc-players' `RcEmbeddedPlayer`.

## Features

Every component explains in plain words what it is and what to use it for, so designers and product managers can see what Remote Compose can do; developers get the code, signatures and documents behind it.

The stage has a viewport size, a light or dark player theme and a centered or top-start layout. Each story has these tabs:

- **Controls**: edit the story's args; the story is re-created and replayed live.
- **Code**: the story's source rewritten to `androidx.compose.remote.*` imports, with the call for the current control values.
- **Docs**: the AndroidX API signatures for the component, taken from `api/current.txt`.
- **Document**: the remote document the story produced.
- **Changes**: the structural diff against the previous document, e.g. after changing a control.
- **Host values**: set the document's named values as the host app would; the same document replays with them, no new capture.
- **Actions**: host actions triggered in the player (for example by tapping a story that defines one).

Pages:

- **Overview**: what Remote Compose is, how to depend on it, and every component.
- **API coverage**: every public building block in the AndroidX API and the stories that cover it. The build fails when a public building block has no story. Public blocks no story can demonstrate yet (an AndroidX limitation, or API newer than the fork) are listed with the reason (`CANNOT_DEMONSTRATE` in `tools/storybook_tools.py`).

## How it works

- Stories live in `storybook/src/wasmJsMain/kotlin/rcstorybook/stories/*.kt`. They are registered with their controls in `storybook/src/wasmJsMain/kotlin/rcstorybook/catalog/*.kt`.
- `tools/storybook_tools.py` extracts each story's source (rewritten to AndroidX packages, with every `androidx.compose.remote` import checked against AndroidX's `api/current.txt`), parses the API signatures in `reference/androidx-api/*.txt`, and produces the API coverage report.
- Gradle compiles the storybook to Wasm. `web/` holds the plain HTML/JS page shell.
- `scripts/build-site.sh` assembles everything into `build/site`.
- `e2e/run.mjs` (Playwright) opens every story in Chromium and checks that it renders, that its Code and Docs tabs are populated, that changing a control produces a structural diff, and that host actions reach the Actions tab. Screenshots go to `e2e/out/`.
- `e2e/social.mjs` writes link previews: for every story a page `s/<story-id>/` with its own Open Graph tags and a 1200×630 card (`card.jpg`, the component, what it is for and the story's stage), plus the site card. Chat apps and social networks never see the `#/story/...` part of a link, so "Copy link" hands out the `s/<story-id>/` address, which opens the story with the same args. Absolute URLs use `SITE_URL`, or the GitHub Pages address of the repository.

## Versions

The catalog follows the newest API: `reference/androidx-api` is AndroidX's `api/current.txt` at androidx-main (commit in `COMMIT`), and the stories run on the newest rc-players release, whose fork of the creation API is pinned a few days behind androidx-main. The Overview page shows the Gradle setup for AndroidX snapshots, which have every block shown here.

## Local development

Requirements: JDK 17, Python 3, Node 20+.

```sh
scripts/build-site.sh --dev            # fast, unoptimized build into build/site
python3 -m http.server 8080 -d build/site
# open http://localhost:8080
```

`scripts/build-site.sh` without `--dev` makes the optimized production build that is deployed.

End-to-end test (needs a built `build/site`):

```sh
cd e2e && npm ci && npx playwright install --with-deps chromium && node social.mjs && node run.mjs
# node run.mjs --only <story-id-substring> runs a subset
```

## Adding a story

1. Write a `@Composable @RemoteComposable` function in `storybook/src/wasmJsMain/kotlin/rcstorybook/stories/`, for example in `Layout.kt`:

```kotlin
@Composable
@RemoteComposable
fun BoxContentAlignment(contentAlignment: RemoteAlignment, boxSize: Int, childSize: Int) {
    RemoteBox(
        modifier = RemoteModifier.size(boxSize.rdp).background(Color(0xFFE3F2FD).rc),
        contentAlignment = contentAlignment,
    ) {
        RemoteBox(RemoteModifier.size(childSize.rdp).background(Color(0xFF1E88E5).rc))
    }
}
```

2. Register a `Story(...)` with its args in the matching component in a file under `storybook/src/wasmJsMain/kotlin/rcstorybook/catalog/`, for example `LayoutCatalog.kt`:

```kotlin
Story(
    id = "remote-box--content-alignment", name = "Content alignment", function = "BoxContentAlignment",
    description = "Every RemoteAlignment and RemoteAbsoluteAlignment value applied to one child.",
    args = listOf(
        choice("contentAlignment", Options.alignments, default = 4),
        int("boxSize", 220, 80, 320, step = 10),
        int("childSize", 72, 16, 200, step = 4),
    ),
) { BoxContentAlignment(it["contentAlignment"], it.int("boxSize"), it.int("childSize")) },
```

3. Rebuild with `scripts/build-site.sh --dev`. The Code tab, API coverage and e2e run pick the story up automatically.

## Updating versions

- AndroidX API: `scripts/update-androidx-api.sh [ref-or-sha]` refreshes `reference/androidx-api/*.txt` from `androidx-main` (or the given ref) and records the commit in `reference/androidx-api/COMMIT`. Rebuild afterwards: new public building blocks without a story fail the coverage check.
- rc-players: change `rc-players` in `gradle/libs.versions.toml`, rebuild, and re-check `CANNOT_DEMONSTRATE`.

## Deployment

`.github/workflows/pages.yml` builds the site and runs the e2e test on every push to `main`, on pull requests and on manual dispatch. Pull requests only build and test. Pushes to `main` also deploy `build/site` to GitHub Pages.

One-time setup: in the repository, Settings → Pages → Source: GitHub Actions.

## License

Apache-2.0 (see `LICENSE`), as AndroidX and rc-players are. The page uses no web fonts. The sample image (`web/assets/sample.png`) is generated.
