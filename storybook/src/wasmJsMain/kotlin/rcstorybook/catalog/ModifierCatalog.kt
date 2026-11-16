package rcstorybook.catalog

import androidx.compose.ui.graphics.Color
import rcstorybook.stories.*

private const val M = "androidx.compose.remote.creation.compose.modifier"
private const val C = "androidx.compose.remote.creation.compose"

internal val modifierComponents =
    listOf(
        Component(
            id = "size-modifiers",
            title = "Size",
            category = "Modifiers",
            symbols =
                listOf(
                    "$M.size",
                    "$M.width",
                    "$M.height",
                    "$M.fillMaxWidth",
                    "$M.fillMaxHeight",
                    "$M.fillMaxSize",
                    "$M.widthIn",
                    "$M.heightIn",
                    "$M.defaultMinSize",
                    "$M.wrapContentSize",
                ),
            description = "Fixed, fractional, bounded and wrapped sizes.",
            stories =
                listOf(
                    Story(
                        "size-modifiers--fixed",
                        "size()",
                        "SizeModifiers",
                        "A fixed size in dp.",
                        listOf(
                            int("width", 160, 20, 320, step = 10),
                            int("height", 100, 20, 320, step = 10),
                        ),
                    ) {
                        SizeModifiers(it.int("width"), it.int("height"))
                    },
                    Story(
                        "size-modifiers--fill",
                        "fillMax*",
                        "FillFractions",
                        "fillMaxWidth / fillMaxHeight with a fraction of the parent.",
                        listOf(
                            float("widthFraction", 0.6f, 0f, 1f, step = 0.05f),
                            float("heightFraction", 0.5f, 0f, 1f, step = 0.05f),
                        ),
                    ) {
                        FillFractions(it.float("widthFraction"), it.float("heightFraction"))
                    },
                    Story(
                        "size-modifiers--fill-parent",
                        "fillParentMax*",
                        "FillMaxSizeInParent",
                        "fillParentMaxWidth / Height and fillMaxSize(fraction).",
                        listOf(float("fraction", 0.8f, 0.1f, 1f, step = 0.05f)),
                    ) {
                        FillMaxSizeInParent(it.float("fraction"))
                    },
                    Story(
                        "size-modifiers--constraints",
                        "widthIn / heightIn / defaultMinSize",
                        "SizeConstraints",
                        "Bounds on the size the content asks for.",
                        listOf(
                            int("minWidth", 80, 0, 200, step = 10),
                            int("maxWidth", 220, 40, 320, step = 10),
                            int("minHeight", 40, 0, 120, step = 4),
                            text("text", "Bounded by widthIn and heightIn"),
                        ),
                    ) {
                        SizeConstraints(
                            it.int("minWidth"),
                            it.int("maxWidth"),
                            it.int("minHeight"),
                            it.text("text"),
                        )
                    },
                    Story(
                        "size-modifiers--wrap",
                        "wrapContent*",
                        "WrapContent",
                        "Let the content choose its size inside a larger constraint.",
                        listOf(choice("mode", More.wrapModes)),
                    ) {
                        WrapContent(it["mode"])
                    },
                ),
        ),
        Component(
            id = "padding-offset",
            title = "Padding, offset, zIndex",
            category = "Modifiers",
            symbols =
                listOf("$M.padding", "$C.layout.RemotePaddingValues", "$M.offset", "$M.zIndex"),
            description =
                "Space around content, a drawing offset, and the stacking order of siblings.",
            stories =
                listOf(
                    Story(
                        "padding-offset--padding",
                        "padding",
                        "PaddingVariants",
                        "Per-side, horizontal/vertical and RemotePaddingValues padding.",
                        listOf(
                            int("start", 16, 0, 48),
                            int("top", 8, 0, 48),
                            int("end", 32, 0, 48),
                            int("bottom", 8, 0, 48),
                        ),
                    ) {
                        PaddingVariants(
                            it.int("start"),
                            it.int("top"),
                            it.int("end"),
                            it.int("bottom"),
                        )
                    },
                    Story(
                        "padding-offset--offset",
                        "offset & zIndex",
                        "OffsetAndZIndex",
                        "Move B over A, then flip which one is on top.",
                        listOf(
                            int("x", 60, -60, 140, step = 4),
                            int("y", 50, -60, 80, step = 4),
                            bool("frontOnTop", false),
                        ),
                    ) {
                        OffsetAndZIndex(it.int("x"), it.int("y"), it.bool("frontOnTop"))
                    },
                ),
        ),
        Component(
            id = "background-border-clip",
            title = "Background, border, clip",
            category = "Modifiers",
            symbols =
                listOf(
                    "$M.background",
                    "$M.border",
                    "$M.clip",
                    "$C.shapes.RemoteRoundedCornerShape",
                    "$C.shapes.RemoteCornerSize",
                    "$C.painter.painterRemoteColor",
                ),
            description = "Paint a component, outline it and clip it to a RemoteShape.",
            stories =
                listOf(
                    Story(
                        "background-border-clip--shape",
                        "Shapes",
                        "BackgroundBorderClip",
                        "The same shape clips the background and draws the border.",
                        listOf(
                            choice("shape", More.shapes),
                            int("borderWidth", 3, 0, 12),
                            color("borderColor", Color(0xFF1E88E5)),
                            color("fill", Color(0xFFE3F2FD)),
                        ),
                    ) {
                        BackgroundBorderClip(
                            it["shape"],
                            it.int("borderWidth"),
                            it.color("borderColor"),
                            it.color("fill"),
                        )
                    },
                    Story(
                        "background-border-clip--corners",
                        "Per-corner radius",
                        "CornerShapes",
                        "Each corner on its own, and a percentage RemoteCornerSize.",
                        listOf(
                            int("topStart", 40, 0, 55),
                            int("topEnd", 8, 0, 55),
                            int("bottomEnd", 40, 0, 55),
                            int("bottomStart", 8, 0, 55),
                        ),
                    ) {
                        CornerShapes(
                            it.int("topStart"),
                            it.int("topEnd"),
                            it.int("bottomEnd"),
                            it.int("bottomStart"),
                        )
                    },
                    Story(
                        "background-border-clip--painter",
                        "Painter background",
                        "PainterBackground",
                        "background(painterRemoteColor(…)).",
                        listOf(color("color", Color(0xFF8E24AA)), bool("useImage", false)),
                    ) {
                        PainterBackground(it.color("color"), it.bool("useImage"))
                    },
                    Story(
                        "background-border-clip--clip",
                        "clip",
                        "Shapes",
                        "One box clipped to each shape.",
                        listOf(choice("shape", More.shapes, default = 3)),
                    ) {
                        Shapes(it["shape"])
                    },
                ),
        ),
        Component(
            id = "transform-modifiers",
            title = "alpha, rotate, scale, graphicsLayer",
            category = "Modifiers",
            symbols =
                listOf(
                    "$M.alpha",
                    "$M.rotate",
                    "$M.scale",
                    "$M.graphicsLayer",
                    "$M.RemoteGraphicsLayerScope",
                ),
            description = "Transform how a component draws, without changing its layout.",
            stories =
                listOf(
                    Story(
                        "transform-modifiers--simple",
                        "alpha / rotate / scale",
                        "AlphaRotateScale",
                        "The three most common draw transforms.",
                        listOf(
                            float("alpha", 1f, 0f, 1f, step = 0.05f),
                            float("degrees", 15f, -180f, 180f, step = 5f),
                            float("scale", 1f, 0.2f, 2f, step = 0.05f),
                        ),
                    ) {
                        AlphaRotateScale(it.float("alpha"), it.float("degrees"), it.float("scale"))
                    },
                    Story(
                        "transform-modifiers--graphics-layer",
                        "graphicsLayer",
                        "GraphicsLayer",
                        "3D rotations, scale and shadow on a layer.",
                        listOf(
                            float("rotationX", 0f, -80f, 80f, step = 5f),
                            float("rotationY", 30f, -80f, 80f, step = 5f),
                            float("rotationZ", 0f, -180f, 180f, step = 5f),
                            float("scaleX", 1f, 0.2f, 2f, step = 0.05f),
                            float("scaleY", 1f, 0.2f, 2f, step = 0.05f),
                            float("shadowElevation", 8f, 0f, 32f, step = 1f),
                        ),
                    ) {
                        GraphicsLayer(
                            it.float("rotationX"),
                            it.float("rotationY"),
                            it.float("rotationZ"),
                            it.float("scaleX"),
                            it.float("scaleY"),
                            it.float("shadowElevation"),
                        )
                    },
                    Story(
                        "transform-modifiers--graphics-layer-block",
                        "graphicsLayer { }",
                        "GraphicsLayerBlock",
                        "The block form of graphicsLayer.",
                        listOf(
                            float("translationX", 40f, -100f, 100f, step = 5f),
                            float("translationY", 0f, -100f, 100f, step = 5f),
                            float("alpha", 0.8f, 0f, 1f, step = 0.05f),
                        ),
                    ) {
                        GraphicsLayerBlock(
                            it.float("translationX"),
                            it.float("translationY"),
                            it.float("alpha"),
                        )
                    },
                ),
        ),
        Component(
            id = "visibility",
            title = "visibility",
            category = "Modifiers",
            symbols = listOf("$M.visibility"),
            description =
                "Show, hide (keeping space) or remove a component, from a RemoteInt the player can change.",
            stories =
                listOf(
                    Story(
                        "visibility--states",
                        "Visible / invisible / gone",
                        "Visibility",
                        "The middle box in its three visibility states.",
                        listOf(choice("visibility", More.visibilities)),
                    ) {
                        Visibility(it["visibility"])
                    },
                ),
        ),
        Component(
            id = "scroll",
            title = "verticalScroll / horizontalScroll",
            category = "Modifiers",
            symbols =
                listOf(
                    "$M.verticalScroll",
                    "$M.horizontalScroll",
                    "$M.rememberRemoteScrollState",
                    "$M.RemoteScrollState",
                ),
            description =
                "Scrolling handled entirely by the player. Drag the content on the stage.",
            stories =
                listOf(
                    Story(
                        "scroll--vertical",
                        "Vertical",
                        "VerticalScroll",
                        "A scrolling column; notches > 0 snaps to positions.",
                        listOf(int("itemCount", 12, 1, 40), int("notches", 0, 0, 10)),
                    ) {
                        VerticalScroll(it.int("itemCount"), it.int("notches"))
                    },
                    Story(
                        "scroll--horizontal",
                        "Horizontal",
                        "HorizontalScroll",
                        "A scrolling row.",
                        listOf(int("itemCount", 10, 1, 40)),
                    ) {
                        HorizontalScroll(it.int("itemCount"))
                    },
                ),
        ),
        Component(
            id = "align-by-baseline",
            title = "alignByBaseline",
            category = "Modifiers",
            symbols = listOf("$M.alignByBaseline"),
            description = "Align texts of different sizes in a row by their baseline.",
            stories =
                listOf(
                    Story(
                        "align-by-baseline--row",
                        "Mixed sizes",
                        "AlignByBaseline",
                        "Two texts sharing one baseline.",
                        listOf(int("smallSize", 14, 8, 32), int("largeSize", 40, 16, 72)),
                    ) {
                        AlignByBaseline(it.int("smallSize"), it.int("largeSize"))
                    },
                ),
        ),
    )

internal val interactionComponents =
    listOf(
        Component(
            id = "clickable",
            title = "clickable + valueChangeAction",
            category = "Interaction",
            symbols = listOf("$M.clickable", "$C.action.valueChangeAction", "$M.RemoteRole"),
            description =
                "Taps change state stored in the document; the player re-evaluates everything that reads it. Tap the stage.",
            stories =
                listOf(
                    Story(
                        "clickable--toggle",
                        "Toggle",
                        "ClickToggle",
                        "A boolean flipped on tap, selecting colour and text.",
                        listOf(
                            color("onColor", Color(0xFF2E7D32)),
                            color("offColor", Color(0xFF616161)),
                        ),
                    ) {
                        ClickToggle(it.color("onColor"), it.color("offColor"))
                    },
                    Story(
                        "clickable--counter",
                        "Counter",
                        "ClickCounter",
                        "An integer changed by expressions on tap.",
                        listOf(int("step", 1, 1, 10)),
                    ) {
                        ClickCounter(it.int("step"))
                    },
                ),
        ),
        Component(
            id = "host-action",
            title = "hostAction / combinedAction",
            category = "Interaction",
            symbols =
                listOf(
                    "$C.action.hostAction",
                    "$C.action.combinedAction",
                    "$C.action.RemoteAction",
                ),
            description =
                "Actions delivered to the app that plays the document. Tap, then open the Actions tab.",
            stories =
                listOf(
                    Story(
                        "host-action--named",
                        "Named host actions",
                        "HostActions",
                        "A named action, with and without a value.",
                        listOf(text("actionName", "open_details")),
                    ) {
                        HostActions(it.text("actionName"))
                    },
                    Story(
                        "host-action--combined",
                        "combinedAction",
                        "CombinedActions",
                        "Change local state and notify the host in one tap.",
                        listOf(text("actionName", "bookmark_changed")),
                    ) {
                        CombinedActions(it.text("actionName"))
                    },
                ),
        ),
        Component(
            id = "touch",
            title = "onTouchDown / Up / Cancel",
            category = "Interaction",
            symbols = listOf("$M.onTouchDown", "$M.onTouchUp", "$M.onTouchCancel"),
            description = "Actions run at each phase of a touch. Press and hold the stage.",
            stories =
                listOf(
                    Story(
                        "touch--press",
                        "Press state",
                        "TouchEvents",
                        "Pressed while held, with a count of touches.",
                        listOf(color("pressedColor", Color(0xFFE53935))),
                    ) {
                        TouchEvents(it.color("pressedColor"))
                    },
                ),
        ),
        Component(
            id = "semantics",
            title = "semantics",
            category = "Interaction",
            symbols =
                listOf("$M.semantics", "$M.clearAndSetSemantics", "$M.RemoteSemanticsProperties"),
            description =
                "Accessibility properties carried in the document for the player's accessibility tree.",
            stories =
                listOf(
                    Story(
                        "semantics--properties",
                        "Properties",
                        "Semantics",
                        "contentDescription, stateDescription and role; see the Document tab.",
                        listOf(text("description", "Notifications"), text("state", "On")),
                    ) {
                        Semantics(it.text("description"), it.text("state"))
                    },
                ),
        ),
    )

internal val animationComponents =
    listOf(
        Component(
            id = "animate-value",
            title = "animateRemote*AsState",
            category = "Animation",
            symbols =
                listOf(
                    "$C.state.animateRemoteFloatAsState",
                    "$C.state.animateRemoteDpAsState",
                    "$C.state.remoteTween",
                    "$C.state.remoteSpring",
                    "$C.state.RemoteEasing",
                ),
            description =
                "Values the player animates toward their target whenever it changes. Tap the stage.",
            stories =
                listOf(
                    Story(
                        "animate-value--tween",
                        "Tween & easing",
                        "AnimateFloat",
                        "An alpha tweened with each RemoteEasing.",
                        listOf(
                            int("durationMillis", 600, 50, 3000, step = 50),
                            choice("easing", More.easings, default = 6),
                            float("dimmedAlpha", 0.25f, 0f, 1f, step = 0.05f),
                        ),
                    ) {
                        AnimateFloat(
                            it.int("durationMillis"),
                            it["easing"],
                            it.float("dimmedAlpha"),
                        )
                    },
                    Story(
                        "animate-value--spring",
                        "Spring",
                        "AnimateDpSpring",
                        "A size animated by a spring.",
                        listOf(
                            float("dampingRatio", 0.4f, 0.1f, 1f, step = 0.05f),
                            float("stiffness", 120f, 20f, 1500f, step = 10f),
                            int("small", 90, 40, 200, step = 10),
                            int("large", 180, 60, 300, step = 10),
                        ),
                    ) {
                        AnimateDpSpring(
                            it.float("dampingRatio"),
                            it.float("stiffness"),
                            it.int("small"),
                            it.int("large"),
                        )
                    },
                ),
        ),
        Component(
            id = "enter-exit",
            title = "animateEnterExit / animationSpec",
            category = "Animation",
            symbols =
                listOf(
                    "$M.animateEnterExit",
                    "$M.animationSpec",
                    "$M.RemoteEnterTransition",
                    "$M.RemoteExitTransition",
                    "$M.remoteFadeIn",
                    "$M.remoteFadeOut",
                ),
            description =
                "Transitions the player plays when a component's visibility or layout changes.",
            stories =
                listOf(
                    Story(
                        "enter-exit--transitions",
                        "Enter & exit",
                        "EnterExitTransitions",
                        "Every RemoteEnterTransition and RemoteExitTransition.",
                        listOf(
                            choice("enter", More.enters),
                            choice("exit", More.exits),
                            int("durationMillis", 500, 100, 3000, step = 50),
                        ),
                    ) {
                        EnterExitTransitions(it["enter"], it["exit"], it.int("durationMillis"))
                    },
                    Story(
                        "enter-exit--fade",
                        "animationSpec + fade",
                        "FadeHelpers",
                        "animationSpec with remoteFadeIn / remoteFadeOut.",
                        listOf(int("durationMillis", 400, 100, 3000, step = 50)),
                    ) {
                        FadeHelpers(it.int("durationMillis"))
                    },
                    Story(
                        "enter-exit--toggle",
                        "animationSpec(enabled)",
                        "AnimationToggle",
                        "Layout changes animated or instant.",
                        listOf(bool("enabled", true)),
                    ) {
                        AnimationToggle(it.bool("enabled"))
                    },
                ),
        ),
        Component(
            id = "shared-element",
            title = "sharedElement / sharedBounds",
            category = "Animation",
            symbols = listOf("$M.sharedElement", "$M.sharedBounds"),
            description =
                "One element moving between two states of a RemoteStateLayout. Tap the stage.",
            stories =
                listOf(
                    Story(
                        "shared-element--move",
                        "Between states",
                        "SharedElement",
                        "The same key in both branches.",
                        listOf(
                            int("durationMillis", 500, 100, 3000, step = 50),
                            bool("bounds", false),
                        ),
                    ) {
                        SharedElement(it.int("durationMillis"), it.bool("bounds"))
                    },
                ),
        ),
    )

internal val stateComponents =
    listOf(
        Component(
            id = "remote-state-layout",
            title = "RemoteStateLayout",
            category = "State & expressions",
            symbols =
                listOf(
                    "$C.layout.RemoteStateLayout",
                    "$C.state.MutableRemoteBoolean",
                    "$C.state.MutableRemoteInt",
                    "$C.state.MutableRemoteEnum",
                ),
            description =
                "Every branch is in the document; the player shows the one for the current state. Tap the stage.",
            stories =
                listOf(
                    Story(
                        "remote-state-layout--boolean",
                        "Boolean",
                        "StateLayoutBoolean",
                        "Two branches selected by a RemoteBoolean.",
                        listOf(bool("initial", true)),
                    ) {
                        StateLayoutBoolean(it.bool("initial"))
                    },
                    Story(
                        "remote-state-layout--int",
                        "Int",
                        "StateLayoutInt",
                        "Three branches selected by a RemoteInt.",
                        listOf(int("initial", 0, 0, 2)),
                    ) {
                        StateLayoutInt(it.int("initial"))
                    },
                    Story(
                        "remote-state-layout--enum",
                        "Enum",
                        "StateLayoutEnum",
                        "A branch per enum entry.",
                        listOf(choice("initial", More.screens)),
                    ) {
                        StateLayoutEnum(it["initial"])
                    },
                ),
        ),
        Component(
            id = "named-state",
            title = "rememberNamedRemote*",
            category = "State & expressions",
            symbols =
                listOf(
                    "$C.state.rememberNamedRemoteString",
                    "$C.state.rememberNamedRemoteFloat",
                    "$C.state.rememberNamedRemoteColor",
                    "$C.state.rememberNamedRemoteBoolean",
                    "$C.state.rememberNamedRemoteInt",
                    "$C.state.rememberNamedRemoteEnum",
                ),
            description =
                "Named values with defaults in the document, which a host can override at play time without sending a new document.",
            stories =
                listOf(
                    Story(
                        "named-state--card",
                        "Named values",
                        "NamedValues",
                        "String, float, colour, boolean, dp and int named values.",
                        listOf(
                            text("title", "Hello, host"),
                            float("scale", 1.2f, 0.5f, 2.5f, step = 0.05f),
                            color("accent", Color(0xFF3949AB)),
                            bool("showBadge", true),
                        ),
                    ) {
                        NamedValues(
                            it.text("title"),
                            it.float("scale"),
                            it.color("accent"),
                            it.bool("showBadge"),
                        )
                    },
                    Story(
                        "named-state--mutable",
                        "Mutable values",
                        "MutableValues",
                        "MutableRemoteFloat, String and Int changed by one combined action.",
                        listOf(
                            float("step", 0.2f, 0.05f, 0.5f, step = 0.05f),
                            text("label", "Not tapped"),
                        ),
                    ) {
                        MutableValues(it.float("step"), it.text("label"))
                    },
                    Story(
                        "named-state--mutable-image",
                        "Mutable image",
                        "MutableImage",
                        "A mutable bitmap declared by the document.",
                        listOf(bool("swap", false)),
                    ) {
                        MutableImage(it.bool("swap"))
                    },
                    Story(
                        "named-state--enum",
                        "Named enum",
                        "NamedEnum",
                        "An enum a host can set by name.",
                        listOf(choice("initial", More.screens, default = 1)),
                    ) {
                        NamedEnum(it["initial"])
                    },
                ),
        ),
        Component(
            id = "expressions",
            title = "RemoteFloat expressions",
            category = "State & expressions",
            symbols =
                listOf(
                    "$C.state.RemoteFloat",
                    "$C.state.RemoteFloatOperations",
                    "$C.state.RemoteTimeVariables",
                ),
            description =
                "Arithmetic and math on remote values builds expressions the player evaluates each frame.",
            stories =
                listOf(
                    Story(
                        "expressions--functions",
                        "Math functions",
                        "FloatFunctionPlot",
                        "y = f(x) plotted from 61 player-evaluated expressions.",
                        listOf(
                            choice("function", More.functions),
                            bool("animate", false),
                            color("color", Color(0xFFD81B60)),
                        ),
                    ) {
                        FloatFunctionPlot(it["function"], it.bool("animate"), it.color("color"))
                    },
                    Story(
                        "expressions--width",
                        "Driving a size",
                        "FloatExpressions",
                        "A width computed from time.",
                        listOf(
                            float("speed", 1.5f, 0f, 6f, step = 0.1f),
                            float("base", 40f, 0f, 120f, step = 5f),
                        ),
                    ) {
                        FloatExpressions(it.float("speed"), it.float("base"))
                    },
                ),
        ),
        Component(
            id = "color-expressions",
            title = "RemoteColor expressions",
            category = "State & expressions",
            symbols = listOf("$C.state.RemoteColor", "$C.state.lerp"),
            description =
                "Colours computed by the player: lerp, hsv and copy with a computed alpha.",
            stories =
                listOf(
                    Story(
                        "color-expressions--animated",
                        "lerp / hsv / copy",
                        "ColorExpressions",
                        "Three colour expressions driven by time.",
                        listOf(
                            color("from", Color(0xFF1E88E5)),
                            color("to", Color(0xFFE53935)),
                            float("speed", 1f, 0f, 5f, step = 0.1f),
                        ),
                    ) {
                        ColorExpressions(it.color("from"), it.color("to"), it.float("speed"))
                    },
                ),
        ),
        Component(
            id = "time-config",
            title = "Time & configuration",
            category = "State & expressions",
            symbols =
                listOf(
                    "$C.state.RemoteTimeVariables",
                    "$C.state.RemoteConfiguration",
                    "$C.text.RemoteTimeDefaults",
                ),
            description = "Values only the player knows: the clock and its display.",
            stories =
                listOf(
                    Story(
                        "time-config--clock",
                        "Clock",
                        "Clock",
                        "A ticking clock with no new documents.",
                        listOf(bool("is24Hour", true)),
                    ) {
                        Clock(it.bool("is24Hour"))
                    },
                    Story(
                        "time-config--configuration",
                        "RemoteConfiguration",
                        "Configuration",
                        "The player's window, density and font size. Change the viewport.",
                        listOf(),
                    ) {
                        Configuration()
                    },
                ),
        ),
    )
