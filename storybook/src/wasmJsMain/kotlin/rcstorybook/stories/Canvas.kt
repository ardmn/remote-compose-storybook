package rcstorybook.stories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteCanvas
import ee.schimke.composeai.rccreation.compose.layout.RemoteColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
import ee.schimke.composeai.rccreation.compose.layout.RemoteOffset
import ee.schimke.composeai.rccreation.compose.layout.RemoteSize
import ee.schimke.composeai.rccreation.compose.layout.RemoteText
import ee.schimke.composeai.rccreation.compose.layout.remoteComponent
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.background
import ee.schimke.composeai.rccreation.compose.modifier.drawWithContent
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxWidth
import ee.schimke.composeai.rccreation.compose.modifier.height
import ee.schimke.composeai.rccreation.compose.modifier.size
import ee.schimke.composeai.rccreation.compose.shaders.RemoteBrush
import ee.schimke.composeai.rccreation.compose.shaders.RemoteShaderBrush
import ee.schimke.composeai.rccreation.compose.shaders.horizontalGradient
import ee.schimke.composeai.rccreation.compose.shaders.image
import ee.schimke.composeai.rccreation.compose.shaders.linearGradient
import ee.schimke.composeai.rccreation.compose.shaders.radialGradient
import ee.schimke.composeai.rccreation.compose.shaders.sweepGradient
import ee.schimke.composeai.rccreation.compose.shaders.verticalGradient
import ee.schimke.composeai.rccreation.compose.shapes.RemoteRoundedCornerShape
import ee.schimke.composeai.rccreation.compose.shapes.clipOutline
import ee.schimke.composeai.rccreation.compose.shapes.drawOutline
import ee.schimke.composeai.rccreation.compose.state.RemoteBlendMode
import ee.schimke.composeai.rccreation.compose.state.RemoteImageBitmap
import ee.schimke.composeai.rccreation.compose.state.RemotePaint
import ee.schimke.composeai.rccreation.compose.state.RemotePaintingStyle
import ee.schimke.composeai.rccreation.compose.state.RemoteStrokeCap
import ee.schimke.composeai.rccreation.compose.state.RemoteStrokeJoin
import ee.schimke.composeai.rccreation.compose.state.RemoteTileMode
import ee.schimke.composeai.rccreation.compose.state.RemoteTimeVariables
import ee.schimke.composeai.rccreation.compose.state.cos
import ee.schimke.composeai.rccreation.compose.state.rc
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.rf
import ee.schimke.composeai.rccreation.compose.state.rs
import ee.schimke.composeai.rccreation.compose.state.sin
import ee.schimke.composeai.rccreation.compose.state.toRemoteDp
import ee.schimke.composeai.rccreation.compose.text.RemoteTypeface

@Composable
@RemoteComposable
fun CanvasShapes(style: RemotePaintingStyle, strokeWidth: Float, color: Color) {
    RemoteCanvas(modifier = RemoteModifier.size(300.rdp, 220.rdp)) {
        val paint = RemotePaint {
            this.color = color.rc
            this.style = style
            this.strokeWidth = strokeWidth.rf
        }
        drawRect(paint, topLeft = RemoteOffset(10f.rf, 10f.rf), size = RemoteSize(80f.rf, 80f.rf))
        drawRoundRect(
            paint,
            topLeft = RemoteOffset(110f.rf, 10f.rf),
            size = RemoteSize(80f.rf, 80f.rf),
            cornerRadius = RemoteOffset(20f.rf, 20f.rf),
        )
        drawCircle(paint, radius = 40f.rf, center = RemoteOffset(250f.rf, 50f.rf))
        drawOval(paint, topLeft = RemoteOffset(10f.rf, 120f.rf), size = RemoteSize(120f.rf, 70f.rf))
        drawArc(
            paint,
            startAngle = 0f.rf,
            sweepAngle = 270f.rf,
            useCenter = true,
            topLeft = RemoteOffset(150f.rf, 115f.rf),
            size = RemoteSize(80f.rf, 80f.rf),
        )
        drawLine(
            paint,
            start = RemoteOffset(250f.rf, 110f.rf),
            end = RemoteOffset(290f.rf, 200f.rf),
        )
    }
}

@Composable
@RemoteComposable
fun CanvasStrokes(strokeWidth: Float, cap: RemoteStrokeCap, join: RemoteStrokeJoin) {
    RemoteCanvas(modifier = RemoteModifier.size(300.rdp, 200.rdp)) {
        val paint = RemotePaint {
            color = Color(0xFF3949AB).rc
            style = RemotePaintingStyle.Stroke
            this.strokeWidth = strokeWidth.rf
            strokeCap = cap
            strokeJoin = join
        }
        drawLine(paint, RemoteOffset(30f.rf, 40f.rf), RemoteOffset(270f.rf, 40f.rf))
        drawRect(paint, topLeft = RemoteOffset(40f.rf, 90f.rf), size = RemoteSize(220f.rf, 80f.rf))
    }
}

@Composable
@RemoteComposable
fun CanvasUsePaint(color: Color, blendMode: RemoteBlendMode) {
    RemoteCanvas(modifier = RemoteModifier.size(260.rdp, 200.rdp)) {
        drawCircle(
            RemotePaint { this.color = Color(0xFF1E88E5).rc },
            radius = 70f.rf,
            center = RemoteOffset(100f.rf, 100f.rf),
        )
        // usePaint makes a paint current for every draw in the block that passes no paint.
        usePaint(
            RemotePaint {
                this.color = color.rc
                this.blendMode = blendMode
            },
        ) {
            drawCircle(null, radius = 70f.rf, center = RemoteOffset(160f.rf, 100f.rf))
        }
    }
}

@Composable
@RemoteComposable
fun CanvasAnimated(speed: Float, amplitude: Float) {
    // RemoteTimeVariables are evaluated by the player every frame: the document never changes.
    RemoteCanvas(modifier = RemoteModifier.size(300.rdp, 200.rdp)) {
        val t = RemoteTimeVariables.continuousSeconds * speed.rf
        val x = 150f.rf + cos(t) * amplitude.rf
        val y = 100f.rf + sin(t * 2f.rf) * (amplitude / 2f).rf
        drawCircle(
            RemotePaint {
                color = Color(0x558E24AA).rc
                style = RemotePaintingStyle.Stroke
                strokeWidth = 2f.rf
            },
            radius = amplitude.rf,
            center = RemoteOffset(150f.rf, 100f.rf),
        )
        drawCircle(
            RemotePaint { color = Color(0xFF8E24AA).rc },
            radius = 18f.rf,
            center = RemoteOffset(x, y),
        )
    }
}

@Composable
@RemoteComposable
fun CanvasOutline(corner: Int, color: Color) {
    RemoteCanvas(modifier = RemoteModifier.size(260.rdp, 180.rdp)) {
        // A shape turns into an outline for the canvas's size, which drawOutline paints…
        val outline =
            RemoteRoundedCornerShape(corner.rdp).createOutline(size, layoutDirection, remoteDensity)
        drawOutline(outline, RemotePaint { this.color = color.rc })
        // …and clipOutline clips the drawing in its block.
        clipOutline(outline) {
            drawCircle(
                RemotePaint { this.color = Color(0x88FFFFFF).rc },
                radius = height / 2f.rf,
                center = RemoteOffset(0f.rf, 0f.rf),
            )
        }
    }
}

@Composable
@RemoteComposable
fun CanvasGradients(kind: String, first: Color, second: Color) {
    RemoteCanvas(modifier = RemoteModifier.size(280.rdp, 180.rdp)) {
        val colors = listOf(first.rc, second.rc)
        val brush: RemoteShaderBrush =
            when (kind) {
                "linear" ->
                    RemoteBrush.linearGradient(
                        colors,
                        start = RemoteOffset.Zero,
                        end = RemoteOffset(width, height),
                    )
                "horizontal" -> RemoteBrush.horizontalGradient(colors)
                "vertical" -> RemoteBrush.verticalGradient(colors)
                "radial" ->
                    RemoteBrush.radialGradient(colors, center = center, radius = height / 2f.rf)
                else -> RemoteBrush.sweepGradient(colors, center = center)
            }
        val paint = RemotePaint()
        with(brush) { applyTo(paint, size) }
        drawRoundRect(paint, size = size, cornerRadius = RemoteOffset(24f.rf, 24f.rf))
    }
}

@Composable
@RemoteComposable
fun DrawWithContent(ringColor: Color, ringScale: Float, behind: Boolean) {
    RemoteBox(
        modifier =
            RemoteModifier.size(200.rdp).drawWithContent {
                val ring = RemotePaint { color = ringColor.rc }
                if (behind) {
                    drawCircle(ring, radius = width * (ringScale / 2f).rf, center = center)
                    drawContent()
                } else {
                    drawContent()
                    drawCircle(ring, radius = width * (ringScale / 2f).rf, center = center)
                }
            },
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteBox(
            RemoteModifier.size(100.rdp).background(Color(0xFF263238).rc),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("content".rs, color = Color.White.rc)
        }
    }
}

@Composable
@RemoteComposable
fun ComponentSizeExpression(fraction: Float, parentHeight: Int) {
    RemoteColumn(
        modifier = RemoteModifier.size(220.rdp, parentHeight.rdp).background(Color(0xFFF1F3F4).rc),
    ) {
        // remoteComponent() reads the enclosing component's measured size at play time.
        val parent = remoteComponent()
        RemoteBox(
            modifier =
                RemoteModifier.fillMaxWidth()
                    .height((parent.height * fraction.rf).toRemoteDp())
                    .background(Color(0xFF1E88E5).rc),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("parent.height × $fraction".rs, color = Color.White.rc)
        }
    }
}

@Composable
@RemoteComposable
fun CanvasText(text: String, textSize: Float, color: Color, typeface: RemoteTypeface) {
    RemoteCanvas(modifier = RemoteModifier.size(300.rdp, 120.rdp)) {
        val paint = RemotePaint {
            this.color = color.rc
            this.textSize = textSize.rf
            this.typeface = typeface
        }
        // x, y is the start of the baseline.
        drawText(text.rs, 16f.rf, 70f.rf, paint)
        drawLine(
            RemotePaint {
                this.color = Color(0x55000000).rc
                strokeWidth = 1f.rf
            },
            RemoteOffset(0f.rf, 70f.rf),
            RemoteOffset(width, 70f.rf),
        )
    }
}

@Composable
@RemoteComposable
fun CanvasTransforms(degrees: Float, scale: Float, translateX: Float) {
    RemoteCanvas(modifier = RemoteModifier.size(300.rdp, 240.rdp)) {
        drawRect(
            RemotePaint {
                color = Color(0x22000000).rc
                style = RemotePaintingStyle.Stroke
                strokeWidth = 1f.rf
            },
        )
        val pivot = RemoteOffset(150f.rf, 120f.rf)
        val fill = RemotePaint { color = Color(0xFFF4511E).rc }
        // Each transform applies to the draws inside its block, innermost first.
        translate(translateX.rf, 0f.rf) {
            rotate(degrees.rf, pivot) {
                scale(scale.rf, scale.rf, pivot) {
                    drawRect(
                        fill,
                        topLeft = RemoteOffset(100f.rf, 70f.rf),
                        size = RemoteSize(100f.rf, 100f.rf),
                    )
                }
            }
        }
    }
}

@Composable
@RemoteComposable
fun CanvasClipAndTransform(degrees: Float, inset: Float) {
    RemoteCanvas(modifier = RemoteModifier.size(300.rdp, 220.rdp)) {
        val stripe = RemotePaint { color = Color(0xFF3949AB).rc }
        // clipRect limits the block's drawing to a rectangle.
        clipRect(inset.rf, inset.rf, width - inset.rf, height - inset.rf) {
            // withTransform applies several transforms to one block.
            withTransform({ rotate(degrees.rf, center) }) {
                for (i in 0..12) {
                    drawRect(
                        stripe,
                        topLeft = RemoteOffset((i * 40f - 120f).rf, (-200f).rf),
                        size = RemoteSize(18f.rf, 600f.rf),
                    )
                }
            }
        }
    }
}

@Composable
@RemoteComposable
fun CanvasImage(x: Float, y: Float) {
    val image = remember {
        val bitmap = ImageBitmap(96, 96)
        Canvas(bitmap).apply {
            drawRect(0f, 0f, 96f, 96f, Paint().apply { color = Color(0xFF43A047) })
            drawCircle(Offset(48f, 48f), 30f, Paint().apply { color = Color(0xFFFFFFFF) })
        }
        RemoteImageBitmap(bitmap)
    }
    RemoteCanvas(modifier = RemoteModifier.size(300.rdp, 200.rdp)) {
        // drawImage places an embedded bitmap at a canvas position.
        drawImage(image, topLeft = RemoteOffset(x.rf, y.rf))
    }
}

@Composable
@RemoteComposable
fun CanvasImageBrush(tileMode: RemoteTileMode) {
    val tile = remember {
        val bitmap = ImageBitmap(40, 40)
        Canvas(bitmap).apply {
            drawRect(0f, 0f, 40f, 40f, Paint().apply { color = Color(0xFFFFF3E0) })
            drawCircle(Offset(10f, 10f), 9f, Paint().apply { color = Color(0xFFFB8C00) })
        }
        RemoteImageBitmap(bitmap)
    }
    RemoteCanvas(modifier = RemoteModifier.size(280.rdp, 180.rdp)) {
        // An image brush fills a shape with a bitmap; the tile mode decides what happens past its
        // edges.
        val brush = RemoteBrush.image(tile, tileModeX = tileMode, tileModeY = tileMode)
        val paint = RemotePaint()
        with(brush) { applyTo(paint, size) }
        drawRoundRect(paint, size = size, cornerRadius = RemoteOffset(24f.rf, 24f.rf))
    }
}
