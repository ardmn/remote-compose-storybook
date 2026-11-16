package rcstorybook.catalog

import ee.schimke.composeai.rccreation.compose.layout.RemoteContentScale
import ee.schimke.composeai.rccreation.compose.modifier.RemoteEnterTransition
import ee.schimke.composeai.rccreation.compose.modifier.RemoteExitTransition
import ee.schimke.composeai.rccreation.compose.shapes.RemoteCircleShape
import ee.schimke.composeai.rccreation.compose.shapes.RemoteRectangleShape
import ee.schimke.composeai.rccreation.compose.shapes.RemoteRoundedCornerShape
import ee.schimke.composeai.rccreation.compose.shapes.RemoteShape
import ee.schimke.composeai.rccreation.compose.state.RemoteBlendMode
import ee.schimke.composeai.rccreation.compose.state.RemoteEasing
import ee.schimke.composeai.rccreation.compose.state.RemotePaintingStyle
import ee.schimke.composeai.rccreation.compose.state.RemoteStrokeCap
import ee.schimke.composeai.rccreation.compose.state.RemoteStrokeJoin
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.text.RemoteFontStyle
import ee.schimke.composeai.rccreation.compose.text.RemoteFontWeight
import ee.schimke.composeai.rccreation.compose.text.RemoteTextAlign
import ee.schimke.composeai.rccreation.compose.text.RemoteTextDecoration
import ee.schimke.composeai.rccreation.compose.text.RemoteTextOverflow
import ee.schimke.composeai.rccreation.compose.text.RemoteTypeface
import rcstorybook.stories.Screen

internal object More {
    val fontWeights = listOf(
        option("Thin", "RemoteFontWeight.Thin", RemoteFontWeight.Thin),
        option("Light", "RemoteFontWeight.Light", RemoteFontWeight.Light),
        option("Normal", "RemoteFontWeight.Normal", RemoteFontWeight.Normal),
        option("Medium", "RemoteFontWeight.Medium", RemoteFontWeight.Medium),
        option("SemiBold", "RemoteFontWeight.SemiBold", RemoteFontWeight.SemiBold),
        option("Bold", "RemoteFontWeight.Bold", RemoteFontWeight.Bold),
        option("ExtraBold", "RemoteFontWeight.ExtraBold", RemoteFontWeight.ExtraBold),
        option("Black", "RemoteFontWeight.Black", RemoteFontWeight.Black),
    )
    val fontStyles = listOf(
        option("Normal", "RemoteFontStyle.Normal", RemoteFontStyle.Normal),
        option("Italic", "RemoteFontStyle.Italic", RemoteFontStyle.Italic),
    )
    val overflows = listOf(
        option("Clip", "RemoteTextOverflow.Clip", RemoteTextOverflow.Clip),
        option("Ellipsis", "RemoteTextOverflow.Ellipsis", RemoteTextOverflow.Ellipsis),
        option("StartEllipsis", "RemoteTextOverflow.StartEllipsis", RemoteTextOverflow.StartEllipsis),
        option("MiddleEllipsis", "RemoteTextOverflow.MiddleEllipsis", RemoteTextOverflow.MiddleEllipsis),
        option("Visible", "RemoteTextOverflow.Visible", RemoteTextOverflow.Visible),
    )
    val textAligns = listOf(
        option("Start", "RemoteTextAlign.Start", RemoteTextAlign.Start),
        option("Center", "RemoteTextAlign.Center", RemoteTextAlign.Center),
        option("End", "RemoteTextAlign.End", RemoteTextAlign.End),
        option("Left", "RemoteTextAlign.Left", RemoteTextAlign.Left),
        option("Right", "RemoteTextAlign.Right", RemoteTextAlign.Right),
        option("Justify", "RemoteTextAlign.Justify", RemoteTextAlign.Justify),
        option("Unspecified", "RemoteTextAlign.Unspecified", RemoteTextAlign.Unspecified),
    )
    val decorations = listOf(
        option("None", "RemoteTextDecoration.None", RemoteTextDecoration.None),
        option("Underline", "RemoteTextDecoration.Underline", RemoteTextDecoration.Underline),
        option("LineThrough", "RemoteTextDecoration.LineThrough", RemoteTextDecoration.LineThrough),
        option("Both", "RemoteTextDecoration.Underline + RemoteTextDecoration.LineThrough", RemoteTextDecoration.Underline + RemoteTextDecoration.LineThrough),
    )
    val contentScales = listOf(
        option("Fit", "RemoteContentScale.Fit", RemoteContentScale.Fit),
        option("Crop", "RemoteContentScale.Crop", RemoteContentScale.Crop),
        option("FillBounds", "RemoteContentScale.FillBounds", RemoteContentScale.FillBounds),
        option("FillWidth", "RemoteContentScale.FillWidth", RemoteContentScale.FillWidth),
        option("FillHeight", "RemoteContentScale.FillHeight", RemoteContentScale.FillHeight),
        option("Inside", "RemoteContentScale.Inside", RemoteContentScale.Inside),
        option("None", "RemoteContentScale.None", RemoteContentScale.None),
    )
    val paintingStyles = listOf(
        option("Fill", "RemotePaintingStyle.Fill", RemotePaintingStyle.Fill),
        option("Stroke", "RemotePaintingStyle.Stroke", RemotePaintingStyle.Stroke),
    )
    val strokeCaps = listOf(
        option("Butt", "RemoteStrokeCap.Butt", RemoteStrokeCap.Butt),
        option("Round", "RemoteStrokeCap.Round", RemoteStrokeCap.Round),
        option("Square", "RemoteStrokeCap.Square", RemoteStrokeCap.Square),
    )
    val strokeJoins = listOf(
        option("Miter", "RemoteStrokeJoin.Miter", RemoteStrokeJoin.Miter),
        option("Round", "RemoteStrokeJoin.Round", RemoteStrokeJoin.Round),
        option("Bevel", "RemoteStrokeJoin.Bevel", RemoteStrokeJoin.Bevel),
    )
    val blendModes = listOf(
        "SrcOver", "Multiply", "Screen", "Overlay", "Darken", "Lighten", "ColorDodge", "ColorBurn", "Hardlight",
        "Softlight", "Difference", "Exclusion", "Hue", "Saturation", "Color", "Luminosity", "Xor", "Plus",
        "Modulate", "Src", "Dst", "SrcIn", "DstIn", "SrcOut", "DstOut", "SrcAtop", "DstAtop", "DstOver", "Clear",
    ).map { name -> option(name, "RemoteBlendMode.$name", blendMode(name)) }

    private fun blendMode(name: String): RemoteBlendMode = when (name) {
        "SrcOver" -> RemoteBlendMode.SrcOver; "Multiply" -> RemoteBlendMode.Multiply; "Screen" -> RemoteBlendMode.Screen
        "Overlay" -> RemoteBlendMode.Overlay; "Darken" -> RemoteBlendMode.Darken; "Lighten" -> RemoteBlendMode.Lighten
        "ColorDodge" -> RemoteBlendMode.ColorDodge; "ColorBurn" -> RemoteBlendMode.ColorBurn; "Hardlight" -> RemoteBlendMode.Hardlight
        "Softlight" -> RemoteBlendMode.Softlight; "Difference" -> RemoteBlendMode.Difference; "Exclusion" -> RemoteBlendMode.Exclusion
        "Hue" -> RemoteBlendMode.Hue; "Saturation" -> RemoteBlendMode.Saturation; "Color" -> RemoteBlendMode.Color
        "Luminosity" -> RemoteBlendMode.Luminosity; "Xor" -> RemoteBlendMode.Xor; "Plus" -> RemoteBlendMode.Plus
        "Modulate" -> RemoteBlendMode.Modulate; "Src" -> RemoteBlendMode.Src; "Dst" -> RemoteBlendMode.Dst
        "SrcIn" -> RemoteBlendMode.SrcIn; "DstIn" -> RemoteBlendMode.DstIn; "SrcOut" -> RemoteBlendMode.SrcOut
        "DstOut" -> RemoteBlendMode.DstOut; "SrcAtop" -> RemoteBlendMode.SrcAtop; "DstAtop" -> RemoteBlendMode.DstAtop
        "DstOver" -> RemoteBlendMode.DstOver; else -> RemoteBlendMode.Clear
    }
    val shapes = listOf<Option<RemoteShape>>(
        option("RoundedCorner 16dp", "RemoteRoundedCornerShape(16.rdp)", RemoteRoundedCornerShape(16.rdp)),
        option("RoundedCorner 50%", "RemoteRoundedCornerShape(50)", RemoteRoundedCornerShape(50)),
        option("Cut top corners", "RemoteRoundedCornerShape(topStart = 32.rdp, topEnd = 32.rdp)", RemoteRoundedCornerShape(topStart = 32.rdp, topEnd = 32.rdp)),
        option("Circle", "RemoteCircleShape", RemoteCircleShape),
        option("Rectangle", "RemoteRectangleShape", RemoteRectangleShape),
    )
    val enters = listOf(
        option("FadeIn", "RemoteEnterTransition.FadeIn", RemoteEnterTransition.FadeIn),
        option("SlideInTop", "RemoteEnterTransition.SlideInTop", RemoteEnterTransition.SlideInTop),
        option("SlideInBottom", "RemoteEnterTransition.SlideInBottom", RemoteEnterTransition.SlideInBottom),
        option("SlideInLeft", "RemoteEnterTransition.SlideInLeft", RemoteEnterTransition.SlideInLeft),
        option("SlideInRight", "RemoteEnterTransition.SlideInRight", RemoteEnterTransition.SlideInRight),
        option("Rotate", "RemoteEnterTransition.Rotate", RemoteEnterTransition.Rotate),
        option("Particle", "RemoteEnterTransition.Particle", RemoteEnterTransition.Particle),
    )
    val exits = listOf(
        option("FadeOut", "RemoteExitTransition.FadeOut", RemoteExitTransition.FadeOut),
        option("SlideOutTop", "RemoteExitTransition.SlideOutTop", RemoteExitTransition.SlideOutTop),
        option("SlideOutBottom", "RemoteExitTransition.SlideOutBottom", RemoteExitTransition.SlideOutBottom),
        option("SlideOutLeft", "RemoteExitTransition.SlideOutLeft", RemoteExitTransition.SlideOutLeft),
        option("SlideOutRight", "RemoteExitTransition.SlideOutRight", RemoteExitTransition.SlideOutRight),
        option("Rotate", "RemoteExitTransition.Rotate", RemoteExitTransition.Rotate),
        option("Particle", "RemoteExitTransition.Particle", RemoteExitTransition.Particle),
    )
    val easings = listOf(
        option("Standard", "RemoteEasing.Standard", RemoteEasing.Standard),
        option("Linear", "RemoteEasing.Linear", RemoteEasing.Linear),
        option("Accelerate", "RemoteEasing.Accelerate", RemoteEasing.Accelerate),
        option("Decelerate", "RemoteEasing.Decelerate", RemoteEasing.Decelerate),
        option("Anticipate", "RemoteEasing.Anticipate", RemoteEasing.Anticipate),
        option("Overshoot", "RemoteEasing.Overshoot", RemoteEasing.Overshoot),
        option("Bounce", "RemoteEasing.Bounce", RemoteEasing.Bounce),
        option("Elastic", "RemoteEasing.Elastic", RemoteEasing.Elastic),
    )
    val screens = Screen.entries.map { option(it.name, "Screen.${it.name}", it) }
    val functions = listOf(
        "sin", "cos", "tan", "asin", "acos", "atan", "atan2", "abs", "sign", "copySign", "sqrt", "cbrt", "pow",
        "exp", "ln", "log", "floor", "ceil", "round", "min", "max", "clamp", "lerp", "mad", "toDeg", "toRad",
    ).map { option(it, "\"$it\"", it) }
    val typefaces = listOf<Option<RemoteTypeface>>(
        option("Default", "RemoteTypeface.Default", RemoteTypeface.Default),
        option("DefaultBold", "RemoteTypeface.DefaultBold", RemoteTypeface.DefaultBold),
        option("SansSerif", "RemoteTypeface.SansSerif", RemoteTypeface.SansSerif),
        option("Serif", "RemoteTypeface.Serif", RemoteTypeface.Serif),
        option("Monospace", "RemoteTypeface.Monospace", RemoteTypeface.Monospace),
    )
    val gradients = listOf("linear", "horizontal", "vertical", "radial", "sweep").map { option(it, "\"$it\"", it) }
    val wrapModes = listOf("size", "width", "height").map { option(it, "\"$it\"", it) }
    val visibilities = listOf(
        option("VISIBLE (1)", "1", 1),
        option("INVISIBLE (2)", "2", 2),
        option("GONE (0)", "0", 0),
    )
}
