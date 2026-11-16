package rcstorybook.catalog

import androidx.compose.ui.graphics.Color
import rcstorybook.stories.*

private const val C = "androidx.compose.remote.creation.compose"

internal val recipeComponents =
    listOf(
        Component(
            id = "recipe-product-card",
            title = "Product card",
            category = "Recipes",
            symbols =
                listOf(
                    "$C.layout.RemoteImage",
                    "$C.modifier.clickable",
                    "$C.action.combinedAction",
                ),
            description =
                "A product card composed from the building blocks: image, text, a button whose cart count " +
                    "lives in the document, and a host action for the app.",
            stories =
                listOf(
                    Story(
                        "recipe-product-card--default",
                        "Product card",
                        "ProductCard",
                        "Tap the button: the count updates on the device and the app gets add_to_cart (Actions tab).",
                        listOf(
                            text("title", "Blood orange, 1 kg"),
                            text("price", "€3.49"),
                            bool("inStock", true),
                            color("accent", Color(0xFFE65100)),
                        ),
                        height = 400,
                    ) {
                        ProductCard(
                            it.text("title"),
                            it.text("price"),
                            it.bool("inStock"),
                            it.color("accent"),
                        )
                    },
                ),
        ),
        Component(
            id = "recipe-weather-widget",
            title = "Weather widget",
            category = "Recipes",
            symbols =
                listOf(
                    "$C.layout.RemoteFitBox",
                    "$C.state.rememberNamedRemoteInt",
                    "$C.state.RemoteTimeVariables",
                ),
            description =
                "A home-screen style widget with three layouts the player chooses by size, live data from " +
                    "the app through named values, and the device's clock.",
            stories =
                listOf(
                    Story(
                        "recipe-weather-widget--default",
                        "Weather widget",
                        "WeatherWidget",
                        "Change Viewport to Widget 320×180 or Small 240×240 to see the layout switch; change " +
                            "temperature in the Host values tab to see live data arrive without a new document.",
                        listOf(
                            text("city", "Yerevan"),
                            int("temperature", 14, -30, 45),
                            text("condition", "Sunny"),
                        ),
                        width = 320,
                        height = 180,
                    ) {
                        WeatherWidget(it.text("city"), it.int("temperature"), it.text("condition"))
                    },
                ),
        ),
        Component(
            id = "recipe-onboarding",
            title = "Onboarding",
            category = "Recipes",
            symbols = listOf("$C.layout.RemoteStateLayout", "$C.action.valueChangeAction"),
            description =
                "A three-step flow: every step is in the document and the player moves between them.",
            stories =
                listOf(
                    Story(
                        "recipe-onboarding--default",
                        "Onboarding",
                        "Onboarding",
                        "Tap Next.",
                        listOf(color("accent", Color(0xFF3949AB))),
                        height = 380,
                    ) {
                        Onboarding(it.color("accent"))
                    },
                ),
        ),
        Component(
            id = "recipe-media-player",
            title = "Media player",
            category = "Recipes",
            symbols = listOf("$C.layout.RemoteCanvas", "$C.state.RemoteTimeVariables"),
            description =
                "Play/pause on the device, a progress bar driven by the player's clock, and a host action.",
            stories =
                listOf(
                    Story(
                        "recipe-media-player--default",
                        "Media player",
                        "MediaPlayer",
                        "Tap play.",
                        listOf(
                            text("track", "Northern Lights"),
                            text("artist", "The Remote Band"),
                            int("trackSeconds", 20, 5, 240),
                        ),
                    ) {
                        MediaPlayer(it.text("track"), it.text("artist"), it.int("trackSeconds"))
                    },
                ),
        ),
        Component(
            id = "recipe-settings",
            title = "Settings screen",
            category = "Recipes",
            symbols = listOf("$C.modifier.semantics", "$C.state.rememberNamedRemoteBoolean"),
            description =
                "Switches kept on the device and reported to the app, plus values the app sets (Wi-Fi, brightness).",
            stories =
                listOf(
                    Story(
                        "recipe-settings--default",
                        "Settings screen",
                        "SettingsScreen",
                        "Tap a switch; set wifi or brightness in the Host values tab.",
                        listOf(color("accent", Color(0xFF00897B))),
                    ) {
                        SettingsScreen(it.color("accent"))
                    },
                ),
        ),
    )
