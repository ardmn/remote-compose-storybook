package rcstorybook.stories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import ee.schimke.composeai.rccreation.compose.action.RemoteAction
import ee.schimke.composeai.rccreation.compose.action.combinedAction
import ee.schimke.composeai.rccreation.compose.action.hostAction
import ee.schimke.composeai.rccreation.compose.action.valueChangeAction
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteArrangement
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
import ee.schimke.composeai.rccreation.compose.layout.RemoteRow
import ee.schimke.composeai.rccreation.compose.layout.RemoteText
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.RemoteRole
import ee.schimke.composeai.rccreation.compose.modifier.background
import ee.schimke.composeai.rccreation.compose.modifier.clearAndSetSemantics
import ee.schimke.composeai.rccreation.compose.modifier.clickable
import ee.schimke.composeai.rccreation.compose.modifier.clip
import ee.schimke.composeai.rccreation.compose.modifier.contentDescription
import ee.schimke.composeai.rccreation.compose.modifier.onTouchCancel
import ee.schimke.composeai.rccreation.compose.modifier.onTouchDown
import ee.schimke.composeai.rccreation.compose.modifier.onTouchUp
import ee.schimke.composeai.rccreation.compose.modifier.padding
import ee.schimke.composeai.rccreation.compose.modifier.role
import ee.schimke.composeai.rccreation.compose.modifier.semantics
import ee.schimke.composeai.rccreation.compose.modifier.size
import ee.schimke.composeai.rccreation.compose.modifier.stateDescription
import ee.schimke.composeai.rccreation.compose.shapes.RemoteRoundedCornerShape
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteBoolean
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteInt
import ee.schimke.composeai.rccreation.compose.state.RemoteColor
import ee.schimke.composeai.rccreation.compose.state.rb
import ee.schimke.composeai.rccreation.compose.state.rc
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.ri
import ee.schimke.composeai.rccreation.compose.state.rs
import ee.schimke.composeai.rccreation.compose.state.rsp

@Composable
@RemoteComposable
fun ClickToggle(onColor: Color, offColor: Color) {
    // State lives in the document: the player toggles it on tap, no server round-trip.
    val isOn = remember { MutableRemoteBoolean(false) }
    RemoteBox(
        RemoteModifier.clip(RemoteRoundedCornerShape(12.rdp))
            .background(isOn.select(onColor.rc, offColor.rc))
            .clickable(valueChangeAction(isOn, !isOn), role = RemoteRole.Switch)
            .padding(horizontal = 24.rdp, vertical = 14.rdp),
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteText(
            isOn.select("ON — tap to turn off".rs, "OFF — tap to turn on".rs),
            color = Color.White.rc,
        )
    }
}

@Composable
@RemoteComposable
fun ClickCounter(step: Int) {
    val count = remember { MutableRemoteInt(0) }
    RemoteRow(
        verticalAlignment = RemoteAlignment.CenterVertically,
        horizontalArrangement = RemoteArrangement.spacedBy(16.rdp),
    ) {
        RemoteBox(
            RemoteModifier.size(56.rdp)
                .clip(RemoteRoundedCornerShape(28.rdp))
                .background(Color(0xFF1E88E5).rc)
                .clickable(valueChangeAction(count, count - step.ri), role = RemoteRole.Button),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("−$step".rs, color = Color.White.rc, fontSize = 18.rsp)
        }
        RemoteText(count.toRemoteString(), fontSize = 32.rsp)
        RemoteBox(
            RemoteModifier.size(56.rdp)
                .clip(RemoteRoundedCornerShape(28.rdp))
                .background(Color(0xFF1E88E5).rc)
                .clickable(valueChangeAction(count, count + step.ri), role = RemoteRole.Button),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("+$step".rs, color = Color.White.rc, fontSize = 18.rsp)
        }
    }
}

@Composable
@RemoteComposable
fun HostActions(actionName: String) {
    // A host action is delivered to the app that plays the document (see the Actions tab).
    RemoteColumn(verticalArrangement = RemoteArrangement.spacedBy(12.rdp)) {
        RemoteBox(
            RemoteModifier.clip(RemoteRoundedCornerShape(12.rdp))
                .background(Color(0xFF1E88E5).rc)
                .clickable(hostAction(name = actionName.rs))
                .padding(16.rdp),
        ) {
            RemoteText("hostAction(\"$actionName\")".rs, color = Color.White.rc)
        }
        RemoteBox(
            RemoteModifier.clip(RemoteRoundedCornerShape(12.rdp))
                .background(Color(0xFF43A047).rc)
                .clickable(hostAction(name = actionName.rs, value = 42.ri))
                .padding(16.rdp),
        ) {
            RemoteText("hostAction with a value".rs, color = Color.White.rc)
        }
    }
}

@Composable
@RemoteComposable
fun CombinedActions(actionName: String) {
    val isBookmarked = remember { MutableRemoteBoolean(false) }
    val next = !isBookmarked
    RemoteBox(
        RemoteModifier.clip(RemoteRoundedCornerShape(12.rdp))
            .background(isBookmarked.select(Color(0xFF2E7D32).rc, Color(0xFF616161).rc))
            // Update local state and tell the host, in one tap.
            .clickable(
                combinedAction(
                    valueChangeAction(isBookmarked, next),
                    hostAction(actionName.rs, next),
                ),
            )
            .padding(16.rdp),
    ) {
        RemoteText(
            isBookmarked.select("Bookmarked".rs, "Tap to bookmark".rs),
            color = Color.White.rc,
        )
    }
}

@Composable
@RemoteComposable
fun TouchEvents(pressedColor: Color) {
    val pressed = remember { MutableRemoteBoolean(false) }
    val downs = remember { MutableRemoteInt(0) }
    RemoteBox(
        RemoteModifier.size(220.rdp, 120.rdp)
            .clip(RemoteRoundedCornerShape(16.rdp))
            .background(pressed.select(pressedColor.rc, Color(0xFFB0BEC5).rc))
            .onTouchDown(
                combinedAction(
                    valueChangeAction(pressed, true.rb),
                    valueChangeAction(downs, downs + 1.ri),
                ),
            )
            .onTouchUp(valueChangeAction(pressed, false.rb))
            .onTouchCancel(valueChangeAction(pressed, false.rb)),
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteText(("Press and hold · downs: ".rs + downs.toRemoteString()), color = Color.White.rc)
    }
}

@Composable
@RemoteComposable
fun Semantics(description: String, state: String) {
    RemoteRow(horizontalArrangement = RemoteArrangement.spacedBy(12.rdp)) {
        // semantics adds accessibility properties; see them in the Document tab.
        RemoteBox(
            RemoteModifier.size(120.rdp, 60.rdp).background(Color(0xFF5C6BC0).rc).semantics {
                contentDescription = description.rs
                stateDescription = state.rs
                role = RemoteRole.Checkbox
            },
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("semantics".rs, color = Color.White.rc)
        }
        // clearAndSetSemantics replaces the children's semantics with its own.
        RemoteBox(
            RemoteModifier.size(120.rdp, 60.rdp)
                .background(Color(0xFF8E24AA).rc)
                .clearAndSetSemantics { contentDescription = description.rs },
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText("cleared".rs, color = Color.White.rc)
        }
    }
}

@Composable
@RemoteComposable
fun HostActionById(id: Int, metadata: String, enabled: Boolean) {
    // A host action can be a number the app knows, with optional text; RemoteAction.None is the
    // action that does nothing.
    val action = if (enabled) hostAction(id, metadata.rs) else RemoteAction.None
    RemoteBox(
        RemoteModifier.clip(RemoteRoundedCornerShape(12.rdp))
            .background(if (enabled) Color(0xFF1E88E5).rc else Color(0xFF9E9E9E).rc)
            .clickable(action)
            .padding(16.rdp),
    ) {
        RemoteText(
            (if (enabled) "hostAction($id, \"$metadata\")" else "RemoteAction.None").rs,
            color = RemoteColor.White,
        )
    }
}
