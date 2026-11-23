package rcstorybook.stories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteArrangement
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
import ee.schimke.composeai.rccreation.compose.layout.RemotePaddingValues
import ee.schimke.composeai.rccreation.compose.layout.RemoteRow
import ee.schimke.composeai.rccreation.compose.layout.RemoteText
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.alignByBaseline
import ee.schimke.composeai.rccreation.compose.modifier.alpha
import ee.schimke.composeai.rccreation.compose.modifier.background
import ee.schimke.composeai.rccreation.compose.modifier.border
import ee.schimke.composeai.rccreation.compose.modifier.clip
import ee.schimke.composeai.rccreation.compose.modifier.defaultMinSize
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxHeight
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxSize
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxWidth
import ee.schimke.composeai.rccreation.compose.modifier.fillParentMaxHeight
import ee.schimke.composeai.rccreation.compose.modifier.fillParentMaxWidth
import ee.schimke.composeai.rccreation.compose.modifier.graphicsLayer
import ee.schimke.composeai.rccreation.compose.modifier.height
import ee.schimke.composeai.rccreation.compose.modifier.heightIn
import ee.schimke.composeai.rccreation.compose.modifier.horizontalScroll
import ee.schimke.composeai.rccreation.compose.modifier.offset
import ee.schimke.composeai.rccreation.compose.modifier.padding
import ee.schimke.composeai.rccreation.compose.modifier.rememberRemoteScrollState
import ee.schimke.composeai.rccreation.compose.modifier.rotate
import ee.schimke.composeai.rccreation.compose.modifier.scale
import ee.schimke.composeai.rccreation.compose.modifier.size
import ee.schimke.composeai.rccreation.compose.modifier.verticalScroll
import ee.schimke.composeai.rccreation.compose.modifier.visibility
import ee.schimke.composeai.rccreation.compose.modifier.width
import ee.schimke.composeai.rccreation.compose.modifier.widthIn
import ee.schimke.composeai.rccreation.compose.modifier.wrapContentHeight
import ee.schimke.composeai.rccreation.compose.modifier.wrapContentSize
import ee.schimke.composeai.rccreation.compose.modifier.wrapContentWidth
import ee.schimke.composeai.rccreation.compose.modifier.zIndex
import ee.schimke.composeai.rccreation.compose.painter.painterRemoteColor
import ee.schimke.composeai.rccreation.compose.painter.painterRemoteImageBitmap
import ee.schimke.composeai.rccreation.compose.shapes.RemoteCornerSize
import ee.schimke.composeai.rccreation.compose.shapes.RemoteRoundedCornerShape
import ee.schimke.composeai.rccreation.compose.shapes.RemoteShape
import ee.schimke.composeai.rccreation.compose.state.RemoteCompositingStrategy
import ee.schimke.composeai.rccreation.compose.state.RemoteImageBitmap
import ee.schimke.composeai.rccreation.compose.state.RemoteInt
import ee.schimke.composeai.rccreation.compose.state.rc
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.rf
import ee.schimke.composeai.rccreation.compose.state.rs
import ee.schimke.composeai.rccreation.compose.state.rsp

@Composable
@RemoteComposable
fun SizeModifiers(width: Int, height: Int) {
    RemoteBox(
        RemoteModifier.size(width.rdp, height.rdp).background(Color(0xFF42A5F5).rc),
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteText("size($width, $height)".rs, color = Color.White.rc)
    }
}

@Composable
@RemoteComposable
fun FillFractions(widthFraction: Float, heightFraction: Float) {
    RemoteBox(RemoteModifier.size(300.rdp, 240.rdp).background(Color(0xFFF1F3F4).rc)) {
        RemoteBox(
            RemoteModifier.fillMaxWidth(widthFraction.rf)
                .fillMaxHeight(heightFraction.rf)
                .background(Color(0xFF7E57C2).rc),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("fillMax".rs, color = Color.White.rc)
        }
    }
}

@Composable
@RemoteComposable
fun FillMaxSizeInParent(fraction: Float) {
    RemoteColumn(RemoteModifier.size(300.rdp, 220.rdp).background(Color(0xFFF1F3F4).rc)) {
        // fillParentMax* sizes against the parent even inside a scrolling or weighted container.
        RemoteBox(
            RemoteModifier.fillParentMaxWidth(fraction.rf)
                .height(40.rdp)
                .background(Color(0xFF26A69A).rc),
        )
        RemoteBox(
            RemoteModifier.width(60.rdp)
                .fillParentMaxHeight((fraction / 2f).rf)
                .background(Color(0xFFFFA726).rc),
        )
        RemoteBox(RemoteModifier.fillMaxSize(0.5f.rf).background(Color(0x5542A5F5).rc))
    }
}

@Composable
@RemoteComposable
fun SizeConstraints(minWidth: Int, maxWidth: Int, minHeight: Int, text: String) {
    RemoteColumn(verticalArrangement = RemoteArrangement.spacedBy(12.rdp)) {
        // widthIn / heightIn bound the size the content asks for.
        RemoteText(
            text.rs,
            modifier =
                RemoteModifier.widthIn(min = minWidth.rdp, max = maxWidth.rdp)
                    .heightIn(min = minHeight.rdp)
                    .background(Color(0xFFFFE0B2).rc)
                    .padding(8.rdp),
        )
        // defaultMinSize applies only when nothing else constrains the size.
        RemoteBox(
            RemoteModifier.defaultMinSize(minWidth = minWidth.rdp, minHeight = minHeight.rdp)
                .background(Color(0xFFEF6C00).rc),
        )
    }
}

@Composable
@RemoteComposable
fun WrapContent(mode: String) {
    RemoteBox(RemoteModifier.size(280.rdp, 160.rdp).background(Color(0xFFF1F3F4).rc)) {
        val modifier =
            when (mode) {
                "width" -> RemoteModifier.fillMaxSize().wrapContentWidth()
                "height" -> RemoteModifier.fillMaxSize().wrapContentHeight()
                else -> RemoteModifier.fillMaxSize().wrapContentSize()
            }
        RemoteText(
            "wrapContent: $mode".rs,
            modifier = modifier.background(Color(0xFF5C6BC0).rc).padding(8.rdp),
            color = Color.White.rc,
        )
    }
}

@Composable
@RemoteComposable
fun PaddingVariants(start: Int, top: Int, end: Int, bottom: Int) {
    RemoteColumn(verticalArrangement = RemoteArrangement.spacedBy(10.rdp)) {
        RemoteBox(
            RemoteModifier.background(Color(0xFFBBDEFB).rc)
                .padding(start = start.rdp, top = top.rdp, end = end.rdp, bottom = bottom.rdp),
        ) {
            RemoteBox(RemoteModifier.size(120.rdp, 40.rdp).background(Color(0xFF1E88E5).rc))
        }
        RemoteBox(
            RemoteModifier.background(Color(0xFFC8E6C9).rc)
                .padding(horizontal = start.rdp, vertical = top.rdp),
        ) {
            RemoteBox(RemoteModifier.size(120.rdp, 40.rdp).background(Color(0xFF43A047).rc))
        }
        RemoteBox(
            RemoteModifier.background(Color(0xFFFFE0B2).rc).padding(RemotePaddingValues(start.rdp)),
        ) {
            RemoteBox(RemoteModifier.size(120.rdp, 40.rdp).background(Color(0xFFFB8C00).rc))
        }
    }
}

@Composable
@RemoteComposable
fun OffsetAndZIndex(x: Int, y: Int, frontOnTop: Boolean) {
    RemoteBox(RemoteModifier.size(260.rdp, 200.rdp)) {
        RemoteBox(
            RemoteModifier.zIndex(if (frontOnTop) 1f.rf else 0f.rf)
                .size(120.rdp)
                .background(Color(0xFFE53935).rc),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("A".rs, color = Color.White.rc, fontSize = 24.rsp)
        }
        // offset moves the drawing without changing the layout; zIndex orders siblings.
        RemoteBox(
            RemoteModifier.offset(x.rdp, y.rdp)
                .zIndex(if (frontOnTop) 0f.rf else 1f.rf)
                .size(120.rdp)
                .background(Color(0xFF1E88E5).rc),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("B".rs, color = Color.White.rc, fontSize = 24.rsp)
        }
    }
}

@Composable
@RemoteComposable
fun BackgroundBorderClip(shape: RemoteShape, borderWidth: Int, borderColor: Color, fill: Color) {
    RemoteBox(
        RemoteModifier.size(180.rdp, 120.rdp)
            .border(borderWidth.rdp, borderColor.rc, shape)
            .clip(shape)
            .background(fill.rc),
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteText("clip + border".rs)
    }
}

@Composable
@RemoteComposable
fun CornerShapes(topStart: Int, topEnd: Int, bottomEnd: Int, bottomStart: Int) {
    RemoteRow(horizontalArrangement = RemoteArrangement.spacedBy(12.rdp)) {
        // Per-corner sizes in dp…
        RemoteBox(
            RemoteModifier.size(110.rdp)
                .clip(
                    RemoteRoundedCornerShape(
                        topStart.rdp,
                        topEnd.rdp,
                        bottomEnd.rdp,
                        bottomStart.rdp,
                    ),
                )
                .background(Color(0xFF00897B).rc),
        )
        // …or as a percentage of the shorter side, through RemoteCornerSize.
        RemoteBox(
            RemoteModifier.size(110.rdp)
                .clip(RemoteRoundedCornerShape(RemoteCornerSize(topStart.coerceAtMost(50))))
                .background(Color(0xFF5E35B1).rc),
        )
    }
}

@Composable
@RemoteComposable
fun PainterBackground(color: Color, useImage: Boolean) {
    val pattern = remember {
        val bitmap = ImageBitmap(40, 40)
        Canvas(bitmap).apply {
            drawRect(0f, 0f, 40f, 40f, Paint().apply { this.color = Color(0xFFFFF3E0) })
            drawCircle(Offset(20f, 20f), 10f, Paint().apply { this.color = Color(0xFFFF7043) })
        }
        RemoteImageBitmap(bitmap)
    }
    // background() also takes a RemotePainter: a colour or a bitmap.
    val painter = if (useImage) painterRemoteImageBitmap(pattern) else painterRemoteColor(color.rc)
    RemoteBox(RemoteModifier.size(200.rdp, 120.rdp).background(painter))
}

@Composable
@RemoteComposable
fun AlphaRotateScale(alpha: Float, degrees: Float, scale: Float) {
    RemoteBox(RemoteModifier.size(240.rdp), contentAlignment = RemoteAlignment.Center) {
        // One transform per component: a player keeps a single graphics layer per component
        // (AndroidX's applies the last of alpha/rotate/scale, this web player refuses more than
        // one).
        RemoteBox(RemoteModifier.alpha(alpha.rf)) {
            RemoteBox(RemoteModifier.rotate(degrees.rf)) {
                RemoteBox(
                    RemoteModifier.scale(scale.rf).size(110.rdp).background(Color(0xFFD81B60).rc),
                    contentAlignment = RemoteAlignment.Center,
                ) {
                    RemoteText("Remote".rs, color = Color.White.rc)
                }
            }
        }
    }
}

@Composable
@RemoteComposable
fun GraphicsLayer(
    rotationX: Float,
    rotationY: Float,
    rotationZ: Float,
    scaleX: Float,
    scaleY: Float,
    shadowElevation: Float,
) {
    RemoteBox(RemoteModifier.size(260.rdp), contentAlignment = RemoteAlignment.Center) {
        RemoteBox(
            RemoteModifier.graphicsLayer(
                    rotationX = rotationX.rf,
                    rotationY = rotationY.rf,
                    rotationZ = rotationZ.rf,
                    scaleX = scaleX.rf,
                    scaleY = scaleY.rf,
                    shadowElevation = shadowElevation.rf,
                )
                .size(140.rdp, 90.rdp)
                .background(Color(0xFF3949AB).rc),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("graphicsLayer".rs, color = Color.White.rc)
        }
    }
}

@Composable
@RemoteComposable
fun GraphicsLayerBlock(translationX: Float, translationY: Float, alpha: Float) {
    RemoteBox(RemoteModifier.size(260.rdp), contentAlignment = RemoteAlignment.Center) {
        RemoteBox(
            // The block form sets the same properties on a RemoteGraphicsLayerScope.
            RemoteModifier.graphicsLayer {
                    this.translationX = translationX.rf
                    this.translationY = translationY.rf
                    this.alpha = alpha.rf
                }
                .size(120.rdp)
                .background(Color(0xFF00ACC1).rc),
        )
    }
}

@Composable
@RemoteComposable
fun Visibility(visibility: Int) {
    RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically) {
        RemoteBox(RemoteModifier.size(60.rdp).background(Color(0xFF90A4AE).rc))
        // 1 = visible, 2 = invisible (keeps its space), 0 = gone (takes no space).
        RemoteBox(
            RemoteModifier.visibility(RemoteInt(visibility))
                .size(60.rdp)
                .background(Color(0xFFE53935).rc),
        )
        RemoteBox(RemoteModifier.size(60.rdp).background(Color(0xFF90A4AE).rc))
    }
}

@Composable
@RemoteComposable
fun VerticalScroll(itemCount: Int, notches: Int) {
    val scrollState = rememberRemoteScrollState(notches)
    RemoteColumn(
        RemoteModifier.size(240.rdp, 260.rdp)
            .background(Color(0xFFF1F3F4).rc)
            .verticalScroll(scrollState),
    ) {
        for (i in 1..itemCount) {
            RemoteBox(
                RemoteModifier.fillMaxWidth()
                    .height(56.rdp)
                    .padding(4.rdp)
                    .background(Color(0xFF26A69A).rc),
                contentAlignment = RemoteAlignment.Center,
            ) {
                RemoteText("Item $i".rs, color = Color.White.rc)
            }
        }
    }
}

@Composable
@RemoteComposable
fun HorizontalScroll(itemCount: Int) {
    val scrollState = rememberRemoteScrollState()
    RemoteRow(
        RemoteModifier.size(300.rdp, 100.rdp)
            .background(Color(0xFFF1F3F4).rc)
            .horizontalScroll(scrollState),
        verticalAlignment = RemoteAlignment.CenterVertically,
    ) {
        for (i in 1..itemCount) {
            RemoteBox(
                RemoteModifier.size(80.rdp).padding(4.rdp).background(Color(0xFFEF5350).rc),
                contentAlignment = RemoteAlignment.Center,
            ) {
                RemoteText("$i".rs, color = Color.White.rc)
            }
        }
    }
}

@Composable
@RemoteComposable
fun AlignByBaseline(smallSize: Int, largeSize: Int) {
    RemoteRow(RemoteModifier.background(Color(0xFFF1F3F4).rc).padding(8.rdp)) {
        // Children aligned by their first text baseline rather than by their top.
        RemoteText(
            "Small".rs,
            fontSize = smallSize.rsp,
            modifier = RemoteModifier.alignByBaseline(),
        )
        RemoteText(
            "Large".rs,
            fontSize = largeSize.rsp,
            modifier = RemoteModifier.alignByBaseline(),
        )
    }
}

@Composable
@RemoteComposable
fun Shapes(shape: RemoteShape) {
    RemoteBox(RemoteModifier.size(140.rdp).clip(shape).background(Color(0xFFFF7043).rc))
}

@Composable
@RemoteComposable
fun GraphicsLayerCompositing(alpha: Float, compositingStrategy: RemoteCompositingStrategy) {
    // With an alpha below 1, Offscreen fades the layer as one image; ModulateAlpha fades each draw
    // on its own, so the overlap shows through.
    RemoteBox(
        RemoteModifier.graphicsLayer(alpha = alpha.rf, compositingStrategy = compositingStrategy)
            .size(220.rdp, 140.rdp),
    ) {
        RemoteBox(RemoteModifier.size(140.rdp).background(Color(0xFF3949AB).rc))
        // Padding, not offset, moves the second box: both stay inside the layer's bounds.
        RemoteBox(
            RemoteModifier.padding(start = 80.rdp)
                .size(220.rdp, 140.rdp)
                .background(Color(0xFFE53935).rc),
        )
    }
}
