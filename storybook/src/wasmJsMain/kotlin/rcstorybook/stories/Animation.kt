package rcstorybook.stories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import ee.schimke.composeai.rccreation.compose.action.valueChangeAction
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteArrangement
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
import ee.schimke.composeai.rccreation.compose.layout.RemoteRow
import ee.schimke.composeai.rccreation.compose.layout.RemoteStateLayout
import ee.schimke.composeai.rccreation.compose.layout.RemoteText
import ee.schimke.composeai.rccreation.compose.modifier.RemoteEnterTransition
import ee.schimke.composeai.rccreation.compose.modifier.RemoteExitTransition
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.alpha
import ee.schimke.composeai.rccreation.compose.modifier.animateEnterExit
import ee.schimke.composeai.rccreation.compose.modifier.animationSpec
import ee.schimke.composeai.rccreation.compose.modifier.background
import ee.schimke.composeai.rccreation.compose.modifier.clickable
import ee.schimke.composeai.rccreation.compose.modifier.clip
import ee.schimke.composeai.rccreation.compose.modifier.padding
import ee.schimke.composeai.rccreation.compose.modifier.remoteFadeIn
import ee.schimke.composeai.rccreation.compose.modifier.remoteFadeOut
import ee.schimke.composeai.rccreation.compose.modifier.sharedBounds
import ee.schimke.composeai.rccreation.compose.modifier.sharedElement
import ee.schimke.composeai.rccreation.compose.modifier.size
import ee.schimke.composeai.rccreation.compose.modifier.visibility
import ee.schimke.composeai.rccreation.compose.shapes.RemoteRoundedCornerShape
import ee.schimke.composeai.rccreation.compose.state.MutableRemoteBoolean
import ee.schimke.composeai.rccreation.compose.state.RemoteEasing
import ee.schimke.composeai.rccreation.compose.state.animateRemoteDpAsState
import ee.schimke.composeai.rccreation.compose.state.animateRemoteFloatAsState
import ee.schimke.composeai.rccreation.compose.state.rc
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.remoteSpring
import ee.schimke.composeai.rccreation.compose.state.remoteTween
import ee.schimke.composeai.rccreation.compose.state.rf
import ee.schimke.composeai.rccreation.compose.state.ri
import ee.schimke.composeai.rccreation.compose.state.rs

@Composable
@RemoteComposable
fun AnimateFloat(durationMillis: Int, easing: RemoteEasing, dimmedAlpha: Float) {
    val isDimmed = remember { MutableRemoteBoolean(false) }
    // The player animates between targets itself whenever the state flips.
    val animatedAlpha = animateRemoteFloatAsState(
        targetValue = isDimmed.select(dimmedAlpha.rf, 1f.rf),
        animationSpec = remoteTween(durationMillis = durationMillis, easing = easing),
    )
    RemoteBox(
        RemoteModifier.size(180.rdp, 72.rdp)
            .alpha(animatedAlpha)
            .clip(RemoteRoundedCornerShape(16.rdp))
            .background(Color(0xFF1E88E5).rc)
            .clickable(valueChangeAction(isDimmed, !isDimmed)),
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteText("Tap to fade".rs, color = Color.White.rc)
    }
}

@Composable
@RemoteComposable
fun AnimateDpSpring(dampingRatio: Float, stiffness: Float, small: Int, large: Int) {
    val isExpanded = remember { MutableRemoteBoolean(false) }
    val animatedSize = animateRemoteDpAsState(
        targetValue = isExpanded.select(large.rdp, small.rdp),
        animationSpec = remoteSpring(dampingRatio = dampingRatio, stiffness = stiffness),
    )
    RemoteBox(
        RemoteModifier.size(animatedSize)
            .clip(RemoteRoundedCornerShape(20.rdp))
            .background(Color(0xFF2E7D32).rc)
            .clickable(valueChangeAction(isExpanded, !isExpanded)),
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteText("Tap".rs, color = Color.White.rc)
    }
}

@Composable
@RemoteComposable
fun EnterExitTransitions(enter: RemoteEnterTransition, exit: RemoteExitTransition, durationMillis: Int) {
    val shown = remember { MutableRemoteBoolean(true) }
    RemoteColumn(horizontalAlignment = RemoteAlignment.CenterHorizontally, verticalArrangement = RemoteArrangement.spacedBy(16.rdp)) {
        RemoteBox(
            RemoteModifier.clip(RemoteRoundedCornerShape(12.rdp)).background(Color(0xFF424242).rc)
                .clickable(valueChangeAction(shown, !shown)).padding(12.rdp),
        ) { RemoteText(shown.select("Hide".rs, "Show".rs), color = Color.White.rc) }
        RemoteBox(RemoteModifier.size(160.rdp), contentAlignment = RemoteAlignment.Center) {
            // A visibility change on a component with animateEnterExit plays its transitions.
            RemoteBox(
                RemoteModifier.animateEnterExit(enter = enter, exit = exit, spec = remoteTween(durationMillis))
                    .visibility(shown.select(1.ri, 0.ri))
                    .size(120.rdp)
                    .background(Color(0xFFF4511E).rc),
            )
        }
    }
}

@Composable
@RemoteComposable
fun FadeHelpers(durationMillis: Int) {
    val shown = remember { MutableRemoteBoolean(true) }
    RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically, horizontalArrangement = RemoteArrangement.spacedBy(16.rdp)) {
        RemoteBox(
            RemoteModifier.clip(RemoteRoundedCornerShape(12.rdp)).background(Color(0xFF424242).rc)
                .clickable(valueChangeAction(shown, !shown)).padding(12.rdp),
        ) { RemoteText("Toggle".rs, color = Color.White.rc) }
        RemoteBox(
            RemoteModifier.animationSpec(
                motionSpec = remoteTween(durationMillis),
                visibilitySpec = remoteTween(durationMillis),
                enter = remoteFadeIn(),
                exit = remoteFadeOut(),
            ).visibility(shown.select(1.ri, 2.ri)).size(100.rdp).background(Color(0xFF8E24AA).rc),
        )
    }
}

@Composable
@RemoteComposable
fun SharedElement(durationMillis: Int, bounds: Boolean) {
    val expanded = remember { MutableRemoteBoolean(false) }
    // The same key in both states: the player moves the element between them.
    val shared = if (bounds) RemoteModifier.sharedBounds(1, remoteTween(durationMillis))
    else RemoteModifier.sharedElement(1, remoteTween(durationMillis))
    RemoteStateLayout(expanded, RemoteModifier.clickable(valueChangeAction(expanded, !expanded))) { isExpanded ->
        if (isExpanded) {
            RemoteBox(RemoteModifier.size(300.rdp, 220.rdp), contentAlignment = RemoteAlignment.BottomEnd) {
                RemoteBox(shared.size(200.rdp, 120.rdp).background(Color(0xFF1E88E5).rc), contentAlignment = RemoteAlignment.Center) {
                    RemoteText("Tap to collapse".rs, color = Color.White.rc)
                }
            }
        } else {
            RemoteBox(RemoteModifier.size(300.rdp, 220.rdp), contentAlignment = RemoteAlignment.TopStart) {
                RemoteBox(shared.size(90.rdp).background(Color(0xFF1E88E5).rc), contentAlignment = RemoteAlignment.Center) {
                    RemoteText("Tap".rs, color = Color.White.rc)
                }
            }
        }
    }
}

@Composable
@RemoteComposable
fun AnimationToggle(enabled: Boolean) {
    val wide = remember { MutableRemoteBoolean(false) }
    // animationSpec(enabled) switches layout animation on or off for a component.
    RemoteBox(
        RemoteModifier.animationSpec(enabled)
            .size(wide.select(260.rdp, 120.rdp), 80.rdp)
            .background(Color(0xFF00897B).rc)
            .clickable(valueChangeAction(wide, !wide)),
        contentAlignment = RemoteAlignment.Center,
    ) { RemoteText("Tap to resize".rs, color = Color.White.rc) }
}
