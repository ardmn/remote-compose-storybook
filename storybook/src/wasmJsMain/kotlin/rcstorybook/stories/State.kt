package rcstorybook.stories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import ee.schimke.composeai.rccreation.compose.action.combinedAction
import ee.schimke.composeai.rccreation.compose.action.valueChangeAction
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteArrangement
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
import ee.schimke.composeai.rccreation.compose.layout.RemoteImage
import ee.schimke.composeai.rccreation.compose.layout.RemoteRow
import ee.schimke.composeai.rccreation.compose.layout.RemoteStateLayout
import ee.schimke.composeai.rccreation.compose.layout.RemoteText
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.background
import ee.schimke.composeai.rccreation.compose.modifier.clickable
import ee.schimke.composeai.rccreation.compose.modifier.clip
import ee.schimke.composeai.rccreation.compose.modifier.padding
import ee.schimke.composeai.rccreation.compose.modifier.size
import ee.schimke.composeai.rccreation.compose.modifier.width
import ee.schimke.composeai.rccreation.compose.shapes.RemoteRoundedCornerShape
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteBoolean
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteEnum
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteFloat
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteImageBitmap
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteInt
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteString
import ee.schimke.composeai.rccreation.compose.state.RemoteColor
import ee.schimke.composeai.rccreation.compose.state.RemoteConfiguration
import ee.schimke.composeai.rccreation.compose.state.RemoteEnum
import ee.schimke.composeai.rccreation.compose.state.RemoteTimeVariables
import ee.schimke.composeai.rccreation.compose.state.lerp
import ee.schimke.composeai.rccreation.compose.state.rb
import ee.schimke.composeai.rccreation.compose.state.rc
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.rememberMutableRemoteImageBitmap
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteBoolean
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteColor
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteDp
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteEnum
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteFloat
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteInt
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteString
import ee.schimke.composeai.rccreation.compose.state.rf
import ee.schimke.composeai.rccreation.compose.state.ri
import ee.schimke.composeai.rccreation.compose.state.rs
import ee.schimke.composeai.rccreation.compose.state.rsp
import ee.schimke.composeai.rccreation.compose.state.sin
import ee.schimke.composeai.rccreation.compose.state.toRemoteDp
import ee.schimke.composeai.rccreation.compose.text.RemoteTimeDefaults

enum class Screen {
    Loading,
    Content,
    Error,
}

@Composable
@RemoteComposable
fun StateLayoutBoolean(initial: Boolean) {
    val state = remember { MutableRemoteBoolean(initial) }
    RemoteStateLayout(state, RemoteModifier.clickable(valueChangeAction(state, !state))) { isTrue ->
        if (isTrue) {
            RemoteBox(
                RemoteModifier.size(200.rdp, 100.rdp).background(Color(0xFF43A047).rc),
                contentAlignment = RemoteAlignment.Center,
            ) {
                RemoteText("true — tap".rs, color = Color.White.rc)
            }
        } else {
            RemoteBox(
                RemoteModifier.size(140.rdp, 140.rdp).background(Color(0xFFE53935).rc),
                contentAlignment = RemoteAlignment.Center,
            ) {
                RemoteText("false — tap".rs, color = Color.White.rc)
            }
        }
    }
}

@Composable
@RemoteComposable
fun StateLayoutInt(initial: Int) {
    val step = remember { MutableRemoteInt(initial) }
    // Each listed state is one branch of the document; the player shows the current one.
    RemoteStateLayout(
        step,
        0,
        1,
        2,
        modifier = RemoteModifier.clickable(valueChangeAction(step, (step + 1.ri) % 3.ri)),
    ) { index ->
        val color =
            when (index) {
                0 -> Color(0xFF1E88E5)
                1 -> Color(0xFFFB8C00)
                else -> Color(0xFF8E24AA)
            }
        RemoteBox(
            RemoteModifier.size(220.rdp, 90.rdp).background(color.rc),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("Step $index of 3 — tap for next".rs, color = Color.White.rc)
        }
    }
}

@Composable
@RemoteComposable
fun StateLayoutEnum(initial: Screen) {
    val screen = remember { MutableRemoteEnum(initial) }
    RemoteStateLayout(screen) { current ->
        val next = Screen.entries[(current.ordinal + 1) % Screen.entries.size]
        RemoteBox(
            RemoteModifier.size(240.rdp, 90.rdp)
                .background(
                    when (current) {
                        Screen.Loading -> Color(0xFF757575)
                        Screen.Content -> Color(0xFF43A047)
                        Screen.Error -> Color(0xFFE53935)
                    }.rc,
                )
                .clickable(valueChangeAction(screen, RemoteEnum(next))),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("${current.name} — tap for ${next.name}".rs, color = Color.White.rc)
        }
    }
}

@Composable
@RemoteComposable
fun NamedValues(title: String, scale: Float, accent: Color, showBadge: Boolean) {
    // Named values have defaults in the document; a host can override them by name at play time
    // without a new document.
    val name = rememberNamedRemoteString("title", title)
    val fontScale = rememberNamedRemoteFloat("fontScale", scale.rf)
    val color = rememberNamedRemoteColor("accent", accent.rc)
    val badge = rememberNamedRemoteBoolean("showBadge", showBadge)
    val padding = rememberNamedRemoteDp("padding", 16.rdp)
    val unread = rememberNamedRemoteInt("unread", 3)
    RemoteColumn(
        RemoteModifier.width(280.rdp)
            .clip(RemoteRoundedCornerShape(16.rdp))
            .background(color)
            .padding(padding),
        verticalArrangement = RemoteArrangement.spacedBy(6.rdp),
    ) {
        RemoteText(name, color = Color.White.rc, fontSize = (18f.rf * fontScale).rsp)
        RemoteText(
            badge.select("unread: ".rs + unread.toRemoteString(), "no badge".rs),
            color = Color.White.rc,
        )
    }
}

@Composable
@RemoteComposable
fun NamedEnum(initial: Screen) {
    val screen = rememberNamedRemoteEnum("screen", initial)
    RemoteText("screen = ".rs + screen.toRemoteString { it.name.rs }, fontSize = 20.rsp)
}

@Composable
@RemoteComposable
fun FloatExpressions(speed: Float, base: Float) {
    // Arithmetic on RemoteFloat builds an expression the player evaluates every frame.
    val t = RemoteTimeVariables.continuousSeconds * speed.rf
    val wave = (sin(t) + 1f.rf) / 2f.rf
    val width = base.rf + wave * 160f.rf
    RemoteColumn(verticalArrangement = RemoteArrangement.spacedBy(8.rdp)) {
        RemoteBox(RemoteModifier.size(width.toRemoteDp(), 40.rdp).background(Color(0xFF00897B).rc))
        RemoteText(
            "width = $base + sin(t) × 160".rs + " → ".rs + width.toRemoteInt().toRemoteString(),
            fontSize = 14.rsp,
        )
    }
}

@Composable
@RemoteComposable
fun ColorExpressions(from: Color, to: Color, speed: Float) {
    val t = (sin(RemoteTimeVariables.continuousSeconds * speed.rf) + 1f.rf) / 2f.rf
    RemoteRow(horizontalArrangement = RemoteArrangement.spacedBy(10.rdp)) {
        // lerp between two colors, by a time-driven fraction.
        RemoteBox(RemoteModifier.size(90.rdp).background(lerp(from.rc, to.rc, t)))
        // A hue that rotates with time: hue, saturation and value are all in 0..1.
        RemoteBox(
            RemoteModifier.size(90.rdp).background(RemoteColor.hsv(t, 0.8f.rf, 0.9f.rf)),
        )
        // A copy of a color with a computed alpha.
        RemoteBox(RemoteModifier.size(90.rdp).background(from.rc.copy(alpha = t)))
    }
}

@Composable
@RemoteComposable
fun Clock(is24Hour: Boolean) {
    // Time is read by the player, so the clock ticks with no new document.
    val time = RemoteTimeVariables
    val hh = time.hourOfDay.toRemoteString()
    val mm = (time.minuteOfDay % 60.ri).toRemoteString()
    val ss = time.secondOfHour % 60.ri
    RemoteColumn(
        horizontalAlignment = RemoteAlignment.CenterHorizontally,
        verticalArrangement = RemoteArrangement.spacedBy(4.rdp),
    ) {
        RemoteText(
            RemoteTimeDefaults.defaultTimeString(is24HourFormat = is24Hour.rb),
            fontSize = 40.rsp,
        )
        RemoteText(hh + ":".rs + mm + ":".rs + ss.toRemoteString(), fontSize = 16.rsp)
        // toRemoteString() groups digits as the player's locale does, so a year reads "2,026".
        RemoteText(
            "day ".rs +
                time.dayOfMonth.toRemoteString() +
                " / month ".rs +
                time.month.toRemoteString() +
                " / year ".rs +
                time.year.toRemoteString(),
            fontSize = 14.rsp,
        )
        RemoteText(
            "weekday ".rs +
                time.dayOfWeek.toRemoteString() +
                " · day of year ".rs +
                time.dayOfYear.toRemoteString() +
                " · UTC offset ".rs +
                time.utcOffsetSeconds.toRemoteString() +
                "s".rs,
            fontSize = 12.rsp,
        )
        RemoteText(
            "epoch ".rs +
                time.epochSecond.toRemoteString() +
                " · animation ".rs +
                time.animationTime.toRemoteInt().toRemoteString() +
                "s · frame Δ ".rs +
                (time.animationDeltaTime * 1000f.rf).toRemoteInt().toRemoteString() +
                "ms".rs,
            fontSize = 12.rsp,
        )
    }
}

@Composable
@RemoteComposable
fun Configuration() {
    // RemoteConfiguration is the player's display, known only at play time.
    val config = RemoteConfiguration
    RemoteColumn(verticalArrangement = RemoteArrangement.spacedBy(4.rdp)) {
        RemoteText(
            "window: ".rs +
                config.windowWidthPx.toRemoteString() +
                " × ".rs +
                config.windowHeightPx.toRemoteString() +
                " px".rs,
        )
        RemoteText("density: ".rs + config.density.toRemoteString())
        RemoteText("fontSize: ".rs + config.fontSizePx.toRemoteString() + " px".rs)
        // The window size as dp drives layout: a bar half the window wide.
        RemoteBox(
            RemoteModifier.size(config.windowWidth / 2, 12.rdp).background(Color(0xFF1E88E5).rc),
        )
        // …a square the size of the user's font size, and a bar a tenth of the window high.
        RemoteRow(horizontalArrangement = RemoteArrangement.spacedBy(8.rdp)) {
            RemoteBox(
                RemoteModifier.size(config.fontSize, config.fontSize)
                    .background(Color(0xFFFB8C00).rc),
            )
            RemoteBox(
                RemoteModifier.size(24.rdp, config.windowHeight / 10)
                    .background(Color(0xFF8E24AA).rc),
            )
        }
    }
}

@Composable
@RemoteComposable
fun MutableValues(step: Float, label: String) {
    // Mutable remote values: the document holds them, actions change them.
    val progress = remember { MutableRemoteFloat(0f) }
    val title = remember { MutableRemoteString(label) }
    val taps = remember { MutableRemoteInt(0) }
    RemoteColumn(verticalArrangement = RemoteArrangement.spacedBy(10.rdp)) {
        RemoteBox(RemoteModifier.size(260.rdp, 16.rdp).background(Color(0xFFE0E0E0).rc)) {
            RemoteBox(
                RemoteModifier.size((progress * 260f.rf).toRemoteDp(), 16.rdp)
                    .background(Color(0xFF43A047).rc),
            )
        }
        RemoteText(title + " · taps: ".rs + taps.toRemoteString())
        RemoteBox(
            RemoteModifier.clip(RemoteRoundedCornerShape(12.rdp))
                .background(Color(0xFF1E88E5).rc)
                .clickable(
                    combinedAction(
                        valueChangeAction(progress, (progress + step.rf) % 1.0001f.rf),
                        valueChangeAction(title, "Tapped".rs),
                        valueChangeAction(taps, taps + 1.ri),
                    ),
                )
                .padding(12.rdp),
        ) {
            RemoteText("Advance".rs, color = Color.White.rc)
        }
    }
}

@Composable
@RemoteComposable
fun MutableImage(swap: Boolean) {
    // A mutable bitmap: the document declares an image that can be replaced while it plays.
    val image: MutableRemoteImageBitmap =
        rememberMutableRemoteImageBitmap(
            solidBitmap(if (swap) Color(0xFFE53935) else Color(0xFF1E88E5)),
        )
    RemoteImage(image, "current".rs, RemoteModifier.size(100.rdp))
}

fun solidBitmap(color: Color): ImageBitmap {
    val bitmap = ImageBitmap(32, 32)
    Canvas(bitmap).drawRect(0f, 0f, 32f, 32f, Paint().apply { this.color = color })
    return bitmap
}
