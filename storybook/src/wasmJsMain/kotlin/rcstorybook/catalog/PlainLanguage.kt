package rcstorybook.catalog

/**
 * What each component is for, in words a designer or a product manager can use: what it does on
 * screen and when you would reach for it. The developer description stays on the component.
 */
internal class Plain(val what: String, val useWhen: List<String>)

internal val plainLanguage: Map<String, Plain> = mapOf(
    "remote-box" to Plain(
        "A container that stacks things on top of each other, like layers in a design tool, and pins them to a corner, an edge or the centre.",
        listOf("A badge over an avatar", "Text centred on a coloured card", "A background shape behind content"),
    ),
    "remote-row" to Plain(
        "Places items side by side, left to right, and decides how leftover horizontal space is shared between them.",
        listOf("An icon next to a label", "A toolbar of buttons", "Columns that share the width in a ratio"),
    ),
    "remote-column" to Plain(
        "Stacks items top to bottom and decides how leftover vertical space is shared between them.",
        listOf("A card with a title, body and footer", "A list of settings", "A header and footer with content filling the middle"),
    ),
    "remote-arrangement" to Plain(
        "A fixed gap between every item in a row or a column, like auto-layout spacing in Figma.",
        listOf("Consistent spacing in lists and toolbars"),
    ),
    "remote-spacer" to Plain(
        "An invisible gap. A fixed spacer adds a set distance; a flexible one pushes the next item to the far end.",
        listOf("Pushing a button to the right edge", "Adding breathing room between two blocks"),
    ),
    "remote-collapsible" to Plain(
        "A row or column that hides its least important items when space runs out, instead of squashing or cutting them.",
        listOf("Widgets that must work at several sizes", "Toolbars that drop secondary actions on small screens"),
    ),
    "remote-fit-box" to Plain(
        "Holds several versions of the same content, large to small, and shows the biggest one that fits. The device decides, not the server.",
        listOf("A widget with large, medium and small layouts", "Responsive cards without separate designs per device"),
    ),
    "remote-text" to Plain(
        "Shows text with a chosen size, colour, weight, font, alignment and line limit, ending in “…” when it does not fit.",
        listOf("Titles, labels and paragraphs", "Prices and status text that the server updates"),
    ),
    "remote-basic-text" to Plain(
        "The simplest piece of text, with no built-in styling: the building block that richer text is made from.",
        listOf("Fully custom text styles"),
    ),
    "remote-image" to Plain(
        "Shows a picture, embedded in the screen or loaded from a web address, cropped or fitted into its box.",
        listOf("Product photos and banners", "Avatars and thumbnails", "Images the server can change without an app update"),
    ),
    "remote-basic-image" to Plain(
        "The simplest picture element, without extra behaviour.",
        listOf("Decorative images"),
    ),
    "remote-basic-icon" to Plain(
        "A small vector icon that stays sharp at any size and can be recoloured.",
        listOf("Action icons", "Status symbols that change colour"),
    ),
    "remote-canvas" to Plain(
        "A free drawing surface: shapes, lines, text and transformations drawn exactly where you place them, including animated drawings.",
        listOf("Charts, gauges and progress rings", "Custom illustrations", "Watch-face style animated graphics"),
    ),
    "remote-brush" to Plain(
        "Colour gradients — straight, circular or sweeping — used to fill shapes.",
        listOf("Gradient backgrounds and buttons", "Colourful progress indicators"),
    ),
    "remote-outline" to Plain(
        "Turns a shape such as a rounded rectangle into something that can be filled or used as a mask while drawing.",
        listOf("Custom cards and frames drawn on a canvas"),
    ),
    "draw-with-content" to Plain(
        "Draws extra decoration behind or on top of any component, without changing the component itself.",
        listOf("Highlight rings and glows", "Overlays such as badges or scrims"),
    ),
    "remote-component" to Plain(
        "Lets an element know its parent's real size on the device, so it can size itself as a fraction of it.",
        listOf("Bars and fills that are a percentage of their container"),
    ),
    "size-modifiers" to Plain(
        "Controls how big something is: an exact size, a share of the available space, a minimum or maximum, or just as big as its content.",
        listOf("Fixed-size buttons", "Full-width banners", "Text boxes with a maximum width"),
    ),
    "padding-offset" to Plain(
        "Inner spacing around content, nudging an element away from its position, and choosing which of two overlapping elements is on top.",
        listOf("Comfortable tap areas", "Overlapping avatars", "Bringing a selected card to the front"),
    ),
    "background-border-clip" to Plain(
        "Fills an element with colour or an image, gives it an outline, and rounds or reshapes its corners.",
        listOf("Cards and chips", "Circular avatars", "Pill-shaped buttons"),
    ),
    "transform-modifiers" to Plain(
        "Fades, rotates, scales, tilts in 3D or drops a shadow under an element, without moving anything around it.",
        listOf("Disabled states (faded)", "Tilted cards and flips", "Emphasis through scale"),
    ),
    "visibility" to Plain(
        "Shows or hides an element, either keeping its space or letting the layout close the gap. The device can switch it without a new screen from the server.",
        listOf("Expandable sections", "Optional badges"),
    ),
    "scroll" to Plain(
        "Lets content that is longer or wider than its box scroll, with optional snapping, handled entirely on the device.",
        listOf("Lists and feeds", "Horizontal carousels"),
    ),
    "align-by-baseline" to Plain(
        "Lines up texts of different sizes on the same baseline, the way typographers align them.",
        listOf("A large price next to a small currency label"),
    ),
    "clickable" to Plain(
        "Makes something tappable and changes what is shown immediately on the device — toggles, counters, selections — with no trip to the server.",
        listOf("Switches and checkboxes", "Steppers and counters", "Tabs and selections"),
    ),
    "host-action" to Plain(
        "A tap that tells the app around the screen to do something, such as open a page or save a bookmark, optionally while also updating the screen.",
        listOf("Navigation to app screens", "Like, bookmark and share buttons"),
    ),
    "touch" to Plain(
        "Reacts to the moment a finger goes down, lifts, or slides away, for press-and-hold feedback.",
        listOf("Pressed states", "Hold-to-confirm buttons"),
    ),
    "semantics" to Plain(
        "Describes elements to screen readers and other assistive technology: what they are, what state they are in, what they do.",
        listOf("Making every screen accessible", "Labelling icon-only buttons"),
    ),
    "animate-value" to Plain(
        "Smoothly animates a value such as size or transparency when it changes, with a chosen speed and feel (ease, bounce, spring).",
        listOf("Expanding cards", "Fading content in and out", "Playful bouncy feedback"),
    ),
    "enter-exit" to Plain(
        "Animations that play when an element appears or disappears: fade, slide in from an edge, rotate or dissolve into particles.",
        listOf("Showing and hiding panels", "Toasts and banners"),
    ),
    "shared-element" to Plain(
        "Moves the same element smoothly from one layout to another, so users can follow it between two states of the screen.",
        listOf("A thumbnail growing into a detail view", "Expanding a compact card"),
    ),
    "remote-state-layout" to Plain(
        "Holds several versions of a screen — for example loading, content and error — and shows the right one for the current state, switching on the device.",
        listOf("Loading / success / error states", "Step-by-step flows", "Collapsed and expanded views"),
    ),
    "named-state" to Plain(
        "Values with a name, such as a title or a colour, that the app showing the screen can change later without the server sending a new design.",
        listOf("Live scores and prices", "Personalised greetings", "Theme colours chosen by the app"),
    ),
    "expressions" to Plain(
        "Calculations that run on the device, every frame if needed, so things can move or resize from math or time without new data.",
        listOf("Animated charts and waves", "Sizes computed from other sizes"),
    ),
    "color-expressions" to Plain(
        "Colours computed on the device: blends between two colours, rotating hues or changing transparency.",
        listOf("Status colours that shift smoothly", "Ambient animated backgrounds"),
    ),
    "time-config" to Plain(
        "Things only the device knows: the current time and date, and the screen's size, density and text size.",
        listOf("Clocks and countdowns", "Layouts that adapt to the screen", "Respecting the user's font size"),
    ),
)
