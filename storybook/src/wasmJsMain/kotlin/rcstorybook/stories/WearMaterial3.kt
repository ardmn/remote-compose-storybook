package rcstorybook.stories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import ee.schimke.composeai.rccreation.compose.action.hostAction
import ee.schimke.composeai.rccreation.compose.action.valueChangeAction
import ee.schimke.composeai.rccreation.compose.capture.RemoteImageVector
import ee.schimke.composeai.rccreation.compose.capture.path
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteArrangement
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
import ee.schimke.composeai.rccreation.compose.layout.RemoteRow
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxSize
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxWidth
import ee.schimke.composeai.rccreation.compose.modifier.padding
import ee.schimke.composeai.rccreation.compose.modifier.size
import ee.schimke.composeai.rccreation.compose.shaders.RemoteBrush
import ee.schimke.composeai.rccreation.compose.shaders.solidColor
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteBoolean
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteInt
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
import ee.schimke.composeai.rccreation.wear.material3.RemoteOutlinedCard
import ee.schimke.composeai.rccreation.wear.material3.RemoteRadioButton
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
            RemoteEdgeButton(
                onClick = valueChangeAction(taps, taps + 1.ri),
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
    val muted = remember { MutableRemoteBoolean(initiallyChecked) }
    val bold = remember { MutableRemoteBoolean(initiallyChecked) }
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
    WatchScreen {
        RemoteButtonGroup(modifier = RemoteModifier.fillMaxWidth()) {
            repeat(buttons) { index ->
                RemoteIconButton(
                    onClick = hostAction("group_click".rs, index.ri),
                    modifier = RemoteModifier.weight(1f.rf),
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
    val checked = remember { MutableRemoteBoolean(initiallyChecked) }
    val split = remember { MutableRemoteBoolean(initiallyChecked) }
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
    val checked = remember { MutableRemoteBoolean(initiallyChecked) }
    val split = remember { MutableRemoteBoolean(initiallyChecked) }
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
fun WearCard(text: String, outlined: Boolean) {
    WatchScreen {
        if (outlined) {
            RemoteOutlinedCard(
                onClick = hostAction("open_card".rs),
                modifier = RemoteModifier.fillMaxWidth(),
            ) {
                RemoteText(text.rs)
            }
        } else {
            RemoteCard(
                onClick = hostAction("open_card".rs),
                modifier = RemoteModifier.fillMaxWidth(),
            ) {
                RemoteText(text.rs)
            }
        }
    }
}

@Composable
@RemoteComposable
fun WearAppCard(appName: String, title: String, body: String, time: String) {
    WatchScreen {
        RemoteAppCard(
            onClick = hostAction("open_card".rs),
            appName = { RemoteText(appName.rs) },
            time = { RemoteText(time.rs) },
            title = { RemoteText(title.rs) },
            modifier = RemoteModifier.fillMaxWidth(),
        ) {
            RemoteText(body.rs)
        }
    }
}

@Composable
@RemoteComposable
fun WearTitleCard(title: String, subtitle: String, time: String) {
    WatchScreen {
        RemoteTitleCard(
            onClick = hostAction("open_card".rs),
            title = { RemoteText(title.rs) },
            time = { RemoteText(time.rs) },
            subtitle = { RemoteText(subtitle.rs) },
            modifier = RemoteModifier.fillMaxWidth(),
        )
    }
}

@Composable
@RemoteComposable
fun WearText(text: String, maxLines: Int, primaryColor: Boolean) {
    WatchScreen {
        RemoteText(
            text = text.rs,
            color =
                if (primaryColor) {
                    RemoteMaterialTheme.colorScheme.primary
                } else {
                    RemoteMaterialTheme.colorScheme.onSurface
                },
            style = RemoteMaterialTheme.typography.titleMedium,
            textAlign = RemoteTextAlign.Center,
            maxLines = maxLines,
            overflow = RemoteTextOverflow.Ellipsis,
        )
    }
}

@Composable
@RemoteComposable
fun WearIcon(size: Int, primaryTint: Boolean) {
    WatchScreen {
        RemoteIcon(
            imageVector = heartIcon(),
            contentDescription = "Favourite".rs,
            modifier = RemoteModifier.size(size.rdp),
            tint =
                if (primaryTint) {
                    RemoteMaterialTheme.colorScheme.primary
                } else {
                    RemoteMaterialTheme.colorScheme.onSurface
                },
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
fun WearCurvedProgress(progress: Float, sweepAngle: Int, introOutro: Boolean) {
    // Drawn along the round screen's edge, around the content.
    RemoteBox(modifier = RemoteModifier.fillMaxSize(), contentAlignment = RemoteAlignment.Center) {
        if (introOutro) {
            RemoteIntroOutroCurvedProgressIndicator(
                progress = progress.rf,
                modifier = RemoteModifier.fillMaxSize(),
                sweepAngle = sweepAngle.toFloat().rf,
            )
        } else {
            RemoteCurvedProgressIndicator(
                progress = progress.rf,
                modifier = RemoteModifier.fillMaxSize(),
                sweepAngle = sweepAngle.toFloat().rf,
            )
        }
        RemoteText("${(progress * 100).roundToInt()}%".rs)
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

@Composable
@RemoteComposable
fun WearTheme(primary: Color, onPrimary: Color) {
    val colors =
        RemoteMaterialTheme.colorScheme.copy(primary = primary.rc, onPrimary = onPrimary.rc)
    RemoteMaterialTheme(colorScheme = colors) {
        WatchScreen {
            RemoteButton(
                onClick = hostAction("themed_click".rs),
                modifier = RemoteModifier.fillMaxWidth(),
            ) {
                RemoteText("Themed button".rs)
            }
        }
    }
}
