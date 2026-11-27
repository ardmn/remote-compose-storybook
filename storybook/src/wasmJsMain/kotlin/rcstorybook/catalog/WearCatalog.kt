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

/** A pick of [roles] for one color slot, as `WearColorRole.X` in the Code tab. */
private fun role(
    name: String,
    default: WearColorRole,
    roles: List<WearColorRole> = WearColorRole.entries,
    doc: String = "",
) =
    choice(
        name,
        roles.map { option(it.name, "WearColorRole.${it.name}", it) },
        default = roles.indexOf(default),
        doc = doc,
    )

/** The five roles the Text and Icon stories offer: distinct on Wear's black background. */
private val textRoles =
    listOf(
        WearColorRole.Primary,
        WearColorRole.Secondary,
        WearColorRole.Tertiary,
        WearColorRole.OnSurfaceVariant,
        WearColorRole.Error,
    )

private fun shape(name: String = "shape") =
    choice(
        name,
        WearShapeSize.entries.map { option(it.name, "WearShapeSize.${it.name}", it) },
        default = WearShapeSize.entries.indexOf(WearShapeSize.Large),
        doc = "A RemoteShapes size; cards default to large.",
    )

/** What the filled and outlined card stories show. */
private const val CARD_TEXT = "Next: Team sync at 10:30"

/** RemoteCurvedProgressIndicator's default sweep, in degrees. */
private const val DEFAULT_SWEEP = 90

private const val MIN_SWEEP = 30

private const val FULL_CIRCLE = 360

/** The curved indicators' sweep, in degrees: AndroidX's default, up to a full circle. */
private fun sweepAngle() = int("sweepAngle", DEFAULT_SWEEP, MIN_SWEEP, FULL_CIRCLE, step = 10)

/** RemoteCardDefaults.ContentPadding, in dp. */
private const val CARD_PADDING = 12

/** The largest card padding the control offers, in dp. */
private const val MAX_CARD_PADDING = 24

private fun contentPadding() =
    int(
        "contentPadding",
        CARD_PADDING,
        0,
        MAX_CARD_PADDING,
        doc = "All sides, in dp; RemoteCardDefaults.ContentPadding is $CARD_PADDING.",
    )

/** The press-feedback limitation in full: the Button story's description ends with it. */
private const val NO_PRESS_FEEDBACK =
    " Known limitation: a press shows no feedback. remote-material3 has no pressed state, and the " +
        "Compose players (this one included) draw a ripple only where the document asks for one; " +
        "AndroidX's View player draws a temporary ripple on any click (b/585818680)."

/** The same limitation in short: every other tappable Wear story's description ends with it. */
private const val NO_PRESS_FEEDBACK_SHORT =
    " Known limitation: no press feedback (remote-material3 has no pressed state; see Button)."

/** RemoteCardDefaults.OutlinedBorderSize, in dp. */
private const val OUTLINED_BORDER = 1

/** The widest border the outlined card's control offers, in dp. */
private const val MAX_BORDER = 4

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
                    "Button variants",
                    "WearButton",
                    "Every color set of RemoteButtonDefaults, with or without an icon and a secondary " +
                        "label." +
                        NO_PRESS_FEEDBACK,
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
                    "An icon and a label in the compact size, in each color set." +
                        NO_PRESS_FEEDBACK_SHORT,
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
                    "Tap it: the count above is document state, updated by the player. The story adds " +
                        "RemoteEdgeButtonDefaults.VerticalPadding below the button, as Wear Material " +
                        "3's EdgeButton does; RemoteEdgeButton itself omits it and touches the screen's edge." +
                        NO_PRESS_FEEDBACK_SHORT,
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
                    "Icon button variants",
                    "WearIconButtons",
                    "Filled, filled tonal and outlined icon buttons side by side." +
                        NO_PRESS_FEEDBACK_SHORT,
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
                    "Text button variants",
                    "WearTextButtons",
                    "A filled tonal and a plain text button." + NO_PRESS_FEEDBACK_SHORT,
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
                    "Tap either button to toggle it; the variant shapes change the checked shape." +
                        NO_PRESS_FEEDBACK_SHORT,
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
            "A row of buttons that share the width, with Wear's standard spacing. A pressed button " +
                "grows and its neighbours shrink.",
            listOf(
                watchStory(
                    "wear-button-group--default",
                    "Button group",
                    "WearButtonGroup",
                    "Two or three icon buttons that share the row by weight (the middle of three is wider). " +
                        "Press and hold one: animateWidth widens it with a spring and squeezes the others, " +
                        "then lets go when you release." +
                        NO_PRESS_FEEDBACK_SHORT,
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
                    "Tap to toggle; the split button's label sends a host action instead." +
                        NO_PRESS_FEEDBACK_SHORT,
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
                    "Tap to switch; the split button's label sends a host action instead." +
                        NO_PRESS_FEEDBACK_SHORT,
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
                    "One selected index in the document: selecting one option deselects the other." +
                        NO_PRESS_FEEDBACK_SHORT,
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
            listOf("RemoteCard", "RemoteOutlinedCard", "RemoteCardDefaults", "RemoteCardColors"),
            "A tappable surface for a block of content: filled, outlined, or over an image.",
            listOf(
                watchStory(
                    "wear-card--variants",
                    "Filled",
                    "WearCard",
                    "RemoteCard with its shape, padding and the two colors it draws (container and " +
                        "content). Turning enabled off only stops the tap: cards have no disabled colors." +
                        NO_PRESS_FEEDBACK_SHORT,
                    listOf(
                        text("text", CARD_TEXT),
                        bool("enabled", true),
                        shape(),
                        contentPadding(),
                        role("containerColor", WearColorRole.SurfaceContainer),
                        role("contentColor", WearColorRole.OnSurfaceVariant),
                    ),
                ) {
                    WearCard(
                        it.text("text"),
                        it.bool("enabled"),
                        it["shape"],
                        it.int("contentPadding"),
                        it["containerColor"],
                        it["contentColor"],
                    )
                },
                watchStory(
                    "wear-card--outlined",
                    "Outlined",
                    "WearOutlinedCard",
                    "RemoteOutlinedCard: a transparent container inside a border, whose color and width " +
                        "come from outlinedCardBorder." +
                        NO_PRESS_FEEDBACK_SHORT,
                    listOf(
                        text("text", CARD_TEXT),
                        bool("enabled", true),
                        shape(),
                        contentPadding(),
                        role("contentColor", WearColorRole.OnSurfaceVariant),
                        role("borderColor", WearColorRole.Outline),
                        int(
                            "borderWidth",
                            OUTLINED_BORDER,
                            1,
                            MAX_BORDER,
                            doc =
                                "In dp; RemoteCardDefaults.OutlinedBorderSize is $OUTLINED_BORDER.",
                        ),
                    ),
                ) {
                    WearOutlinedCard(
                        it.text("text"),
                        it.bool("enabled"),
                        it["shape"],
                        it.int("contentPadding"),
                        it["contentColor"],
                        it["borderColor"],
                        it.int("borderWidth"),
                    )
                },
                watchStory(
                    "wear-card--image",
                    "Image background",
                    "WearImageCard",
                    "A title card over an image: RemoteCardDefaults.containerPainter draws the bitmap " +
                        "under the content, with a scrim so the text stays readable." +
                        NO_PRESS_FEEDBACK_SHORT,
                    listOf(
                        text("title", "Sunset walk"),
                        text("subtitle", "18:42"),
                        float("imageAlpha", 1f, 0f, 1f, step = 0.05f),
                        bool("scrim", true),
                    ),
                ) {
                    WearImageCard(
                        it.text("title"),
                        it.text("subtitle"),
                        it.float("imageAlpha"),
                        it.bool("scrim"),
                    )
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
                    "App image, app name, time, title and content in Wear's card layout. Empty time or " +
                        "content leaves the slot out. The colors are the five RemoteCardColors an app " +
                        "card draws (it has no subtitle)." +
                        NO_PRESS_FEEDBACK_SHORT,
                    listOf(
                        text("appName", "Messages"),
                        text("title", "Anna"),
                        text("time", "now"),
                        text("content", "On my way!"),
                        bool("appImage", true),
                        bool("enabled", true),
                        shape(),
                        contentPadding(),
                        role("containerColor", WearColorRole.SurfaceContainer),
                        role("contentColor", WearColorRole.OnSurfaceVariant),
                        role("appNameColor", WearColorRole.OnSurface),
                        role("timeColor", WearColorRole.OnSurfaceVariant),
                        role("titleColor", WearColorRole.OnSurface),
                    ),
                ) {
                    WearAppCard(
                        it.text("appName"),
                        it.text("title"),
                        it.text("time"),
                        it.text("content"),
                        it.bool("appImage"),
                        it.bool("enabled"),
                        it["shape"],
                        it.int("contentPadding"),
                        it["containerColor"],
                        it["contentColor"],
                        it["appNameColor"],
                        it["timeColor"],
                        it["titleColor"],
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
                    "Title, time, subtitle and content; an empty time, subtitle or content leaves the " +
                        "slot out. The colors are the five RemoteCardColors a title card draws (it has no " +
                        "app name)." +
                        NO_PRESS_FEEDBACK_SHORT,
                    listOf(
                        text("title", "Flight LH 123"),
                        text("time", "14:05"),
                        text("subtitle", "Gate B12"),
                        text("content", "Boarding in 20 min"),
                        bool("enabled", true),
                        shape(),
                        contentPadding(),
                        role("containerColor", WearColorRole.SurfaceContainer),
                        role("contentColor", WearColorRole.OnSurfaceVariant),
                        role("timeColor", WearColorRole.OnSurfaceVariant),
                        role("titleColor", WearColorRole.OnSurface),
                        role("subtitleColor", WearColorRole.Tertiary),
                    ),
                ) {
                    WearTitleCard(
                        it.text("title"),
                        it.text("time"),
                        it.text("subtitle"),
                        it.text("content"),
                        it.bool("enabled"),
                        it["shape"],
                        it.int("contentPadding"),
                        it["containerColor"],
                        it["contentColor"],
                        it["timeColor"],
                        it["titleColor"],
                        it["subtitleColor"],
                    )
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
                        role("color", WearColorRole.Primary, textRoles),
                    ),
                ) {
                    WearText(it.text("text"), it.int("maxLines"), it["color"])
                },
            ),
        ),
        wear(
            "wear-icon",
            "Icon",
            listOf("RemoteIcon"),
            "A vector icon tinted with a color role from the theme.",
            listOf(
                watchStory(
                    "wear-icon--default",
                    "Icon",
                    "WearIcon",
                    "A vector icon at a size and tint.",
                    listOf(
                        int("size", 48, 16, 96, step = 4),
                        role("tint", WearColorRole.Primary, textRoles),
                    ),
                ) {
                    WearIcon(it.int("size"), it["tint"])
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
                    "Determinate progress, or the indeterminate spinner. At 100% the ring closes " +
                        "into a full circle.",
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
                    "The edge indicator, from 135° with a 90° sweep by default. Known limitation: at " +
                        "100% with a 360° sweep the ring does not close; its rounded ends meet at a " +
                        "pinch near startAngle. This is a bug in RemoteCurvedProgressIndicator, not the " +
                        "intended look. Circular progress had the same bug, and remote-material3 " +
                        "1.0.0-alpha16 fixes it there; a fix for the curved indicator is not in " +
                        "AndroidX yet.",
                    listOf(
                        float("progress", 0.7f, 0f, 1f, step = 0.05f),
                        sweepAngle(),
                    ),
                ) {
                    WearCurvedProgress(it.float("progress"), it.int("sweepAngle"))
                },
                watchStory(
                    "wear-curved-progress--intro-outro",
                    "Intro and outro",
                    "WearIntroOutroProgress",
                    "Tap the screen to step the progress 0 → 50% → 100% → 0. The intro/outro indicator " +
                        "is made for timers and goals (steps, a workout): when progress leaves 0, the " +
                        "dot at the start grows in and pushes the track ahead of it; when progress " +
                        "reaches 100%, the track's tail collapses and the arc runs to the end in about " +
                        "0.85 s. The springs only play when the progress changes inside the document, " +
                        "so the story keeps it in a MutableRemoteFloat and steps it on tap." +
                        NO_PRESS_FEEDBACK_SHORT,
                    listOf(sweepAngle()),
                ) {
                    WearIntroOutroProgress(it.int("sweepAngle"))
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
                        int(
                            "selectedPage",
                            1,
                            0,
                            5,
                            doc = "0-based; the story clamps it to pageCount - 1.",
                        ),
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
            listOf(
                "RemoteMaterialTheme",
                "RemoteColorScheme",
                "RemoteShapes",
                "RemoteMotionScheme",
            ),
            "The Wear Material 3 theme: colors, type, shapes and motion every Wear component reads. " +
                "Override a few and every component inside the theme follows.",
            listOf(
                watchStory(
                    "wear-theme--colors",
                    "Colors, shapes and motion",
                    "WearTheme",
                    "A copy of the default color scheme with the main roles replaced, a shape set and a " +
                        "motion scheme, shown on a text, a title card, a progress bar, a warning and a " +
                        "button. The motion scheme sets the springs of animated components (a button " +
                        "group's press, for one), so it shows no change on a still screen." +
                        NO_PRESS_FEEDBACK_SHORT,
                    listOf(
                        color("primary", Color(0xFF80CBC4)),
                        color("onPrimary", Color(0xFF00332C)),
                        color("secondary", Color(0xFFFFCC80)),
                        color("tertiary", Color(0xFFF48FB1)),
                        color("surfaceContainer", Color(0xFF263238)),
                        color("onSurface", Color(0xFFECEFF1)),
                        color("onSurfaceVariant", Color(0xFFB0BEC5)),
                        color("error", Color(0xFFFF8A80)),
                        choice(
                            "motion",
                            WearMotion.entries.map { option(it.name, "WearMotion.${it.name}", it) },
                        ),
                        choice(
                            "shapes",
                            WearShapeSet.entries.map {
                                option(it.name, "WearShapeSet.${it.name}", it)
                            },
                        ),
                    ),
                ) {
                    WearTheme(
                        it.color("primary"),
                        it.color("onPrimary"),
                        it.color("secondary"),
                        it.color("tertiary"),
                        it.color("surfaceContainer"),
                        it.color("onSurface"),
                        it.color("onSurfaceVariant"),
                        it.color("error"),
                        it["motion"],
                        it["shapes"],
                    )
                },
            ),
        ),
    )
