package rcstorybook.catalog

import androidx.compose.ui.graphics.Color
import rcstorybook.stories.*

private const val C = "androidx.compose.remote.creation.compose"
private const val F = "androidx.compose.remote.foundation"

internal val contentComponents = listOf(
    Component(
        id = "remote-text", title = "RemoteText", category = "Content",
        symbols = listOf("$C.layout.RemoteText", "$C.text.RemoteTextStyle", "$C.text.RemoteFontWeight", "$C.text.RemoteFontFamily"),
        description = "Draws a RemoteString with a colour, size, weight, style, family, alignment and overflow; " +
            "every attribute can also be passed as one RemoteTextStyle.",
        stories = listOf(
            Story("remote-text--basics", "Basics", "TextBasics", "Text, size, colour, weight and style.",
                listOf(
                    text("text", "Hello, Remote Compose"),
                    int("fontSize", 24, 8, 64),
                    color("color", Color(0xFF202124)),
                    choice("fontWeight", More.fontWeights, default = 2),
                    choice("fontStyle", More.fontStyles),
                ),
            ) { TextBasics(it.text("text"), it.int("fontSize"), it.color("color"), it["fontWeight"], it["fontStyle"]) },
            Story("remote-text--overflow", "Overflow & alignment", "TextOverflowAndAlign",
                "maxLines and overflow decide how a long text ends; textAlign places it in its width.",
                listOf(
                    int("maxLines", 2, 1, 6),
                    choice("overflow", More.overflows, default = 1),
                    choice("textAlign", More.textAligns),
                    int("width", 260, 80, 340, step = 10),
                ),
            ) { TextOverflowAndAlign(it.int("maxLines"), it["overflow"], it["textAlign"], it.int("width")) },
            Story("remote-text--families", "Font families", "TextFontFamilies", "The generic families a player resolves to its own fonts.",
                listOf(text("sample", "Quick brown fox"), int("fontSize", 18, 10, 36)),
            ) { TextFontFamilies(it.text("sample"), it.int("fontSize")) },
            Story("remote-text--weights", "Font weights", "TextWeights", "Every named RemoteFontWeight.",
                listOf(int("fontSize", 18, 10, 32)), height = 400,
            ) { TextWeights(it.int("fontSize")) },
            Story("remote-text--style", "RemoteTextStyle", "TextStyled",
                "Letter spacing, line height, decoration and background through one RemoteTextStyle.",
                listOf(
                    float("letterSpacing", 0.5f, -1f, 6f, step = 0.25f),
                    int("lineHeight", 28, 16, 48),
                    choice("decoration", More.decorations, default = 1),
                    color("highlight", Color(0x33FFC107)),
                ),
            ) { TextStyled(it.float("letterSpacing"), it.int("lineHeight"), it["decoration"], it.color("highlight")) },
            Story("remote-text--variation", "Font variation", "TextFontVariation",
                "Variable font axes (weight, width). The player applies them only when the font it resolves has those axes; the web player\'s default font does not.",
                listOf(float("weight", 700f, 100f, 1000f, step = 50f), float("width", 100f, 25f, 150f, step = 5f)),
            ) { TextFontVariation(it.float("weight"), it.float("width")) },
        ),
    ),
    Component(
        id = "remote-basic-text", title = "RemoteBasicText", category = "Content",
        symbols = listOf("$F.text.RemoteBasicText"),
        description = "The foundation text primitive, without RemoteText's defaults.",
        stories = listOf(
            Story("remote-basic-text--basics", "Basics", "BasicText", "RemoteBasicText with a size, colour and style.",
                listOf(text("text", "Foundation text"), int("fontSize", 20, 8, 48), color("color", Color(0xFF3949AB)), bool("italic", false)),
            ) { BasicText(it.text("text"), it.int("fontSize"), it.color("color"), it.bool("italic")) },
        ),
    ),
    Component(
        id = "remote-image", title = "RemoteImage", category = "Content",
        symbols = listOf("$C.layout.RemoteImage", "$C.layout.RemoteContentScale", "$C.state.RemoteImageBitmap"),
        description = "Draws a RemoteImageBitmap — embedded in the document or loaded from a URL — scaled with a RemoteContentScale.",
        stories = listOf(
            Story("remote-image--content-scale", "Content scale", "ImageContentScale",
                "A 160×90 bitmap in a box of another shape, with every RemoteContentScale.",
                listOf(choice("contentScale", More.contentScales), int("width", 200, 40, 320, step = 10), int("height", 200, 40, 320, step = 10), float("alpha", 1f, 0f, 1f, step = 0.05f)),
            ) { ImageContentScale(it["contentScale"], it.int("width"), it.int("height"), it.float("alpha")) },
            Story("remote-image--url", "From a URL", "ImageFromUrl",
                "The document carries only the URL; the player fetches the image. A named bitmap can be swapped by the host.",
                listOf(text("url", "https://raw.githubusercontent.com/ardmn/remote-compose-storybook/main/web/assets/sample.png"), bool("named", false)),
            ) { ImageFromUrl(it.text("url"), it.bool("named")) },
        ),
    ),
    Component(
        id = "remote-basic-image", title = "RemoteBasicImage", category = "Content",
        symbols = listOf("$F.image.RemoteBasicImage"),
        description = "The foundation image primitive.",
        stories = listOf(
            Story("remote-basic-image--basics", "Basics", "BasicImage", "RemoteBasicImage with a content scale.",
                listOf(choice("contentScale", More.contentScales, default = 1), int("size", 140, 40, 300, step = 10)),
            ) { BasicImage(it["contentScale"], it.int("size")) },
        ),
    ),
    Component(
        id = "remote-basic-icon", title = "RemoteBasicIcon", category = "Content",
        symbols = listOf("$F.icon.RemoteBasicIcon", "$C.capture.RemoteImageVector", "$C.vector.RemotePathScope"),
        description = "Draws a RemoteImageVector, a vector built from path commands, tinted at play time.",
        stories = listOf(
            Story("remote-basic-icon--vector", "Vector icon", "BasicIcon", "A heart built with RemoteImageVector.Builder and path { }.",
                listOf(color("tint", Color(0xFFE53935)), int("size", 64, 16, 200, step = 4), float("strokeWidth", 0f, 0f, 4f, step = 0.25f)),
            ) { BasicIcon(it.color("tint"), it.int("size"), it.float("strokeWidth")) },
        ),
    ),
)

internal val canvasComponents = listOf(
    Component(
        id = "remote-canvas", title = "RemoteCanvas", category = "Canvas",
        symbols = listOf("$C.layout.RemoteCanvas", "$C.layout.RemoteDrawScope", "$C.state.RemotePaint"),
        description = "Immediate-mode drawing recorded into the document: shapes, lines, text and transforms with a RemotePaint.",
        stories = listOf(
            Story("remote-canvas--shapes", "Shapes", "CanvasShapes", "drawRect, drawRoundRect, drawCircle, drawOval, drawArc, drawLine.",
                listOf(choice("style", More.paintingStyles), float("strokeWidth", 6f, 1f, 24f, step = 1f), color("color", Color(0xFF1E88E5))),
            ) { CanvasShapes(it["style"], it.float("strokeWidth"), it.color("color")) },
            Story("remote-canvas--strokes", "Strokes", "CanvasStrokes", "Stroke width, cap and join.",
                listOf(float("strokeWidth", 16f, 1f, 40f, step = 1f), choice("cap", More.strokeCaps, default = 1), choice("join", More.strokeJoins)),
            ) { CanvasStrokes(it.float("strokeWidth"), it["cap"], it["join"]) },
            Story("remote-canvas--text", "drawText", "CanvasText", "Text drawn at a baseline position.",
                listOf(text("text", "Drawn on a canvas"), float("textSize", 56f, 12f, 120f, step = 2f), color("color", Color(0xFF00897B)), choice("typeface", More.typefaces)),
            ) { CanvasText(it.text("text"), it.float("textSize"), it.color("color"), it["typeface"]) },
            Story("remote-canvas--transforms", "Transforms", "CanvasTransforms", "translate, rotate and scale blocks around a pivot.",
                listOf(float("degrees", 30f, -180f, 180f, step = 5f), float("scale", 1f, 0.2f, 2f, step = 0.05f), float("translateX", 0f, -100f, 100f, step = 5f)),
            ) { CanvasTransforms(it.float("degrees"), it.float("scale"), it.float("translateX")) },
            Story("remote-canvas--clip", "clipRect & withTransform", "CanvasClipAndTransform", "Draw inside a clip rectangle, through one combined transform.",
                listOf(float("degrees", 30f, -90f, 90f, step = 5f), float("inset", 30f, 0f, 90f, step = 2f)),
            ) { CanvasClipAndTransform(it.float("degrees"), it.float("inset")) },
            Story("remote-canvas--image", "drawImage", "CanvasImage", "An embedded bitmap drawn at a position.",
                listOf(float("x", 100f, 0f, 200f, step = 4f), float("y", 50f, 0f, 100f, step = 4f)),
            ) { CanvasImage(it.float("x"), it.float("y")) },
            Story("remote-canvas--use-paint", "usePaint & blend modes", "CanvasUsePaint", "A current paint for a block, and every RemoteBlendMode.",
                listOf(color("color", Color(0xFFFFB300)), choice("blendMode", More.blendModes, default = 1)),
            ) { CanvasUsePaint(it.color("color"), it["blendMode"]) },
            Story("remote-canvas--animated", "Time-driven drawing", "CanvasAnimated", "Positions computed from RemoteTimeVariables every frame.",
                listOf(float("speed", 1.5f, 0f, 6f, step = 0.1f), float("amplitude", 80f, 10f, 100f, step = 2f)),
            ) { CanvasAnimated(it.float("speed"), it.float("amplitude")) },
        ),
    ),
    Component(
        id = "remote-brush", title = "RemoteBrush", category = "Canvas",
        symbols = listOf("$C.shaders.RemoteBrush", "$C.shaders.linearGradient", "$C.shaders.radialGradient", "$C.shaders.sweepGradient"),
        description = "Gradients as shaders applied to a RemotePaint.",
        stories = listOf(
            Story("remote-brush--gradients", "Gradients", "CanvasGradients", "Linear, horizontal, vertical, radial and sweep gradients.",
                listOf(choice("kind", More.gradients), color("first", Color(0xFF7C4DFF)), color("second", Color(0xFF00E5FF))),
            ) { CanvasGradients(it["kind"], it.color("first"), it.color("second")) },
        ),
    ),
    Component(
        id = "remote-outline", title = "Shapes as outlines", category = "Canvas",
        symbols = listOf("$C.shapes.RemoteShape", "$C.shapes.RemoteOutline", "$C.shapes.drawOutline"),
        description = "A RemoteShape creates a RemoteOutline for a size; drawOutline paints it.",
        stories = listOf(
            Story("remote-outline--draw", "drawOutline", "CanvasOutline", "A rounded-corner shape drawn on a canvas.",
                listOf(int("corner", 32, 0, 90), color("color", Color(0xFF1E88E5))),
            ) { CanvasOutline(it.int("corner"), it.color("color")) },
        ),
    ),
    Component(
        id = "draw-with-content", title = "drawWithContent", category = "Canvas",
        symbols = listOf("$C.modifier.drawWithContent", "$C.layout.RemoteContentDrawScope"),
        description = "Draw behind or in front of a component's own content.",
        stories = listOf(
            Story("draw-with-content--ring", "Behind or in front", "DrawWithContent", "A circle drawn before or after drawContent().",
                listOf(color("ringColor", Color(0xFFFFCA28)), float("ringScale", 0.9f, 0.2f, 1f, step = 0.05f), bool("behind", true)),
            ) { DrawWithContent(it.color("ringColor"), it.float("ringScale"), it.bool("behind")) },
        ),
    ),
    Component(
        id = "remote-component", title = "remoteComponent()", category = "Canvas",
        symbols = listOf("$C.layout.remoteComponent", "$C.layout.RemoteComponent"),
        description = "The enclosing component's size and position as remote values, known only at play time.",
        stories = listOf(
            Story("remote-component--size", "Size from the parent", "ComponentSizeExpression", "A child sized to a fraction of its parent's measured height.",
                listOf(float("fraction", 0.5f, 0.1f, 1f, step = 0.05f), int("parentHeight", 220, 80, 320, step = 10)),
            ) { ComponentSizeExpression(it.float("fraction"), it.int("parentHeight")) },
        ),
    ),
)
