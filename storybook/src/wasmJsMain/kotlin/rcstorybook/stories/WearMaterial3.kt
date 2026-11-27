package rcstorybook.stories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import ee.schimke.composeai.rccreation.compose.action.hostAction
import ee.schimke.composeai.rccreation.compose.action.valueChangeAction
import ee.schimke.composeai.rccreation.compose.capture.RemoteImageVector
import ee.schimke.composeai.rccreation.compose.capture.path
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteArrangement
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
import ee.schimke.composeai.rccreation.compose.layout.RemotePaddingValues
import ee.schimke.composeai.rccreation.compose.layout.RemoteRow
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.clickable
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxSize
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxWidth
import ee.schimke.composeai.rccreation.compose.modifier.padding
import ee.schimke.composeai.rccreation.compose.modifier.size
import ee.schimke.composeai.rccreation.compose.shaders.RemoteBrush
import ee.schimke.composeai.rccreation.compose.shaders.solidColor
import ee.schimke.composeai.rccreation.compose.shapes.RemoteRoundedCornerShape
import ee.schimke.composeai.rccreation.compose.shapes.RemoteShape
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteBoolean
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteFloat
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteInt
import ee.schimke.composeai.rccreation.compose.state.RemoteColor
import ee.schimke.composeai.rccreation.compose.state.RemoteImageBitmap
import ee.schimke.composeai.rccreation.compose.state.rb
import ee.schimke.composeai.rccreation.compose.state.rc
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.rf
import ee.schimke.composeai.rccreation.compose.state.ri
import ee.schimke.composeai.rccreation.compose.state.rs
import ee.schimke.composeai.rccreation.compose.text.RemoteTextAlign
import ee.schimke.composeai.rccreation.compose.text.RemoteTextOverflow
import ee.schimke.composeai.rccreation.wear.material3.RemoteAppCard
import ee.schimke.composeai.rccreation.wear.material3.RemoteBorderStroke
import ee.schimke.composeai.rccreation.wear.material3.RemoteButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteButtonColors
import ee.schimke.composeai.rccreation.wear.material3.RemoteButtonDefaults
import ee.schimke.composeai.rccreation.wear.material3.RemoteButtonGroup
import ee.schimke.composeai.rccreation.wear.material3.RemoteCard
import ee.schimke.composeai.rccreation.wear.material3.RemoteCardDefaults
import ee.schimke.composeai.rccreation.wear.material3.RemoteCheckboxButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteCircularProgressIndicator
import ee.schimke.composeai.rccreation.wear.material3.RemoteCompactButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteCurvedProgressIndicator
import ee.schimke.composeai.rccreation.wear.material3.RemoteEdgeButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteEdgeButtonDefaults
import ee.schimke.composeai.rccreation.wear.material3.RemoteEdgeButtonSize
import ee.schimke.composeai.rccreation.wear.material3.RemoteHorizontalPageIndicator
import ee.schimke.composeai.rccreation.wear.material3.RemoteIcon
import ee.schimke.composeai.rccreation.wear.material3.RemoteIconButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteIconButtonDefaults
import ee.schimke.composeai.rccreation.wear.material3.RemoteIconToggleButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteIconToggleButtonDefaults
import ee.schimke.composeai.rccreation.wear.material3.RemoteIntroOutroCurvedProgressIndicator
import ee.schimke.composeai.rccreation.wear.material3.RemoteLinearProgressIndicator
import ee.schimke.composeai.rccreation.wear.material3.RemoteMaterialTheme
import ee.schimke.composeai.rccreation.wear.material3.RemoteMotionScheme
import ee.schimke.composeai.rccreation.wear.material3.RemoteOutlinedCard
import ee.schimke.composeai.rccreation.wear.material3.RemoteRadioButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteShapes
import ee.schimke.composeai.rccreation.wear.material3.RemoteSplitCheckboxButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteSplitRadioButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteSplitSwitchButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteSwitchButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteText
import ee.schimke.composeai.rccreation.wear.material3.RemoteTextButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteTextButtonDefaults
import ee.schimke.composeai.rccreation.wear.material3.RemoteTextToggleButton
import ee.schimke.composeai.rccreation.wear.material3.RemoteTitleCard
import ee.schimke.composeai.rccreation.wear.material3.RemoteVerticalPageIndicator
import ee.schimke.composeai.rccreation.wear.material3.rememberRemotePageIndicatorState
import kotlin.math.roundToInt

// Wear OS Material 3 on Remote Compose (androidx.wear.compose.remote:remote-material3). Wear is
// dark by design: the components draw with the default RemoteMaterialTheme (dark colors), except
// WearTheme, which overrides it.

/** The color sets every Wear Material 3 button offers, from most to least emphasis. */
enum class WearButtonStyle {
    Filled,
    FilledVariant,
    FilledTonal,
    Outlined,
    Child,
}

/** The RemoteButtonDefaults colors for [style]. */
@Composable
@RemoteComposable
fun buttonColors(style: WearButtonStyle): RemoteButtonColors =
    when (style) {
        WearButtonStyle.Filled -> RemoteButtonDefaults.buttonColors()
        WearButtonStyle.FilledVariant -> RemoteButtonDefaults.filledVariantButtonColors()
        WearButtonStyle.FilledTonal -> RemoteButtonDefaults.filledTonalButtonColors()
        WearButtonStyle.Outlined -> RemoteButtonDefaults.outlinedButtonColors()
        WearButtonStyle.Child -> RemoteButtonDefaults.childButtonColors()
    }

/** Color roles of RemoteColorScheme a story lets you pick for a text, an icon or a card slot. */
enum class WearColorRole {
    Primary,
    PrimaryContainer,
    Secondary,
    Tertiary,
    OnSurface,
    OnSurfaceVariant,
    SurfaceContainer,
    SurfaceContainerHigh,
    Outline,
    Error,
    Transparent,
}

/** The theme's color for [role]. */
@Composable
@RemoteComposable
fun themeColor(role: WearColorRole): RemoteColor {
    val scheme = RemoteMaterialTheme.colorScheme
    return when (role) {
        WearColorRole.Primary -> scheme.primary
        WearColorRole.PrimaryContainer -> scheme.primaryContainer
        WearColorRole.Secondary -> scheme.secondary
        WearColorRole.Tertiary -> scheme.tertiary
        WearColorRole.OnSurface -> scheme.onSurface
        WearColorRole.OnSurfaceVariant -> scheme.onSurfaceVariant
        WearColorRole.SurfaceContainer -> scheme.surfaceContainer
        WearColorRole.SurfaceContainerHigh -> scheme.surfaceContainerHigh
        WearColorRole.Outline -> scheme.outline
        WearColorRole.Error -> scheme.error
        WearColorRole.Transparent -> Color.Transparent.rc
    }
}

/** The five corner sizes of RemoteShapes; cards default to Large. */
enum class WearShape {
    ExtraSmall,
    Small,
    Medium,
    Large,
    ExtraLarge,
}

/** The theme's shape for [shape]. */
@Composable
@RemoteComposable
fun themeShape(shape: WearShape): RemoteShape {
    val shapes = RemoteMaterialTheme.shapes
    return when (shape) {
        WearShape.ExtraSmall -> shapes.extraSmall
        WearShape.Small -> shapes.small
        WearShape.Medium -> shapes.medium
        WearShape.Large -> shapes.large
        WearShape.ExtraLarge -> shapes.extraLarge
    }
}

/** A heart, 24×24: the icon the Wear stories put in their buttons. */
fun heartIcon(): RemoteImageVector =
    RemoteImageVector.Builder(
            viewportWidth = 24f.rf,
            viewportHeight = 24f.rf,
            tintColor = Color.White.rc,
            name = "Heart",
        )
        .path(fill = RemoteBrush.solidColor(Color.White.rc)) {
            moveTo(12f.rf, 21f.rf)
            lineTo(4.5f.rf, 13.5f.rf)
            curveTo(1.5f.rf, 10.5f.rf, 2.5f.rf, 4f.rf, 7.5f.rf, 4f.rf)
            curveTo(9.5f.rf, 4f.rf, 11f.rf, 5f.rf, 12f.rf, 6.5f.rf)
            curveTo(13f.rf, 5f.rf, 14.5f.rf, 4f.rf, 16.5f.rf, 4f.rf)
            curveTo(21.5f.rf, 4f.rf, 22.5f.rf, 10.5f.rf, 19.5f.rf, 13.5f.rf)
            close()
        }
        .build()

/**
 * The outline an outlined Wear button draws: the remote API has no default border, so the story
 * passes one (1 dp in the theme's outlineVariant, as Wear Material 3's outlined buttons use).
 */
@Composable
@RemoteComposable
fun outlinedBorder(): RemoteBorderStroke =
    RemoteBorderStroke(1.rdp, RemoteMaterialTheme.colorScheme.outlineVariant)

/** A centred, padded area for one component on a round watch screen. */
@Composable
@RemoteComposable
fun WatchScreen(content: @Composable @RemoteComposable () -> Unit) {
    RemoteBox(
        modifier = RemoteModifier.fillMaxSize().padding(horizontal = 12.rdp),
        contentAlignment = RemoteAlignment.Center,
    ) {
        content()
    }
}

@Composable
@RemoteComposable
fun WearButton(
    label: String,
    secondaryLabel: String,
    style: WearButtonStyle,
    withIcon: Boolean,
    enabled: Boolean,
) {
    WatchScreen {
        RemoteButton(
            onClick = hostAction("button_click".rs),
            modifier = RemoteModifier.fillMaxWidth(),
            enabled = enabled.rb,
            colors = buttonColors(style),
            border = if (style == WearButtonStyle.Outlined) outlinedBorder() else null,
            icon =
                if (withIcon) {
                    {
                        RemoteIcon(
                            imageVector = heartIcon(),
                            contentDescription = "Favourite".rs,
                            modifier = RemoteModifier.size(RemoteButtonDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
            secondaryLabel =
                if (secondaryLabel.isNotEmpty()) {
                    { RemoteText(secondaryLabel.rs) }
                } else {
                    null
                },
            label = { RemoteText(label.rs) },
        )
    }
}

@Composable
@RemoteComposable
fun WearCompactButton(label: String, style: WearButtonStyle) {
    WatchScreen {
        RemoteCompactButton(
            onClick = hostAction("compact_click".rs),
            colors = buttonColors(style),
            border = if (style == WearButtonStyle.Outlined) outlinedBorder() else null,
            icon = {
                RemoteIcon(
                    imageVector = heartIcon(),
                    contentDescription = null,
                    modifier = RemoteModifier.size(RemoteButtonDefaults.ExtraSmallIconSize),
                )
            },
            label = { RemoteText(label.rs) },
        )
    }
}

@Composable
@RemoteComposable
fun WearEdgeButton(label: String, size: RemoteEdgeButtonSize, filledTonal: Boolean) {
    // The tap count lives in the document: the player updates it with no server round-trip.
    val taps = remember { MutableRemoteInt(0) }
    RemoteBox(modifier = RemoteModifier.fillMaxSize()) {
        RemoteText(
            text = "Taps: ".rs + taps.toRemoteString(),
            modifier = RemoteModifier.fillMaxWidth().padding(top = 56.rdp),
            textAlign = RemoteTextAlign.Center,
        )
        RemoteBox(
            modifier = RemoteModifier.fillMaxSize(),
            contentAlignment = RemoteAlignment.BottomCenter,
        ) {
            // Wear Material 3's EdgeButton keeps 3 dp off the bottom edge; RemoteEdgeButton draws
            // flush with it, so the padding is added here.
            RemoteEdgeButton(
                onClick = valueChangeAction(taps, taps + 1.ri),
                modifier =
                    RemoteModifier.padding(bottom = RemoteEdgeButtonDefaults.VerticalPadding),
                buttonSize = size,
                colors =
                    if (filledTonal) {
                        RemoteEdgeButtonDefaults.filledTonalButtonColors()
                    } else {
                        RemoteEdgeButtonDefaults.buttonColors()
                    },
            ) {
                RemoteText(label.rs)
            }
        }
    }
}

@Composable
@RemoteComposable
fun WearIconButtons(enabled: Boolean) {
    WatchScreen {
        RemoteRow(horizontalArrangement = RemoteArrangement.spacedBy(4.rdp)) {
            // Filled, filled tonal, and outlined (which needs its border passed).
            val buttons =
                listOf(
                    RemoteIconButtonDefaults.filledIconButtonColors() to null,
                    RemoteIconButtonDefaults.filledTonalIconButtonColors() to null,
                    RemoteIconButtonDefaults.outlinedIconButtonColors() to outlinedBorder(),
                )
            buttons.forEach { (colors, border) ->
                RemoteIconButton(
                    onClick = hostAction("icon_click".rs),
                    enabled = enabled.rb,
                    colors = colors,
                    border = border,
                ) {
                    RemoteIcon(imageVector = heartIcon(), contentDescription = "Favourite".rs)
                }
            }
        }
    }
}

@Composable
@RemoteComposable
fun WearTextButtons(text: String, enabled: Boolean) {
    WatchScreen {
        RemoteRow(horizontalArrangement = RemoteArrangement.spacedBy(8.rdp)) {
            RemoteTextButton(
                onClick = hostAction("text_click".rs),
                enabled = enabled.rb,
                colors = RemoteTextButtonDefaults.filledTonalTextButtonColors(),
            ) {
                RemoteText(text.rs)
            }
            RemoteTextButton(onClick = hostAction("text_click".rs), enabled = enabled.rb) {
                RemoteText(text.rs)
            }
        }
    }
}

@Composable
@RemoteComposable
fun WearToggleButtons(initiallyChecked: Boolean, variantShapes: Boolean) {
    val muted = remember(initiallyChecked) { MutableRemoteBoolean(initiallyChecked) }
    val bold = remember(initiallyChecked) { MutableRemoteBoolean(initiallyChecked) }
    WatchScreen {
        RemoteRow(horizontalArrangement = RemoteArrangement.spacedBy(8.rdp)) {
            RemoteIconToggleButton(
                checked = muted,
                onCheckedChange = valueChangeAction(muted, !muted),
                shapes =
                    if (variantShapes) {
                        RemoteIconToggleButtonDefaults.variantShapes()
                    } else {
                        RemoteIconToggleButtonDefaults.shapes()
                    },
            ) {
                RemoteIcon(
                    imageVector = heartIcon(),
                    contentDescription = "Favourite".rs,
                    modifier = RemoteModifier.size(RemoteIconToggleButtonDefaults.DefaultIconSize),
                )
            }
            RemoteTextToggleButton(
                checked = bold,
                onCheckedChange = valueChangeAction(bold, !bold),
            ) {
                RemoteText("B".rs)
            }
        }
    }
}

@Composable
@RemoteComposable
fun WearButtonGroup(buttons: Int) {
    // As in AndroidX's RemoteButtonGroupAnimatedSample: the middle of three buttons is wider, and
    // animateWidth makes a pressed button expand while its neighbours give way.
    val weights = if (buttons == 3) listOf(1f, 1.5f, 1f) else List(buttons) { 1f }
    WatchScreen {
        RemoteButtonGroup(modifier = RemoteModifier.fillMaxWidth()) {
            weights.forEachIndexed { index, weight ->
                RemoteIconButton(
                    onClick = hostAction("group_click".rs, index.ri),
                    modifier = RemoteModifier.weight(weight.rf).animateWidth(),
                    colors = RemoteIconButtonDefaults.filledIconButtonColors(),
                    shapes = RemoteIconButtonDefaults.shapes(RemoteButtonDefaults.shape),
                ) {
                    RemoteIcon(imageVector = heartIcon(), contentDescription = null)
                }
            }
        }
    }
}

@Composable
@RemoteComposable
fun WearCheckboxButtons(label: String, initiallyChecked: Boolean, enabled: Boolean) {
    val checked = remember(initiallyChecked) { MutableRemoteBoolean(initiallyChecked) }
    val split = remember(initiallyChecked) { MutableRemoteBoolean(initiallyChecked) }
    WatchScreen {
        RemoteColumn(verticalArrangement = RemoteArrangement.spacedBy(4.rdp)) {
            RemoteCheckboxButton(
                checked = checked,
                onCheckedChange = valueChangeAction(checked, !checked),
                modifier = RemoteModifier.fillMaxWidth(),
                enabled = enabled.rb,
                label = { RemoteText(label.rs) },
            )
            RemoteSplitCheckboxButton(
                checked = split,
                onCheckedChange = valueChangeAction(split, !split),
                toggleContentDescription = "Toggle".rs,
                onContainerClick = hostAction("open_details".rs),
                modifier = RemoteModifier.fillMaxWidth(),
                enabled = enabled.rb,
                secondaryLabel = { RemoteText("Split".rs) },
                label = { RemoteText(label.rs) },
            )
        }
    }
}

@Composable
@RemoteComposable
fun WearSwitchButtons(label: String, initiallyChecked: Boolean, enabled: Boolean) {
    val checked = remember(initiallyChecked) { MutableRemoteBoolean(initiallyChecked) }
    val split = remember(initiallyChecked) { MutableRemoteBoolean(initiallyChecked) }
    WatchScreen {
        RemoteColumn(verticalArrangement = RemoteArrangement.spacedBy(4.rdp)) {
            RemoteSwitchButton(
                checked = checked,
                onCheckedChange = valueChangeAction(checked, !checked),
                modifier = RemoteModifier.fillMaxWidth(),
                enabled = enabled.rb,
                label = { RemoteText(label.rs) },
            )
            RemoteSplitSwitchButton(
                checked = split,
                onCheckedChange = valueChangeAction(split, !split),
                toggleContentDescription = "Toggle".rs,
                onContainerClick = hostAction("open_details".rs),
                modifier = RemoteModifier.fillMaxWidth(),
                enabled = enabled.rb,
                secondaryLabel = { RemoteText("Split".rs) },
                label = { RemoteText(label.rs) },
            )
        }
    }
}

@Composable
@RemoteComposable
fun WearRadioButtons(first: String, second: String, enabled: Boolean) {
    // One selected index for both buttons: selecting one deselects the other.
    val selected = remember { MutableRemoteInt(0) }
    WatchScreen {
        RemoteColumn(verticalArrangement = RemoteArrangement.spacedBy(4.rdp)) {
            RemoteRadioButton(
                selected = selected.isEqualTo(0.ri),
                onSelect = valueChangeAction(selected, 0.ri),
                modifier = RemoteModifier.fillMaxWidth(),
                enabled = enabled.rb,
                label = { RemoteText(first.rs) },
            )
            RemoteSplitRadioButton(
                selected = selected.isEqualTo(1.ri),
                onSelectionClick = valueChangeAction(selected, 1.ri),
                selectionContentDescription = second.rs,
                onContainerClick = hostAction("open_details".rs),
                modifier = RemoteModifier.fillMaxWidth(),
                enabled = enabled.rb,
                label = { RemoteText(second.rs) },
            )
        }
    }
}

@Composable
@RemoteComposable
fun WearCard(
    text: String,
    enabled: Boolean,
    shape: WearShape,
    contentPadding: Int,
    containerColor: WearColorRole,
    contentColor: WearColorRole,
) {
    WatchScreen {
        RemoteCard(
            onClick = hostAction("open_card".rs),
            modifier = RemoteModifier.fillMaxWidth(),
            enabled = enabled.rb,
            shape = themeShape(shape),
            colors =
                RemoteCardDefaults.cardColors(
                    containerColor = themeColor(containerColor),
                    contentColor = themeColor(contentColor),
                ),
            contentPadding = RemotePaddingValues(contentPadding.rdp),
        ) {
            RemoteText(text.rs)
        }
    }
}

@Composable
@RemoteComposable
fun WearOutlinedCard(
    text: String,
    enabled: Boolean,
    shape: WearShape,
    contentPadding: Int,
    contentColor: WearColorRole,
    borderColor: WearColorRole,
    borderWidth: Int,
) {
    WatchScreen {
        // An outlined card's container is transparent: only the border and the content show.
        RemoteOutlinedCard(
            onClick = hostAction("open_card".rs),
            modifier = RemoteModifier.fillMaxWidth(),
            enabled = enabled.rb,
            shape = themeShape(shape),
            colors = RemoteCardDefaults.outlinedCardColors(contentColor = themeColor(contentColor)),
            border =
                RemoteCardDefaults.outlinedCardBorder(
                    outlineColor = themeColor(borderColor),
                    borderWidth = borderWidth.rdp,
                ),
            contentPadding = RemotePaddingValues(contentPadding.rdp),
        ) {
            RemoteText(text.rs)
        }
    }
}

/** A sunset, 160×90, drawn into a bitmap the document carries: the image card's background. */
fun sunsetBitmap(): RemoteImageBitmap {
    val image = ImageBitmap(160, 90)
    Canvas(image).apply {
        drawRect(0f, 0f, 160f, 90f, Paint().apply { color = Color(0xFFFF7043) })
        drawCircle(Offset(110f, 60f), 22f, Paint().apply { color = Color(0xFFFFD54F) })
        drawRect(0f, 64f, 160f, 90f, Paint().apply { color = Color(0xFF4E342E) })
    }
    return RemoteImageBitmap(image)
}

@Composable
@RemoteComposable
fun WearImageCard(title: String, subtitle: String, imageAlpha: Float, scrim: Boolean) {
    val bitmap = remember { sunsetBitmap() }
    WatchScreen {
        // containerPainter draws the image under the content, clipped to the card's shape; the
        // scrim darkens it so the text stays readable.
        RemoteTitleCard(
            onClick = hostAction("open_card".rs),
            containerPainter =
                RemoteCardDefaults.containerPainter(
                    image = bitmap,
                    scrim = if (scrim) RemoteCardDefaults.scrimBrush() else null,
                    alpha = imageAlpha.rf,
                ),
            title = { RemoteText(title.rs) },
            subtitle = { RemoteText(subtitle.rs) },
            modifier = RemoteModifier.fillMaxWidth(),
            colors = RemoteCardDefaults.cardWithContainerPainterColors(),
        )
    }
}

@Composable
@RemoteComposable
fun WearAppCard(
    appName: String,
    title: String,
    time: String,
    content: String,
    appImage: Boolean,
    enabled: Boolean,
    shape: WearShape,
    contentPadding: Int,
    containerColor: WearColorRole,
    contentColor: WearColorRole,
    appNameColor: WearColorRole,
    timeColor: WearColorRole,
    titleColor: WearColorRole,
) {
    WatchScreen {
        RemoteAppCard(
            onClick = hostAction("open_card".rs),
            appName = { RemoteText(appName.rs) },
            title = { RemoteText(title.rs) },
            modifier = RemoteModifier.fillMaxWidth(),
            enabled = enabled.rb,
            shape = themeShape(shape),
            colors =
                RemoteCardDefaults.cardColors(
                    containerColor = themeColor(containerColor),
                    contentColor = themeColor(contentColor),
                    appNameColor = themeColor(appNameColor),
                    timeColor = themeColor(timeColor),
                    titleColor = themeColor(titleColor),
                ),
            contentPadding = RemotePaddingValues(contentPadding.rdp),
            appImage =
                if (appImage) {
                    {
                        RemoteIcon(
                            imageVector = heartIcon(),
                            contentDescription = null,
                            modifier = RemoteModifier.size(RemoteCardDefaults.AppImageSize),
                        )
                    }
                } else {
                    null
                },
            time = if (time.isNotEmpty()) ({ RemoteText(time.rs) }) else null,
        ) {
            if (content.isNotEmpty()) RemoteText(content.rs)
        }
    }
}

@Composable
@RemoteComposable
fun WearTitleCard(
    title: String,
    time: String,
    subtitle: String,
    content: String,
    enabled: Boolean,
    shape: WearShape,
    contentPadding: Int,
    containerColor: WearColorRole,
    contentColor: WearColorRole,
    timeColor: WearColorRole,
    titleColor: WearColorRole,
    subtitleColor: WearColorRole,
) {
    WatchScreen {
        RemoteTitleCard(
            onClick = hostAction("open_card".rs),
            title = { RemoteText(title.rs) },
            modifier = RemoteModifier.fillMaxWidth(),
            time = if (time.isNotEmpty()) ({ RemoteText(time.rs) }) else null,
            subtitle = if (subtitle.isNotEmpty()) ({ RemoteText(subtitle.rs) }) else null,
            enabled = enabled.rb,
            shape = themeShape(shape),
            colors =
                RemoteCardDefaults.cardColors(
                    containerColor = themeColor(containerColor),
                    contentColor = themeColor(contentColor),
                    timeColor = themeColor(timeColor),
                    titleColor = themeColor(titleColor),
                    subtitleColor = themeColor(subtitleColor),
                ),
            contentPadding = RemotePaddingValues(contentPadding.rdp),
            content = if (content.isNotEmpty()) ({ RemoteText(content.rs) }) else null,
        )
    }
}

@Composable
@RemoteComposable
fun WearText(text: String, maxLines: Int, color: WearColorRole) {
    WatchScreen {
        RemoteText(
            text = text.rs,
            color = themeColor(color),
            style = RemoteMaterialTheme.typography.titleMedium,
            textAlign = RemoteTextAlign.Center,
            maxLines = maxLines,
            overflow = RemoteTextOverflow.Ellipsis,
        )
    }
}

@Composable
@RemoteComposable
fun WearIcon(size: Int, tint: WearColorRole) {
    WatchScreen {
        RemoteIcon(
            imageVector = heartIcon(),
            contentDescription = "Favourite".rs,
            modifier = RemoteModifier.size(size.rdp),
            tint = themeColor(tint),
        )
    }
}

@Composable
@RemoteComposable
fun WearCircularProgress(progress: Float, enabled: Boolean, indeterminate: Boolean) {
    WatchScreen {
        if (indeterminate) {
            RemoteCircularProgressIndicator(modifier = RemoteModifier.size(64.rdp))
        } else {
            RemoteCircularProgressIndicator(
                progress = progress.rf,
                modifier = RemoteModifier.size(96.rdp),
                enabled = enabled.rb,
            )
        }
    }
}

@Composable
@RemoteComposable
fun WearLinearProgress(progress: Float, enabled: Boolean) {
    WatchScreen {
        RemoteLinearProgressIndicator(
            progress = progress.rf,
            modifier = RemoteModifier.fillMaxWidth(),
            enabled = enabled.rb,
        )
    }
}

@Composable
@RemoteComposable
fun WearCurvedProgress(progress: Float, sweepAngle: Int) {
    // Drawn along the round screen's edge, around the content.
    RemoteBox(modifier = RemoteModifier.fillMaxSize(), contentAlignment = RemoteAlignment.Center) {
        RemoteCurvedProgressIndicator(
            progress = progress.rf,
            modifier = RemoteModifier.fillMaxSize(),
            sweepAngle = sweepAngle.toFloat().rf,
        )
        RemoteText("${(progress * 100).roundToInt()}%".rs)
    }
}

@Composable
@RemoteComposable
fun WearIntroOutroProgress(sweepAngle: Int) {
    // As in AndroidX's RemoteIntroOutroCurvedProgressIndicatorSample: the progress lives in the
    // document, and each tap steps it 0 -> 0.5 -> 1 -> 0, so the player animates every change.
    val progress = remember { MutableRemoteFloat(0f) }
    val next =
        progress.isGreaterThanOrEqualTo(1f.rf).select(ifTrue = 0f.rf, ifFalse = progress + 0.5f.rf)
    RemoteBox(
        modifier = RemoteModifier.fillMaxSize().clickable(valueChangeAction(progress, next)),
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteIntroOutroCurvedProgressIndicator(
            progress = progress,
            modifier = RemoteModifier.fillMaxSize(),
            sweepAngle = sweepAngle.toFloat().rf,
        )
        RemoteText("Tap to step".rs)
    }
}

@Composable
@RemoteComposable
fun WearPageIndicator(pageCount: Int, selectedPage: Int, vertical: Boolean) {
    val state =
        rememberRemotePageIndicatorState(
            pageCount = pageCount,
            selectedPage = selectedPage.coerceIn(0, pageCount - 1).ri,
        )
    RemoteBox(modifier = RemoteModifier.fillMaxSize(), contentAlignment = RemoteAlignment.Center) {
        RemoteText("Page ${selectedPage.coerceIn(0, pageCount - 1) + 1} of $pageCount".rs)
        if (vertical) {
            RemoteVerticalPageIndicator(state = state)
        } else {
            RemoteHorizontalPageIndicator(state = state)
        }
    }
}

/** RemoteMaterialTheme's two motion schemes. */
enum class WearMotion {
    Standard,
    Expressive,
}

/** Shape sets for the theme story: Wear's default corners, or all corners at one size. */
enum class WearShapes {
    Default,
    Square,
    Round,
}

/** The RemoteShapes for [shapes]. */
fun remoteShapes(shapes: WearShapes): RemoteShapes =
    when (shapes) {
        WearShapes.Default -> RemoteShapes()
        WearShapes.Square -> uniformShapes(RemoteRoundedCornerShape(2.rdp))
        WearShapes.Round -> uniformShapes(RemoteRoundedCornerShape(36.rdp))
    }

private fun uniformShapes(shape: RemoteRoundedCornerShape) =
    RemoteShapes(
        extraSmall = shape,
        small = shape,
        medium = shape,
        large = shape,
        extraLarge = shape,
    )

@Composable
@RemoteComposable
fun WearTheme(
    primary: Color,
    onPrimary: Color,
    secondary: Color,
    tertiary: Color,
    surfaceContainer: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    error: Color,
    motion: WearMotion,
    shapes: WearShapes,
) {
    val colors =
        RemoteMaterialTheme.colorScheme.copy(
            primary = primary.rc,
            onPrimary = onPrimary.rc,
            secondary = secondary.rc,
            tertiary = tertiary.rc,
            surfaceContainer = surfaceContainer.rc,
            onSurface = onSurface.rc,
            onSurfaceVariant = onSurfaceVariant.rc,
            error = error.rc,
        )
    RemoteMaterialTheme(
        colorScheme = colors,
        shapes = remoteShapes(shapes),
        motionScheme =
            when (motion) {
                WearMotion.Standard -> RemoteMotionScheme.standard()
                WearMotion.Expressive -> RemoteMotionScheme.expressive()
            },
    ) {
        // Every component below reads the theme: the text (secondary), the card (surfaceContainer,
        // onSurface, tertiary, onSurfaceVariant, large shape), the progress (primary) and the
        // button (primary, onPrimary); the warning uses error.
        RemoteColumn(
            modifier = RemoteModifier.fillMaxSize().padding(horizontal = 24.rdp),
            verticalArrangement =
                RemoteArrangement.spacedBy(4.rdp, RemoteAlignment.CenterVertically),
            horizontalAlignment = RemoteAlignment.CenterHorizontally,
        ) {
            RemoteText(
                "Morning run".rs,
                color = RemoteMaterialTheme.colorScheme.secondary,
                style = RemoteMaterialTheme.typography.titleSmall,
            )
            RemoteTitleCard(
                onClick = hostAction("themed_card".rs),
                title = { RemoteText("5 km".rs) },
                subtitle = { RemoteText("Goal".rs) },
                modifier = RemoteModifier.fillMaxWidth(),
            ) {
                RemoteText("3.2 km done".rs)
            }
            RemoteLinearProgressIndicator(
                progress = 0.64f.rf,
                modifier = RemoteModifier.fillMaxWidth(),
            )
            RemoteText(
                "Battery low".rs,
                color = RemoteMaterialTheme.colorScheme.error,
                style = RemoteMaterialTheme.typography.labelSmall,
            )
            RemoteCompactButton(onClick = hostAction("themed_click".rs)) { RemoteText("Start".rs) }
        }
    }
}
