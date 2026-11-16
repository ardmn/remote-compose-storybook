package rcstorybook.stories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import ee.schimke.composeai.rccreation.compose.action.combinedAction
import ee.schimke.composeai.rccreation.compose.action.hostAction
import ee.schimke.composeai.rccreation.compose.action.valueChangeAction
import ee.schimke.composeai.rccreation.compose.capture.RemoteImageVector
import ee.schimke.composeai.rccreation.compose.capture.path
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteArrangement
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteCanvas
import ee.schimke.composeai.rccreation.compose.layout.RemoteColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
import ee.schimke.composeai.rccreation.compose.layout.RemoteContentScale
import ee.schimke.composeai.rccreation.compose.layout.RemoteFitBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteImage
import ee.schimke.composeai.rccreation.compose.layout.RemoteOffset
import ee.schimke.composeai.rccreation.compose.layout.RemoteRow
import ee.schimke.composeai.rccreation.compose.layout.RemoteSize
import ee.schimke.composeai.rccreation.compose.layout.RemoteStateLayout
import ee.schimke.composeai.rccreation.compose.layout.RemoteText
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.RemoteRole
import ee.schimke.composeai.rccreation.compose.modifier.background
import ee.schimke.composeai.rccreation.compose.modifier.border
import ee.schimke.composeai.rccreation.compose.modifier.clickable
import ee.schimke.composeai.rccreation.compose.modifier.clip
import ee.schimke.composeai.rccreation.compose.modifier.contentDescription
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxSize
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxWidth
import ee.schimke.composeai.rccreation.compose.modifier.height
import ee.schimke.composeai.rccreation.compose.modifier.padding
import ee.schimke.composeai.rccreation.compose.modifier.semantics
import ee.schimke.composeai.rccreation.compose.modifier.size
import ee.schimke.composeai.rccreation.compose.modifier.stateDescription
import ee.schimke.composeai.rccreation.compose.modifier.width
import ee.schimke.composeai.rccreation.compose.shaders.RemoteBrush
import ee.schimke.composeai.rccreation.compose.shaders.solidColor
import ee.schimke.composeai.rccreation.compose.shapes.RemoteCircleShape
import ee.schimke.composeai.rccreation.compose.shapes.RemoteRoundedCornerShape
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteBoolean
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteInt
import ee.schimke.composeai.rccreation.compose.state.RemoteImageBitmap
import ee.schimke.composeai.rccreation.compose.state.RemotePaint
import ee.schimke.composeai.rccreation.compose.state.RemotePaintingStyle
import ee.schimke.composeai.rccreation.compose.state.RemoteStrokeCap
import ee.schimke.composeai.rccreation.compose.state.RemoteTimeVariables
import ee.schimke.composeai.rccreation.compose.state.rc
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteBoolean
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteFloat
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteInt
import ee.schimke.composeai.rccreation.compose.state.rememberNamedRemoteString
import ee.schimke.composeai.rccreation.compose.state.rf
import ee.schimke.composeai.rccreation.compose.state.ri
import ee.schimke.composeai.rccreation.compose.state.rs
import ee.schimke.composeai.rccreation.compose.state.rsp
import ee.schimke.composeai.rccreation.compose.text.RemoteFontWeight
import ee.schimke.composeai.rccreation.compose.text.RemoteTextOverflow
import ee.schimke.composeai.rccreation.foundation.icon.RemoteBasicIcon

/**
 * A shop's product card: image, title, price, and a cart button whose count lives in the document
 * while every tap also tells the app.
 */
@Composable
@RemoteComposable
fun ProductCard(title: String, price: String, inStock: Boolean, accent: Color) {
    val photo = remember {
        val bitmap = ImageBitmap(160, 120)
        Canvas(bitmap).apply {
            drawRect(0f, 0f, 160f, 120f, Paint().apply { color = Color(0xFFFFF3E0) })
            drawCircle(Offset(80f, 62f), 38f, Paint().apply { color = Color(0xFFFF7043) })
            drawRect(76f, 14f, 84f, 30f, Paint().apply { color = Color(0xFF6D4C41) })
        }
        RemoteImageBitmap(bitmap)
    }
    val inCart = remember { MutableRemoteInt(0) }
    RemoteColumn(
        RemoteModifier.width(220.rdp)
            .clip(RemoteRoundedCornerShape(20.rdp))
            .background(Color.White.rc)
            .border(1.rdp, Color(0xFFE0E0E0).rc, RemoteRoundedCornerShape(20.rdp)),
    ) {
        RemoteImage(
            photo,
            contentDescription = title.rs,
            modifier = RemoteModifier.fillMaxWidth().height(140.rdp),
            contentScale = RemoteContentScale.Crop,
        )
        RemoteColumn(
            RemoteModifier.padding(14.rdp),
            verticalArrangement = RemoteArrangement.spacedBy(6.rdp),
        ) {
            RemoteText(
                title.rs,
                fontSize = 16.rsp,
                fontWeight = RemoteFontWeight.SemiBold,
                maxLines = 1,
                overflow = RemoteTextOverflow.Ellipsis,
            )
            RemoteText(
                price.rs,
                fontSize = 20.rsp,
                fontWeight = RemoteFontWeight.Bold,
                color = accent.rc,
            )
            if (inStock) {
                RemoteBox(
                    RemoteModifier.fillMaxWidth()
                        .clip(RemoteRoundedCornerShape(12.rdp))
                        .background(accent.rc)
                        .clickable(
                            combinedAction(
                                valueChangeAction(inCart, inCart + 1.ri),
                                hostAction("add_to_cart".rs, title.rs),
                            ),
                            role = RemoteRole.Button,
                        )
                        .padding(vertical = 10.rdp),
                    contentAlignment = RemoteAlignment.Center,
                ) {
                    RemoteText(
                        inCart
                            .isGreaterThan(0.ri)
                            .select(
                                "In cart: ".rs + inCart.toRemoteString(),
                                "Add to cart".rs,
                            ),
                        color = Color.White.rc,
                        fontWeight = RemoteFontWeight.Medium,
                    )
                }
            } else {
                RemoteText("Out of stock".rs, color = Color(0xFF9E9E9E).rc)
            }
        }
    }
}

/**
 * A weather widget at three sizes. The temperature and the condition are named values the app
 * refreshes (try the Host values tab); the clock is the player's own.
 */
@Composable
@RemoteComposable
fun WeatherWidget(city: String, temperature: Int, condition: String) {
    val temp = rememberNamedRemoteInt("temperature", temperature)
    val sky = rememberNamedRemoteString("condition", condition)
    val time = RemoteTimeVariables
    val clock =
        time.hourOfDay.toRemoteString() + ":".rs + (time.minuteOfDay % 60.ri).toRemoteString()
    val gradientTop = Color(0xFF42A5F5).rc
    RemoteBox(
        RemoteModifier.fillMaxSize().clip(RemoteRoundedCornerShape(24.rdp)).background(gradientTop),
        contentAlignment = RemoteAlignment.Center,
    ) {
        // The player picks the largest layout that fits the widget's size.
        RemoteFitBox(
            RemoteModifier.fillMaxSize(),
            horizontalAlignment = RemoteAlignment.CenterHorizontally,
            verticalArrangement = RemoteArrangement.Center,
        ) {
            RemoteColumn(
                RemoteModifier.size(300.rdp, 160.rdp).padding(16.rdp),
                verticalArrangement = RemoteArrangement.SpaceBetween,
            ) {
                RemoteRow(
                    RemoteModifier.fillMaxWidth(),
                    horizontalArrangement = RemoteArrangement.SpaceBetween,
                ) {
                    RemoteText(city.rs, color = Color.White.rc, fontSize = 18.rsp)
                    RemoteText(clock, color = Color(0xCCFFFFFF).rc, fontSize = 14.rsp)
                }
                RemoteText(
                    temp.toRemoteString() + "°".rs,
                    color = Color.White.rc,
                    fontSize = 56.rsp,
                    fontWeight = RemoteFontWeight.Light,
                )
                RemoteText(sky, color = Color.White.rc, fontSize = 16.rsp)
            }
            RemoteColumn(
                RemoteModifier.size(150.rdp, 150.rdp).padding(12.rdp),
                horizontalAlignment = RemoteAlignment.CenterHorizontally,
                verticalArrangement = RemoteArrangement.Center,
            ) {
                RemoteText(city.rs, color = Color.White.rc, fontSize = 14.rsp, maxLines = 1)
                RemoteText(
                    temp.toRemoteString() + "°".rs,
                    color = Color.White.rc,
                    fontSize = 44.rsp,
                )
                RemoteText(sky, color = Color.White.rc, fontSize = 12.rsp, maxLines = 1)
            }
            RemoteBox(RemoteModifier.size(72.rdp), contentAlignment = RemoteAlignment.Center) {
                RemoteText(
                    temp.toRemoteString() + "°".rs,
                    color = Color.White.rc,
                    fontSize = 28.rsp,
                )
            }
        }
    }
}

/** A three-step onboarding: every step is in the document, the player moves between them. */
@Composable
@RemoteComposable
fun Onboarding(accent: Color) {
    val step = remember { MutableRemoteInt(0) }
    RemoteColumn(
        RemoteModifier.size(300.rdp, 320.rdp)
            .clip(RemoteRoundedCornerShape(24.rdp))
            .background(Color.White.rc)
            .padding(20.rdp),
        verticalArrangement = RemoteArrangement.SpaceBetween,
        horizontalAlignment = RemoteAlignment.CenterHorizontally,
    ) {
        // A fixed-height box around the steps: the web player otherwise lets a state layout take
        // its
        // parent's whole height.
        RemoteBox(RemoteModifier.fillMaxWidth().height(200.rdp)) {
            RemoteStateLayout(step, 0, 1, 2, modifier = RemoteModifier.fillMaxSize()) { index ->
                val (title, body) =
                    when (index) {
                        0 ->
                            "Welcome" to
                                "Screens like this one arrive as data, not as an app update."
                        1 ->
                            "Interactive" to
                                "Taps, steps and animations run on the device, offline."
                        else ->
                            "Always fresh" to
                                "The server can change this flow any day, for every user."
                    }
                RemoteColumn(
                    RemoteModifier.fillMaxSize(),
                    horizontalAlignment = RemoteAlignment.CenterHorizontally,
                    verticalArrangement =
                        RemoteArrangement.spacedBy(12.rdp, RemoteAlignment.CenterVertically),
                ) {
                    RemoteBox(
                        RemoteModifier.size(72.rdp).clip(RemoteCircleShape).background(accent.rc),
                        contentAlignment = RemoteAlignment.Center,
                    ) {
                        RemoteText("${index + 1}".rs, color = Color.White.rc, fontSize = 28.rsp)
                    }
                    RemoteText(title.rs, fontSize = 22.rsp, fontWeight = RemoteFontWeight.Bold)
                    RemoteText(body.rs, fontSize = 14.rsp, color = Color(0xFF5F6368).rc)
                }
            }
        }
        RemoteRow(
            horizontalArrangement = RemoteArrangement.spacedBy(8.rdp),
            verticalAlignment = RemoteAlignment.CenterVertically,
        ) {
            for (dot in 0..2) {
                RemoteBox(
                    RemoteModifier.size(8.rdp)
                        .clip(RemoteCircleShape)
                        .background(step.isEqualTo(dot.ri).select(accent.rc, Color(0xFFDADCE0).rc)),
                )
            }
        }
        RemoteBox(
            RemoteModifier.fillMaxWidth()
                .clip(RemoteRoundedCornerShape(12.rdp))
                .background(accent.rc)
                .clickable(valueChangeAction(step, (step + 1.ri) % 3.ri), role = RemoteRole.Button)
                .padding(vertical = 12.rdp),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText(
                step.isEqualTo(2.ri).select("Start over".rs, "Next".rs),
                color = Color.White.rc,
            )
        }
    }
}

/** Media controls: a play/pause toggle drives a progress bar the player animates from the clock. */
@Composable
@RemoteComposable
fun MediaPlayer(track: String, artist: String, trackSeconds: Int) {
    val playing = remember { MutableRemoteBoolean(false) }
    RemoteColumn(
        RemoteModifier.width(300.rdp)
            .clip(RemoteRoundedCornerShape(20.rdp))
            .background(Color(0xFF212121).rc)
            .padding(16.rdp),
        verticalArrangement = RemoteArrangement.spacedBy(12.rdp),
    ) {
        RemoteText(
            track.rs,
            color = Color.White.rc,
            fontSize = 18.rsp,
            fontWeight = RemoteFontWeight.SemiBold,
            maxLines = 1,
            overflow = RemoteTextOverflow.Ellipsis,
        )
        RemoteText(artist.rs, color = Color(0xFFBDBDBD).rc, fontSize = 14.rsp)
        RemoteCanvas(RemoteModifier.fillMaxWidth().height(6.rdp)) {
            val track = RemotePaint {
                color = Color(0xFF424242).rc
                strokeWidth = height
                strokeCap = RemoteStrokeCap.Round
                style = RemotePaintingStyle.Stroke
            }
            drawLine(
                track,
                RemoteOffset(height / 2f.rf, height / 2f.rf),
                RemoteOffset(width - height / 2f.rf, height / 2f.rf),
            )
            // Progress from the player's clock while playing; the document is never resent.
            val fraction =
                playing.select(
                    (RemoteTimeVariables.continuousSeconds % trackSeconds.toFloat().rf) /
                        trackSeconds.toFloat().rf,
                    0.08f.rf,
                )
            val fill = RemotePaint {
                color = Color(0xFF1DB954).rc
                strokeWidth = height
                strokeCap = RemoteStrokeCap.Round
                style = RemotePaintingStyle.Stroke
            }
            drawLine(
                fill,
                RemoteOffset(height / 2f.rf, height / 2f.rf),
                RemoteOffset(height / 2f.rf + (width - height) * fraction, height / 2f.rf),
            )
        }
        RemoteRow(RemoteModifier.fillMaxWidth(), horizontalArrangement = RemoteArrangement.Center) {
            RemoteBox(
                RemoteModifier.size(56.rdp)
                    .clip(RemoteCircleShape)
                    .background(Color.White.rc)
                    .clickable(
                        combinedAction(
                            valueChangeAction(playing, !playing),
                            hostAction("play_pause".rs, !playing),
                        ),
                        role = RemoteRole.Button,
                    )
                    .semantics { contentDescription = playing.select("Pause".rs, "Play".rs) },
                contentAlignment = RemoteAlignment.Center,
            ) {
                RemoteStateLayout(playing) { isPlaying ->
                    RemoteBasicIcon(
                        if (isPlaying) pauseIcon() else playIcon(),
                        contentDescription = null,
                        modifier = RemoteModifier.size(28.rdp),
                        tint = Color(0xFF212121).rc,
                    )
                }
            }
        }
    }
}

/** A settings screen: switches whose state is kept on the device, each also reported to the app. */
@Composable
@RemoteComposable
fun SettingsScreen(accent: Color) {
    val wifi = rememberNamedRemoteBoolean("wifi", true)
    val notifications = remember { MutableRemoteBoolean(true) }
    val darkMode = remember { MutableRemoteBoolean(false) }
    val brightness = rememberNamedRemoteFloat("brightness", 0.6f.rf)
    RemoteColumn(
        RemoteModifier.width(320.rdp)
            .clip(RemoteRoundedCornerShape(20.rdp))
            .background(Color.White.rc)
            .padding(vertical = 8.rdp),
    ) {
        RemoteRow(
            RemoteModifier.fillMaxWidth().padding(horizontal = 16.rdp, vertical = 12.rdp),
            verticalAlignment = RemoteAlignment.CenterVertically,
        ) {
            RemoteText("Wi-Fi (set by the app)".rs, modifier = RemoteModifier.weight(1f.rf))
            RemoteText(wifi.select("On".rs, "Off".rs), color = accent.rc)
        }
        SettingSwitch("Notifications", notifications, accent)
        SettingSwitch("Dark mode", darkMode, accent)
        RemoteColumn(
            RemoteModifier.fillMaxWidth().padding(horizontal = 16.rdp, vertical = 12.rdp),
            verticalArrangement = RemoteArrangement.spacedBy(8.rdp),
        ) {
            RemoteText("Brightness (set by the app)".rs)
            RemoteCanvas(RemoteModifier.fillMaxWidth().height(8.rdp)) {
                drawRoundRect(
                    RemotePaint { color = Color(0xFFE0E0E0).rc },
                    size = size,
                    cornerRadius = RemoteOffset(4f.rf, 4f.rf),
                )
                drawRoundRect(
                    RemotePaint { color = accent.rc },
                    size = RemoteSize(width * brightness, height),
                    cornerRadius = RemoteOffset(4f.rf, 4f.rf),
                )
            }
        }
    }
}

@Composable
@RemoteComposable
fun SettingSwitch(label: String, checked: MutableRemoteBoolean, accent: Color) {
    RemoteRow(
        RemoteModifier.fillMaxWidth()
            .clickable(
                combinedAction(
                    valueChangeAction(checked, !checked),
                    hostAction(label.rs, !checked),
                ),
                role = RemoteRole.Switch,
            )
            .semantics { stateDescription = checked.select("On".rs, "Off".rs) }
            .padding(horizontal = 16.rdp, vertical = 12.rdp),
        verticalAlignment = RemoteAlignment.CenterVertically,
    ) {
        RemoteText(label.rs, modifier = RemoteModifier.weight(1f.rf))
        RemoteBox(
            RemoteModifier.size(44.rdp, 26.rdp)
                .clip(RemoteRoundedCornerShape(13.rdp))
                .background(checked.select(accent.rc, Color(0xFFBDBDBD).rc))
                .padding(3.rdp),
            contentAlignment = RemoteAlignment.CenterStart,
        ) {
            // The knob slides by its start padding, chosen by the state on the device.
            RemoteBox(
                RemoteModifier.padding(start = checked.select(18.rdp, 0.rdp))
                    .size(20.rdp)
                    .clip(RemoteCircleShape)
                    .background(Color.White.rc),
            )
        }
    }
}

fun playIcon(): RemoteImageVector =
    RemoteImageVector.Builder(
            viewportWidth = 24f.rf,
            viewportHeight = 24f.rf,
            tintColor = Color.Black.rc,
            name = "Play",
        )
        .path(fill = RemoteBrush.solidColor(Color.Black.rc)) {
            moveTo(8f.rf, 5f.rf)
            lineTo(19f.rf, 12f.rf)
            lineTo(8f.rf, 19f.rf)
            close()
        }
        .build()

fun pauseIcon(): RemoteImageVector =
    RemoteImageVector.Builder(
            viewportWidth = 24f.rf,
            viewportHeight = 24f.rf,
            tintColor = Color.Black.rc,
            name = "Pause",
        )
        .path(fill = RemoteBrush.solidColor(Color.Black.rc)) {
            addRect(6f.rf, 5f.rf, 10f.rf, 19f.rf)
            addRect(14f.rf, 5f.rf, 18f.rf, 19f.rf)
        }
        .build()
