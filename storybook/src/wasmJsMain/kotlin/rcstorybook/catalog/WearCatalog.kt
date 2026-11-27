package rcstorybook.catalog

import androidx.compose.ui.graphics.Color
import ee.schimke.composeai.rccreation.wear.material3.RemoteEdgeButtonSize
import rcstorybook.stories.*

private const val W = "androidx.wear.compose.remote.material3"

private const val CATEGORY = "Wear Material 3"

/** The diameter, in dp, of the round watch the Wear stories open on (a large Wear OS watch). */
private const val WATCH = 227

private val buttonStyles =
    WearButtonStyle.entries.map { option(it.name, "WearButtonStyle.${it.name}", it) }

private val edgeButtonSizes =
    listOf(
        option("ExtraSmall", "RemoteEdgeButtonSize.ExtraSmall", RemoteEdgeButtonSize.ExtraSmall),
        option("Small", "RemoteEdgeButtonSize.Small", RemoteEdgeButtonSize.Small),
        option("Medium", "RemoteEdgeButtonSize.Medium", RemoteEdgeButtonSize.Medium),
        option("Large", "RemoteEdgeButtonSize.Large", RemoteEdgeButtonSize.Large),
    )

/** A story on a round watch screen. */
private fun watchStory(
    id: String,
    name: String,
    function: String,
    description: String,
    args: List<Arg<*>>,
    content:
        @androidx.compose.runtime.Composable
        @ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
        (Args) -> Unit,
) = Story(id, name, function, description, args, WATCH, WATCH, round = true, content = content)

private fun wear(
    id: String,
    title: String,
    symbols: List<String>,
    description: String,
    stories: List<Story>,
) = Component(id, title, CATEGORY, symbols.map { "$W.$it" }, description, stories)

internal val wearComponents =
    listOf(
        wear(
            "wear-button",
            "Button",
            listOf("RemoteButton", "RemoteButtonDefaults"),
            "The main Wear OS button: a full-width pill with a label, an optional secondary label and " +
                "icon, in five color sets from Filled to Child.",
            listOf(
                watchStory(
                    "wear-button--variants",
                    "Variants",
                    "WearButton",
                    "Every color set of RemoteButtonDefaults, with or without an icon and a secondary label.",
                    listOf(
                        text("label", "Start run"),
                        text("secondaryLabel", "5 km"),
                        choice("style", buttonStyles),
                        bool("withIcon", true),
                        bool("enabled", true),
                    ),
                ) {
                    WearButton(
                        it.text("label"),
                        it.text("secondaryLabel"),
                        it["style"],
                        it.bool("withIcon"),
                        it.bool("enabled"),
                    )
                },
            ),
        ),
        wear(
            "wear-compact-button",
            "Compact button",
            listOf("RemoteCompactButton"),
            "A short, low button for secondary actions, with an icon, a label or both.",
            listOf(
                watchStory(
                    "wear-compact-button--default",
                    "Compact button",
                    "WearCompactButton",
                    "An icon and a label in the compact size, in each color set.",
                    listOf(text("label", "Like"), choice("style", buttonStyles, default = 2)),
                ) {
                    WearCompactButton(it.text("label"), it["style"])
                },
            ),
        ),
        wear(
            "wear-edge-button",
            "Edge button",
            listOf("RemoteEdgeButton", "RemoteEdgeButtonSize", "RemoteEdgeButtonDefaults"),
            "The button that hugs the bottom edge of a round screen, in four heights, for the screen's " +
                "main action.",
            listOf(
                watchStory(
                    "wear-edge-button--sizes",
                    "Sizes",
                    "WearEdgeButton",
                    "Tap it: the count above is document state, updated by the player.",
                    listOf(
                        text("label", "Tap"),
                        choice("size", edgeButtonSizes, default = 2),
                        bool("filledTonal", false),
                    ),
                ) {
                    WearEdgeButton(it.text("label"), it["size"], it.bool("filledTonal"))
                },
            ),
        ),
        wear(
            "wear-icon-button",
            "Icon button",
            listOf("RemoteIconButton", "RemoteIconButtonDefaults"),
            "A round button with only an icon: filled, filled tonal and outlined.",
            listOf(
                watchStory(
                    "wear-icon-button--variants",
                    "Variants",
                    "WearIconButtons",
                    "Filled, filled tonal and outlined icon buttons side by side.",
                    listOf(bool("enabled", true)),
                ) {
                    WearIconButtons(it.bool("enabled"))
                },
            ),
        ),
        wear(
            "wear-text-button",
            "Text button",
            listOf("RemoteTextButton", "RemoteTextButtonDefaults"),
            "A round button with a short text, such as a number or two letters.",
            listOf(
                watchStory(
                    "wear-text-button--variants",
                    "Variants",
                    "WearTextButtons",
                    "A filled tonal and a plain text button.",
                    listOf(text("text", "ABC"), bool("enabled", true)),
                ) {
                    WearTextButtons(it.text("text"), it.bool("enabled"))
                },
            ),
        ),
        wear(
            "wear-toggle-buttons",
            "Toggle buttons",
            listOf(
                "RemoteIconToggleButton",
                "RemoteIconToggleButtonDefaults",
                "RemoteTextToggleButton",
            ),
            "Round buttons that stay on or off: an icon toggle and a text toggle. Their state lives in " +
                "the document.",
            listOf(
                watchStory(
                    "wear-toggle-buttons--default",
                    "Icon and text",
                    "WearToggleButtons",
                    "Tap either button to toggle it; the variant shapes change the checked shape.",
                    listOf(bool("initiallyChecked", true), bool("variantShapes", false)),
                ) {
                    WearToggleButtons(it.bool("initiallyChecked"), it.bool("variantShapes"))
                },
            ),
        ),
        wear(
            "wear-button-group",
            "Button group",
            listOf("RemoteButtonGroup", "RemoteButtonGroupDefaults"),
            "A row of buttons that share the width, with Wear's standard spacing.",
            listOf(
                watchStory(
                    "wear-button-group--default",
                    "Button group",
                    "WearButtonGroup",
                    "Two or three icon buttons that share the row by weight.",
                    listOf(int("buttons", 3, 2, 3)),
                ) {
                    WearButtonGroup(it.int("buttons"))
                },
            ),
        ),
        wear(
            "wear-checkbox-button",
            "Checkbox button",
            listOf(
                "RemoteCheckboxButton",
                "RemoteCheckboxButtonDefaults",
                "RemoteSplitCheckboxButton",
            ),
            "A full-width button with a checkbox, and the split version whose label opens details " +
                "while the box toggles.",
            listOf(
                watchStory(
                    "wear-checkbox-button--default",
                    "Checkbox and split",
                    "WearCheckboxButtons",
                    "Tap to toggle; the split button's label sends a host action instead.",
                    listOf(
                        text("label", "Wi-Fi"),
                        bool("initiallyChecked", true),
                        bool("enabled", true),
                    ),
                ) {
                    WearCheckboxButtons(
                        it.text("label"),
                        it.bool("initiallyChecked"),
                        it.bool("enabled"),
                    )
                },
            ),
        ),
        wear(
            "wear-switch-button",
            "Switch button",
            listOf("RemoteSwitchButton", "RemoteSwitchButtonDefaults", "RemoteSplitSwitchButton"),
            "A full-width button with a switch, and the split version.",
            listOf(
                watchStory(
                    "wear-switch-button--default",
                    "Switch and split",
                    "WearSwitchButtons",
                    "Tap to switch; the split button's label sends a host action instead.",
                    listOf(
                        text("label", "Bluetooth"),
                        bool("initiallyChecked", true),
                        bool("enabled", true),
                    ),
                ) {
                    WearSwitchButtons(
                        it.text("label"),
                        it.bool("initiallyChecked"),
                        it.bool("enabled"),
                    )
                },
            ),
        ),
        wear(
            "wear-radio-button",
            "Radio button",
            listOf("RemoteRadioButton", "RemoteRadioButtonDefaults", "RemoteSplitRadioButton"),
            "Full-width buttons for picking one option, plain and split.",
            listOf(
                watchStory(
                    "wear-radio-button--default",
                    "Radio and split",
                    "WearRadioButtons",
                    "One selected index in the document: selecting one option deselects the other.",
                    listOf(
                        text("first", "Metric"),
                        text("second", "Imperial"),
                        bool("enabled", true),
                    ),
                ) {
                    WearRadioButtons(it.text("first"), it.text("second"), it.bool("enabled"))
                },
            ),
        ),
        wear(
            "wear-card",
            "Card",
            listOf("RemoteCard", "RemoteOutlinedCard", "RemoteCardDefaults"),
            "A tappable surface for a block of content, filled or outlined.",
            listOf(
                watchStory(
                    "wear-card--variants",
                    "Filled and outlined",
                    "WearCard",
                    "A card and an outlined card with the same content.",
                    listOf(text("text", "Next: Team sync at 10:30"), bool("outlined", false)),
                ) {
                    WearCard(it.text("text"), it.bool("outlined"))
                },
            ),
        ),
        wear(
            "wear-app-card",
            "App card",
            listOf("RemoteAppCard"),
            "A card for a notification-like item: the app's name, a time, a title and a body.",
            listOf(
                watchStory(
                    "wear-app-card--default",
                    "App card",
                    "WearAppCard",
                    "App name, time, title and body in Wear's card layout.",
                    listOf(
                        text("appName", "Messages"),
                        text("title", "Anna"),
                        text("body", "On my way!"),
                        text("time", "now"),
                    ),
                ) {
                    WearAppCard(
                        it.text("appName"),
                        it.text("title"),
                        it.text("body"),
                        it.text("time"),
                    )
                },
            ),
        ),
        wear(
            "wear-title-card",
            "Title card",
            listOf("RemoteTitleCard"),
            "A card led by its title, with a time and a subtitle.",
            listOf(
                watchStory(
                    "wear-title-card--default",
                    "Title card",
                    "WearTitleCard",
                    "Title, time and subtitle.",
                    listOf(
                        text("title", "Flight LH 123"),
                        text("subtitle", "Gate B12"),
                        text("time", "14:05"),
                    ),
                ) {
                    WearTitleCard(it.text("title"), it.text("subtitle"), it.text("time"))
                },
            ),
        ),
        wear(
            "wear-text",
            "Text",
            listOf("RemoteText", "RemoteMaterialTheme"),
            "Text in Wear's type scale and colors, taken from RemoteMaterialTheme.",
            listOf(
                watchStory(
                    "wear-text--default",
                    "Text",
                    "WearText",
                    "Title medium, centred, ellipsized after maxLines.",
                    listOf(
                        text("text", "Wear Material 3 on Remote Compose"),
                        int("maxLines", 2, 1, 4),
                        bool("primaryColor", true),
                    ),
                ) {
                    WearText(it.text("text"), it.int("maxLines"), it.bool("primaryColor"))
                },
            ),
        ),
        wear(
            "wear-icon",
            "Icon",
            listOf("RemoteIcon"),
            "A vector icon tinted with the theme's content color.",
            listOf(
                watchStory(
                    "wear-icon--default",
                    "Icon",
                    "WearIcon",
                    "A vector icon at a size and tint.",
                    listOf(int("size", 48, 16, 96, step = 4), bool("primaryTint", true)),
                ) {
                    WearIcon(it.int("size"), it.bool("primaryTint"))
                },
            ),
        ),
        wear(
            "wear-circular-progress",
            "Circular progress",
            listOf("RemoteCircularProgressIndicator", "RemoteProgressIndicatorDefaults"),
            "A ring that fills to show progress, or spins while the amount is unknown.",
            listOf(
                watchStory(
                    "wear-circular-progress--default",
                    "Circular progress",
                    "WearCircularProgress",
                    "Determinate progress, or the indeterminate spinner.",
                    listOf(
                        float("progress", 0.6f, 0f, 1f, step = 0.05f),
                        bool("enabled", true),
                        bool("indeterminate", false),
                    ),
                ) {
                    WearCircularProgress(
                        it.float("progress"),
                        it.bool("enabled"),
                        it.bool("indeterminate"),
                    )
                },
            ),
        ),
        wear(
            "wear-linear-progress",
            "Linear progress",
            listOf("RemoteLinearProgressIndicator", "RemoteLinearProgressIndicatorDefaults"),
            "A horizontal bar that fills to show progress.",
            listOf(
                watchStory(
                    "wear-linear-progress--default",
                    "Linear progress",
                    "WearLinearProgress",
                    "A progress bar across the screen.",
                    listOf(float("progress", 0.4f, 0f, 1f, step = 0.05f), bool("enabled", true)),
                ) {
                    WearLinearProgress(it.float("progress"), it.bool("enabled"))
                },
            ),
        ),
        wear(
            "wear-curved-progress",
            "Curved progress",
            listOf("RemoteCurvedProgressIndicator", "RemoteIntroOutroCurvedProgressIndicator"),
            "Progress drawn along the edge of the round screen, around the content.",
            listOf(
                watchStory(
                    "wear-curved-progress--default",
                    "Curved progress",
                    "WearCurvedProgress",
                    "The edge indicator (from 135°, a 90° sweep by default) and the intro/outro variant " +
                        "that animates in and out.",
                    listOf(
                        float("progress", 0.7f, 0f, 1f, step = 0.05f),
                        int("sweepAngle", 90, 30, 360, step = 10),
                        bool("introOutro", false),
                    ),
                ) {
                    WearCurvedProgress(
                        it.float("progress"),
                        it.int("sweepAngle"),
                        it.bool("introOutro"),
                    )
                },
            ),
        ),
        wear(
            "wear-page-indicator",
            "Page indicator",
            listOf(
                "RemoteHorizontalPageIndicator",
                "RemoteVerticalPageIndicator",
                "rememberRemotePageIndicatorState",
            ),
            "Dots that show which page of a pager is on screen, along the bottom or the side.",
            listOf(
                watchStory(
                    "wear-page-indicator--default",
                    "Page indicator",
                    "WearPageIndicator",
                    "Horizontal or vertical dots for a page count and a selected page.",
                    listOf(
                        int("pageCount", 4, 2, 6),
                        int("selectedPage", 1, 0, 5),
                        bool("vertical", false),
                    ),
                ) {
                    WearPageIndicator(
                        it.int("pageCount"),
                        it.int("selectedPage"),
                        it.bool("vertical"),
                    )
                },
            ),
        ),
        wear(
            "wear-theme",
            "Theme",
            listOf("RemoteMaterialTheme", "RemoteColorScheme"),
            "The Wear Material 3 theme: colors, type and shapes every component reads. Override a few " +
                "colors and every component below follows.",
            listOf(
                watchStory(
                    "wear-theme--colors",
                    "Custom colors",
                    "WearTheme",
                    "A copy of the default color scheme with another primary color.",
                    listOf(
                        color("primary", Color(0xFF80CBC4)),
                        color("onPrimary", Color(0xFF00332C)),
                    ),
                ) {
                    WearTheme(it.color("primary"), it.color("onPrimary"))
                },
            ),
        ),
    )
