package rcstorybook.catalog

/**
 * What each component is for, in words a designer or a product manager can use: what it does on
 * screen and when you would reach for it. The developer description stays on the component.
 */
internal class Plain(val what: String, val useWhen: List<String>)

internal val plainLanguage: Map<String, Plain> =
    mapOf(
        "remote-box" to
            Plain(
                "A container that stacks things on top of each other, like layers in a design tool, and pins them to a corner, an edge or the centre.",
                listOf(
                    "A badge over an avatar",
                    "Text centred on a coloured card",
                    "A background shape behind content",
                ),
            ),
        "remote-row" to
            Plain(
                "Places items side by side, left to right, and decides how leftover horizontal space is shared between them.",
                listOf(
                    "An icon next to a label",
                    "A toolbar of buttons",
                    "Columns that share the width in a ratio",
                ),
            ),
        "remote-column" to
            Plain(
                "Stacks items top to bottom and decides how leftover vertical space is shared between them.",
                listOf(
                    "A card with a title, body and footer",
                    "A list of settings",
                    "A header and footer with content filling the middle",
                ),
            ),
        "remote-arrangement" to
            Plain(
                "A fixed gap between every item in a row or a column, like auto-layout spacing in Figma.",
                listOf("Consistent spacing in lists and toolbars"),
            ),
        "remote-spacer" to
            Plain(
                "An invisible gap. A fixed spacer adds a set distance; a flexible one pushes the next item to the far end.",
                listOf(
                    "Pushing a button to the right edge",
                    "Adding breathing room between two blocks",
                ),
            ),
        "remote-collapsible" to
            Plain(
                "A row or column that hides its least important items when space runs out, instead of squashing or cutting them.",
                listOf(
                    "Widgets that must work at several sizes",
                    "Toolbars that drop secondary actions on small screens",
                ),
            ),
        "remote-fit-box" to
            Plain(
                "Holds several versions of the same content, large to small, and shows the biggest one that fits. The device decides, not the server.",
                listOf(
                    "A widget with large, medium and small layouts",
                    "Responsive cards without separate designs per device",
                ),
            ),
        "remote-text" to
            Plain(
                "Shows text with a chosen size, colour, weight, font, alignment and line limit, ending in “…” when it does not fit.",
                listOf(
                    "Titles, labels and paragraphs",
                    "Prices and status text that the server updates",
                ),
            ),
        "remote-basic-text" to
            Plain(
                "The simplest piece of text, with no built-in styling: the building block that richer text is made from.",
                listOf("Fully custom text styles"),
            ),
        "remote-image" to
            Plain(
                "Shows a picture, embedded in the screen or loaded from a web address, cropped or fitted into its box.",
                listOf(
                    "Product photos and banners",
                    "Avatars and thumbnails",
                    "Images the server can change without an app update",
                ),
            ),
        "remote-basic-image" to
            Plain(
                "The simplest picture element, without extra behaviour.",
                listOf("Decorative images"),
            ),
        "remote-basic-icon" to
            Plain(
                "A small vector icon that stays sharp at any size and can be recoloured.",
                listOf("Action icons", "Status symbols that change colour"),
            ),
        "remote-canvas" to
            Plain(
                "A free drawing surface: shapes, lines, text and transformations drawn exactly where you place them, including animated drawings.",
                listOf(
                    "Charts, gauges and progress rings",
                    "Custom illustrations",
                    "Watch-face style animated graphics",
                ),
            ),
        "remote-brush" to
            Plain(
                "Colour gradients — straight, circular or sweeping — used to fill shapes.",
                listOf("Gradient backgrounds and buttons", "Colourful progress indicators"),
            ),
        "remote-outline" to
            Plain(
                "Turns a shape such as a rounded rectangle into something that can be filled or used as a mask while drawing.",
                listOf("Custom cards and frames drawn on a canvas"),
            ),
        "draw-with-content" to
            Plain(
                "Draws extra decoration behind or on top of any component, without changing the component itself.",
                listOf("Highlight rings and glows", "Overlays such as badges or scrims"),
            ),
        "remote-component" to
            Plain(
                "Lets an element know its parent's real size and its own position on the device, so it can size itself as a fraction of it or react to where it ended up.",
                listOf(
                    "Bars and fills that are a percentage of their container",
                    "Decorations that line up with where an element was placed",
                ),
            ),
        "remote-text-measurer" to
            Plain(
                "Measures text on the device, with the device's own fonts, so a drawing can fit around it: how wide and tall a line is, where its baseline is and which pixels the letters cover.",
                listOf(
                    "Badges and pills that fit their label",
                    "Labels and underlines drawn on charts",
                    "Counters that keep the same width while their digits change",
                ),
            ),
        "size-modifiers" to
            Plain(
                "Controls how big something is: an exact size, a share of the available space, a minimum or maximum, or just as big as its content.",
                listOf(
                    "Fixed-size buttons",
                    "Full-width banners",
                    "Text boxes with a maximum width",
                ),
            ),
        "padding-offset" to
            Plain(
                "Inner spacing around content, nudging an element away from its position, and choosing which of two overlapping elements is on top.",
                listOf(
                    "Comfortable tap areas",
                    "Overlapping avatars",
                    "Bringing a selected card to the front",
                ),
            ),
        "background-border-clip" to
            Plain(
                "Fills an element with colour or an image, gives it an outline, and rounds or reshapes its corners.",
                listOf("Cards and chips", "Circular avatars", "Pill-shaped buttons"),
            ),
        "transform-modifiers" to
            Plain(
                "Fades, rotates, scales, tilts in 3D or drops a shadow under an element, without moving anything around it.",
                listOf(
                    "Disabled states (faded)",
                    "Tilted cards and flips",
                    "Emphasis through scale",
                ),
            ),
        "visibility" to
            Plain(
                "Shows or hides an element, either keeping its space or letting the layout close the gap. The device can switch it without a new screen from the server.",
                listOf("Expandable sections", "Optional badges"),
            ),
        "scroll" to
            Plain(
                "Lets content that is longer or wider than its box scroll, with optional snapping, handled entirely on the device.",
                listOf("Lists and feeds", "Horizontal carousels"),
            ),
        "align-by-baseline" to
            Plain(
                "Lines up texts of different sizes on the same baseline, the way typographers align them.",
                listOf("A large price next to a small currency label"),
            ),
        "clickable" to
            Plain(
                "Makes something tappable and changes what is shown immediately on the device — toggles, counters, selections — with no trip to the server.",
                listOf("Switches and checkboxes", "Steppers and counters", "Tabs and selections"),
            ),
        "host-action" to
            Plain(
                "A tap that tells the app around the screen to do something, such as open a page or save a bookmark, optionally while also updating the screen.",
                listOf("Navigation to app screens", "Like, bookmark and share buttons"),
            ),
        "touch" to
            Plain(
                "Reacts to the moment a finger goes down, lifts, or slides away, for press-and-hold feedback.",
                listOf("Pressed states", "Hold-to-confirm buttons"),
            ),
        "semantics" to
            Plain(
                "Describes elements to screen readers and other assistive technology: what they are, what state they are in, what they do.",
                listOf("Making every screen accessible", "Labelling icon-only buttons"),
            ),
        "animate-value" to
            Plain(
                "Smoothly animates a value such as size or transparency when it changes, with a chosen speed and feel (ease, bounce, spring).",
                listOf("Expanding cards", "Fading content in and out", "Playful bouncy feedback"),
            ),
        "enter-exit" to
            Plain(
                "Animations that play when an element appears or disappears: fade, slide in from an edge, rotate or dissolve into particles.",
                listOf("Showing and hiding panels", "Toasts and banners"),
            ),
        "shared-element" to
            Plain(
                "Moves the same element smoothly from one layout to another, so users can follow it between two states of the screen.",
                listOf("A thumbnail growing into a detail view", "Expanding a compact card"),
            ),
        "remote-state-layout" to
            Plain(
                "Holds several versions of a screen — for example loading, content and error — and shows the right one for the current state, switching on the device.",
                listOf(
                    "Loading / success / error states",
                    "Step-by-step flows",
                    "Collapsed and expanded views",
                ),
            ),
        "named-state" to
            Plain(
                "Values with a name, such as a title or a colour, that the app showing the screen can change later without the server sending a new design.",
                listOf(
                    "Live scores and prices",
                    "Personalised greetings",
                    "Theme colours chosen by the app",
                ),
            ),
        "expressions" to
            Plain(
                "Calculations that run on the device, every frame if needed, so things can move or resize from math or time without new data.",
                listOf("Animated charts and waves", "Sizes computed from other sizes"),
            ),
        "color-expressions" to
            Plain(
                "Colours computed on the device: blends between two colours, rotating hues or changing transparency.",
                listOf("Status colours that shift smoothly", "Ambient animated backgrounds"),
            ),
        "time-config" to
            Plain(
                "Things only the device knows: the current time and date, and the screen's size, density and text size.",
                listOf(
                    "Clocks and countdowns",
                    "Layouts that adapt to the screen",
                    "Respecting the user's font size",
                ),
            ),
        "recipe-product-card" to
            Plain(
                "A complete shop card built from the blocks on this site, with a working cart button.",
                listOf("E-commerce promos the server can change daily", "Personalised offers"),
            ),
        "recipe-weather-widget" to
            Plain(
                "A widget that rearranges itself for its size and shows live data the app supplies.",
                listOf(
                    "Home-screen and glanceable widgets",
                    "Live scores, prices, delivery status",
                ),
            ),
        "recipe-onboarding" to
            Plain(
                "A multi-step flow whose steps all arrive at once and run on the device.",
                listOf("Onboarding and tutorials", "Surveys and step-by-step forms"),
            ),
        "recipe-media-player" to
            Plain(
                "Playback controls with a moving progress bar, all animated on the device.",
                listOf("Media and podcast controls", "Timers and progress displays"),
            ),
        "recipe-settings" to
            Plain(
                "A list of switches the user flips on the device and values the app keeps up to date.",
                listOf("Settings and preferences", "Feature toggles shown in a remote screen"),
            ),
        "wear-button" to
            Plain(
                "The main button on a Wear OS watch: a wide pill with a label, an optional icon and a second line, in five levels of emphasis.",
                listOf("Start a workout", "Reply to a message", "Confirm an action"),
            ),
        "wear-compact-button" to
            Plain(
                "A short, low button for less important actions that should not take a whole row.",
                listOf("Secondary actions under a card", "Like or share"),
            ),
        "wear-edge-button" to
            Plain(
                "The button that sits along the bottom curve of a round watch screen for the screen's main action.",
                listOf("The main action of a screen", "Done, Start or Open on phone"),
            ),
        "wear-icon-button" to
            Plain(
                "A round button with only an icon, for actions everyone recognises at a glance.",
                listOf("Play, pause, call", "Rows of quick actions"),
            ),
        "wear-text-button" to
            Plain(
                "A round button with a few characters of text instead of an icon.",
                listOf("Number pads", "Short options like 5, 10, 15 min"),
            ),
        "wear-toggle-buttons" to
            Plain(
                "Round buttons that stay on or off, with an icon or a short text.",
                listOf("Mute, flashlight, do not disturb", "Formatting options"),
            ),
        "wear-button-group" to
            Plain(
                "A row of buttons that share the width of the screen evenly.",
                listOf("Media controls", "Two or three related quick actions"),
            ),
        "wear-checkbox-button" to
            Plain(
                "A wide button with a checkbox; the split version lets the label open details while the box toggles.",
                listOf("Choosing several items", "Settings with a details screen"),
            ),
        "wear-switch-button" to
            Plain(
                "A wide button with an on/off switch, plain or split.",
                listOf("Wi-Fi, Bluetooth, notifications", "Feature on/off settings"),
            ),
        "wear-radio-button" to
            Plain(
                "Wide buttons for picking exactly one option from a list.",
                listOf("Units and formats", "Choosing one of a few plans"),
            ),
        "wear-card" to
            Plain(
                "A tappable surface that groups a piece of content, filled or outlined.",
                listOf("A reminder or next event", "A summary that opens details"),
            ),
        "wear-app-card" to
            Plain(
                "A card for a notification-style item: the app's name, a time, a title and a body.",
                listOf("Messages and notifications", "Updates from an app"),
            ),
        "wear-title-card" to
            Plain(
                "A card led by a bold title, with a time and a subtitle.",
                listOf("Flights, deliveries, appointments", "Anything identified by its title"),
            ),
        "wear-text" to
            Plain(
                "Text in the watch's type scale and colors, so it matches the rest of the system.",
                listOf("Titles and labels on a watch screen", "Short status messages"),
            ),
        "wear-icon" to
            Plain(
                "A vector icon tinted with the theme's colors.",
                listOf("Icons inside buttons and cards", "Status symbols"),
            ),
        "wear-circular-progress" to
            Plain(
                "A ring that fills up to show progress, or spins while the amount is unknown.",
                listOf("Downloads and syncing", "Goals like steps or minutes"),
            ),
        "wear-linear-progress" to
            Plain(
                "A horizontal bar that fills up to show progress.",
                listOf("Progress inside a card", "Battery or storage levels"),
            ),
        "wear-curved-progress" to
            Plain(
                "Progress drawn along the edge of the round screen, around everything else.",
                listOf("Workout or timer progress", "Full-screen countdowns"),
            ),
        "wear-page-indicator" to
            Plain(
                "Dots that show which page of a swipeable set is on screen.",
                listOf("Onboarding pages", "Swiping between tiles or cards"),
            ),
        "wear-theme" to
            Plain(
                "The watch's design tokens: colors, type and shapes that every component reads, so a brand color changes them all.",
                listOf("Brand colors in a remote watch screen", "A per-user accent color"),
            ),
    )
