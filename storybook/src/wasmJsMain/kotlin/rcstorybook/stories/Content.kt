package rcstorybook.stories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import ee.schimke.composeai.rccreation.compose.capture.RemoteImageVector
import ee.schimke.composeai.rccreation.compose.capture.path
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteArrangement
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
import ee.schimke.composeai.rccreation.compose.layout.RemoteContentScale
import ee.schimke.composeai.rccreation.compose.layout.RemoteImage
import ee.schimke.composeai.rccreation.compose.layout.RemoteRow
import ee.schimke.composeai.rccreation.compose.layout.RemoteText
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.background
import ee.schimke.composeai.rccreation.compose.modifier.border
import ee.schimke.composeai.rccreation.compose.modifier.padding
import ee.schimke.composeai.rccreation.compose.modifier.size
import ee.schimke.composeai.rccreation.compose.modifier.width
import ee.schimke.composeai.rccreation.compose.shaders.RemoteBrush
import ee.schimke.composeai.rccreation.compose.shaders.solidColor
import ee.schimke.composeai.rccreation.compose.state.RemoteColor
import ee.schimke.composeai.rccreation.compose.state.RemoteImageBitmap
import ee.schimke.composeai.rccreation.compose.state.RemotePathFillType
import ee.schimke.composeai.rccreation.compose.state.RemoteStrokeCap
import ee.schimke.composeai.rccreation.compose.state.RemoteStrokeJoin
import ee.schimke.composeai.rccreation.compose.state.rc
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteImageBitmap
import ee.schimke.composeai.rccreation.compose.state.rememberRemoteImageBitmap
import ee.schimke.composeai.rccreation.compose.state.rf
import ee.schimke.composeai.rccreation.compose.state.rs
import ee.schimke.composeai.rccreation.compose.state.rsp
import ee.schimke.composeai.rccreation.compose.text.RemoteFontFamily
import ee.schimke.composeai.rccreation.compose.text.RemoteFontStyle
import ee.schimke.composeai.rccreation.compose.text.RemoteFontVariation
import ee.schimke.composeai.rccreation.compose.text.RemoteFontWeight
import ee.schimke.composeai.rccreation.compose.text.RemoteHyphens
import ee.schimke.composeai.rccreation.compose.text.RemoteLineBreak
import ee.schimke.composeai.rccreation.compose.text.RemoteTextAlign
import ee.schimke.composeai.rccreation.compose.text.RemoteTextDecoration
import ee.schimke.composeai.rccreation.compose.text.RemoteTextOverflow
import ee.schimke.composeai.rccreation.compose.text.RemoteTextStyle
import ee.schimke.composeai.rccreation.foundation.icon.RemoteBasicIcon
import ee.schimke.composeai.rccreation.foundation.image.RemoteBasicImage
import ee.schimke.composeai.rccreation.foundation.text.RemoteBasicText

@Composable
@RemoteComposable
fun TextBasics(
    text: String,
    fontSize: Int,
    color: Color,
    fontWeight: RemoteFontWeight,
    fontStyle: RemoteFontStyle,
) {
    RemoteText(
        text = text.rs,
        fontSize = fontSize.rsp,
        color = color.rc,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
    )
}

@Composable
@RemoteComposable
fun TextOverflowAndAlign(
    maxLines: Int,
    overflow: RemoteTextOverflow,
    textAlign: RemoteTextAlign,
    width: Int,
) {
    RemoteText(
        text =
            ("Remote Compose sends UI as data: a server describes the layout once, " +
                    "and every player draws it without shipping new code.")
                .rs,
        modifier = RemoteModifier.width(width.rdp).background(Color(0xFFF1F3F4).rc).padding(8.rdp),
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
        fontSize = 16.rsp,
    )
}

@Composable
@RemoteComposable
fun TextFontFamilies(sample: String, fontSize: Int) {
    RemoteColumn(verticalArrangement = RemoteArrangement.spacedBy(6.rdp)) {
        RemoteText(
            "Default: $sample".rs,
            fontFamily = RemoteFontFamily.Default,
            fontSize = fontSize.rsp,
        )
        RemoteText(
            "SansSerif: $sample".rs,
            fontFamily = RemoteFontFamily.SansSerif,
            fontSize = fontSize.rsp,
        )
        RemoteText(
            "Serif: $sample".rs,
            fontFamily = RemoteFontFamily.Serif,
            fontSize = fontSize.rsp,
        )
        RemoteText(
            "Monospace: $sample".rs,
            fontFamily = RemoteFontFamily.Monospace,
            fontSize = fontSize.rsp,
        )
        RemoteText(
            "Cursive: $sample".rs,
            fontFamily = RemoteFontFamily.Cursive,
            fontSize = fontSize.rsp,
        )
    }
}

@Composable
@RemoteComposable
fun TextWeights(fontSize: Int) {
    RemoteColumn {
        RemoteText("Thin".rs, fontWeight = RemoteFontWeight.Thin, fontSize = fontSize.rsp)
        RemoteText(
            "ExtraLight".rs,
            fontWeight = RemoteFontWeight.ExtraLight,
            fontSize = fontSize.rsp,
        )
        RemoteText("Light".rs, fontWeight = RemoteFontWeight.Light, fontSize = fontSize.rsp)
        RemoteText("Normal".rs, fontWeight = RemoteFontWeight.Normal, fontSize = fontSize.rsp)
        RemoteText("Medium".rs, fontWeight = RemoteFontWeight.Medium, fontSize = fontSize.rsp)
        RemoteText("SemiBold".rs, fontWeight = RemoteFontWeight.SemiBold, fontSize = fontSize.rsp)
        RemoteText("Bold".rs, fontWeight = RemoteFontWeight.Bold, fontSize = fontSize.rsp)
        RemoteText("ExtraBold".rs, fontWeight = RemoteFontWeight.ExtraBold, fontSize = fontSize.rsp)
        RemoteText("Black".rs, fontWeight = RemoteFontWeight.Black, fontSize = fontSize.rsp)
    }
}

@Composable
@RemoteComposable
fun TextStyled(
    letterSpacing: Float,
    lineHeight: Int,
    decoration: RemoteTextDecoration,
    highlight: Color,
) {
    val style =
        RemoteTextStyle(
            color = Color(0xFF202124).rc,
            fontSize = 18.rsp,
            letterSpacing = letterSpacing.rsp,
            lineHeight = lineHeight.rsp,
            textDecoration = decoration,
            background = highlight.rc,
            lineBreak = RemoteLineBreak.Paragraph,
            hyphens = RemoteHyphens.Auto,
        )
    RemoteText(
        text = "A RemoteTextStyle bundles every text attribute into one value you can reuse.".rs,
        modifier = RemoteModifier.width(300.rdp),
        style = style,
    )
}

@Composable
@RemoteComposable
fun TextFontVariation(weight: Float, width: Float) {
    // Variable font axes; the player applies them when the font it resolves has the axes.
    RemoteText(
        text = "Variable axes".rs,
        fontSize = 32.rsp,
        fontVariationSettings =
            RemoteFontVariation.Settings(
                RemoteFontVariation.weight(weight.rf),
                RemoteFontVariation.width(width.rf),
            ),
    )
}

@Composable
@RemoteComposable
fun BasicText(text: String, fontSize: Int, color: Color, italic: Boolean) {
    // RemoteBasicText (foundation) is the unstyled text primitive the higher-level texts build on.
    RemoteBasicText(
        text = text.rs,
        color = color.rc,
        fontSize = fontSize.rsp,
        fontStyle = if (italic) RemoteFontStyle.Italic else RemoteFontStyle.Normal,
    )
}

@Composable
@RemoteComposable
fun ImageContentScale(contentScale: RemoteContentScale, width: Int, height: Int, alpha: Float) {
    val bitmap = remember {
        val image = ImageBitmap(160, 90)
        Canvas(image).apply {
            drawRect(0f, 0f, 160f, 90f, Paint().apply { color = Color(0xFF1E88E5) })
            drawCircle(Offset(80f, 45f), 30f, Paint().apply { color = Color(0xFFFFCA28) })
            drawRect(0f, 70f, 160f, 90f, Paint().apply { color = Color(0xFF43A047) })
        }
        RemoteImageBitmap(image)
    }
    RemoteImage(
        bitmap = bitmap,
        contentDescription = "A sun over a field".rs,
        modifier = RemoteModifier.size(width.rdp, height.rdp).border(1.rdp, Color(0xFF9AA0A6).rc),
        contentScale = contentScale,
        alpha = alpha.rf,
    )
}

@Composable
@RemoteComposable
fun BasicImage(contentScale: RemoteContentScale, size: Int) {
    val bitmap = remember {
        val image = ImageBitmap(120, 60)
        Canvas(image).apply {
            drawRect(0f, 0f, 120f, 60f, Paint().apply { color = Color(0xFF8E24AA) })
            drawCircle(Offset(60f, 30f), 20f, Paint().apply { color = Color(0xFFFFFFFF) })
        }
        RemoteImageBitmap(image)
    }
    RemoteBasicImage(
        bitmap = bitmap,
        contentDescription = "Foundation image".rs,
        modifier = RemoteModifier.size(size.rdp).background(Color(0xFFF1F3F4).rc),
        contentScale = contentScale,
    )
}

@Composable
@RemoteComposable
fun ImageFromUrl(url: String, named: Boolean) {
    // The document carries the URL; the player fetches the image when it draws.
    // A named bitmap can also be replaced by the host at play time.
    val bitmap =
        if (named) rememberNamedRemoteImageBitmap("hero", url) else rememberRemoteImageBitmap(url)
    RemoteImage(
        bitmap = bitmap,
        contentDescription = "Image loaded from a URL".rs,
        modifier = RemoteModifier.size(240.rdp, 160.rdp),
        contentScale = RemoteContentScale.Crop,
    )
}

@Composable
@RemoteComposable
fun BasicIcon(tint: Color, size: Int, strokeWidth: Float) {
    // A vector icon built from path commands, tinted at play time.
    val heart =
        RemoteImageVector.Builder(
                viewportWidth = 24f.rf,
                viewportHeight = 24f.rf,
                tintColor = Color.White.rc,
                name = "Heart",
            )
            .path(
                fill = RemoteBrush.solidColor(Color.White.rc),
                stroke = RemoteBrush.solidColor(Color.White.rc),
                strokeLineWidth = strokeWidth.rf,
                strokeLineCap = RemoteStrokeCap.Round,
                strokeLineJoin = RemoteStrokeJoin.Round,
            ) {
                moveTo(12f.rf, 21f.rf)
                cubicTo(5f.rf, 15f.rf, 2f.rf, 11f.rf, 2f.rf, 7.5f.rf)
                cubicTo(2f.rf, 4.5f.rf, 4.5f.rf, 2f.rf, 7.5f.rf, 2f.rf)
                cubicTo(9.3f.rf, 2f.rf, 11f.rf, 3f.rf, 12f.rf, 4.5f.rf)
                cubicTo(13f.rf, 3f.rf, 14.7f.rf, 2f.rf, 16.5f.rf, 2f.rf)
                cubicTo(19.5f.rf, 2f.rf, 22f.rf, 4.5f.rf, 22f.rf, 7.5f.rf)
                cubicTo(22f.rf, 11f.rf, 19f.rf, 15f.rf, 12f.rf, 21f.rf)
                close()
            }
            .build()
    RemoteRow(
        verticalAlignment = RemoteAlignment.CenterVertically,
        horizontalArrangement = RemoteArrangement.spacedBy(12.rdp),
    ) {
        RemoteBasicIcon(
            imageVector = heart,
            contentDescription = "Favourite".rs,
            modifier = RemoteModifier.size(size.rdp),
            tint = tint.rc,
        )
        RemoteBox(contentAlignment = RemoteAlignment.Center) { RemoteText("RemoteBasicIcon".rs) }
    }
}

@Composable
@RemoteComposable
fun BasicIconFillType(fillType: RemotePathFillType, tint: Color) {
    // Two squares drawn in the same direction: NonZero fills where they overlap, EvenOdd leaves a
    // hole there.
    val frame =
        RemoteImageVector.Builder(
                viewportWidth = 24f.rf,
                viewportHeight = 24f.rf,
                tintColor = RemoteColor.White,
                name = "Frame",
            )
            .path(fill = RemoteBrush.solidColor(RemoteColor.White), pathFillType = fillType) {
                moveTo(2f.rf, 2f.rf)
                lineTo(22f.rf, 2f.rf)
                lineTo(22f.rf, 22f.rf)
                lineTo(2f.rf, 22f.rf)
                close()
                moveTo(7f.rf, 7f.rf)
                lineTo(17f.rf, 7f.rf)
                lineTo(17f.rf, 17f.rf)
                lineTo(7f.rf, 17f.rf)
                close()
            }
            .build()
    RemoteBasicIcon(
        imageVector = frame,
        contentDescription = "Frame".rs,
        modifier = RemoteModifier.size(120.rdp),
        tint = tint.rc,
    )
}
