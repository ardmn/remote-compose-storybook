package rcstorybook.stories

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteArrangement
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteCollapsibleColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteCollapsibleRow
import ee.schimke.composeai.rccreation.compose.layout.RemoteColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
import ee.schimke.composeai.rccreation.compose.layout.RemoteFitBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteRow
import ee.schimke.composeai.rccreation.compose.layout.RemoteText
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.background
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxSize
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxWidth
import ee.schimke.composeai.rccreation.compose.modifier.height
import ee.schimke.composeai.rccreation.compose.modifier.padding
import ee.schimke.composeai.rccreation.compose.modifier.size
import ee.schimke.composeai.rccreation.compose.modifier.width
import ee.schimke.composeai.rccreation.compose.state.rc
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.rf
import ee.schimke.composeai.rccreation.compose.state.rs
import ee.schimke.composeai.rccreation.compose.state.rsp
import ee.schimke.composeai.rccreation.foundation.layout.RemoteSpacer

@Composable
@RemoteComposable
fun BoxContentAlignment(contentAlignment: RemoteAlignment, boxSize: Int, childSize: Int) {
    RemoteBox(
        modifier = RemoteModifier.size(boxSize.rdp).background(Color(0xFFE3F2FD).rc),
        contentAlignment = contentAlignment,
    ) {
        RemoteBox(RemoteModifier.size(childSize.rdp).background(Color(0xFF1E88E5).rc))
    }
}

@Composable
@RemoteComposable
fun BoxLayering(offsetStep: Int) {
    RemoteBox(modifier = RemoteModifier.size(220.rdp), contentAlignment = RemoteAlignment.TopStart) {
        RemoteBox(RemoteModifier.size(140.rdp).background(Color(0xFFEF5350).rc))
        RemoteBox(
            RemoteModifier.padding(start = offsetStep.rdp, top = offsetStep.rdp)
                .size(140.rdp)
                .background(Color(0xCC42A5F5).rc)
        )
        RemoteBox(
            RemoteModifier.padding(start = (offsetStep * 2).rdp, top = (offsetStep * 2).rdp)
                .size(140.rdp)
                .background(Color(0xAA66BB6A).rc)
        )
    }
}

@Composable
@RemoteComposable
fun EmptyBox(width: Int, height: Int, color: Color) {
    // The content-less overload: a box that only draws its modifiers.
    RemoteBox(RemoteModifier.size(width.rdp, height.rdp).background(color.rc))
}

@Composable
@RemoteComposable
fun RowArrangement(
    horizontalArrangement: RemoteArrangement.Horizontal,
    verticalAlignment: RemoteAlignment.Vertical,
    itemCount: Int,
) {
    RemoteRow(
        modifier = RemoteModifier.fillMaxWidth().height(120.rdp).background(Color(0xFFF1F3F4).rc),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        for (i in 1..itemCount) {
            RemoteBox(
                modifier = RemoteModifier.size(48.rdp, (24 + i * 14).rdp).background(Color(0xFF7E57C2).rc),
                contentAlignment = RemoteAlignment.Center,
            ) {
                RemoteText("$i".rs, color = Color.White.rc)
            }
        }
    }
}

@Composable
@RemoteComposable
fun RowWeights(firstWeight: Float, secondWeight: Float, thirdWeight: Float) {
    RemoteRow(modifier = RemoteModifier.fillMaxWidth().height(64.rdp)) {
        RemoteBox(
            RemoteModifier.weight(firstWeight.rf).height(64.rdp).background(Color(0xFFEF5350).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("$firstWeight".rs, color = Color.White.rc) }
        RemoteBox(
            RemoteModifier.weight(secondWeight.rf).height(64.rdp).background(Color(0xFF42A5F5).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("$secondWeight".rs, color = Color.White.rc) }
        RemoteBox(
            RemoteModifier.weight(thirdWeight.rf).height(64.rdp).background(Color(0xFF66BB6A).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("$thirdWeight".rs, color = Color.White.rc) }
    }
}

@Composable
@RemoteComposable
fun ColumnArrangement(
    verticalArrangement: RemoteArrangement.Vertical,
    horizontalAlignment: RemoteAlignment.Horizontal,
    itemCount: Int,
) {
    RemoteColumn(
        modifier = RemoteModifier.width(220.rdp).height(300.rdp).background(Color(0xFFF1F3F4).rc),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
    ) {
        for (i in 1..itemCount) {
            RemoteBox(
                modifier = RemoteModifier.size((40 + i * 24).rdp, 40.rdp).background(Color(0xFF26A69A).rc),
                contentAlignment = RemoteAlignment.Center,
            ) {
                RemoteText("Item $i".rs, color = Color.White.rc)
            }
        }
    }
}

@Composable
@RemoteComposable
fun ColumnWeight(headerHeight: Int, footerHeight: Int) {
    RemoteColumn(modifier = RemoteModifier.width(220.rdp).height(300.rdp)) {
        RemoteBox(
            RemoteModifier.fillMaxWidth().height(headerHeight.rdp).background(Color(0xFF5C6BC0).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("Header".rs, color = Color.White.rc) }
        // weight(1f) takes whatever height the header and footer leave.
        RemoteBox(
            RemoteModifier.weight(1f.rf).fillMaxWidth().background(Color(0xFFE8EAF6).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("weight(1f)".rs) }
        RemoteBox(
            RemoteModifier.fillMaxWidth().height(footerHeight.rdp).background(Color(0xFF5C6BC0).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("Footer".rs, color = Color.White.rc) }
    }
}

@Composable
@RemoteComposable
fun SpacedBy(space: Int, vertical: Boolean) {
    if (vertical) {
        RemoteColumn(verticalArrangement = RemoteArrangement.spacedBy(space.rdp)) {
            for (i in 1..4) RemoteBox(RemoteModifier.size(120.rdp, 32.rdp).background(Color(0xFFFFA726).rc))
        }
    } else {
        RemoteRow(horizontalArrangement = RemoteArrangement.spacedBy(space.rdp)) {
            for (i in 1..4) RemoteBox(RemoteModifier.size(40.rdp, 80.rdp).background(Color(0xFFFFA726).rc))
        }
    }
}

@Composable
@RemoteComposable
fun SpacerBetween(gap: Int, pushApart: Boolean) {
    RemoteRow(modifier = RemoteModifier.width(280.rdp).background(Color(0xFFF1F3F4).rc).padding(8.rdp)) {
        RemoteText("Left".rs)
        if (pushApart) {
            // A weighted spacer pushes the next child to the far end.
            RemoteSpacer(RemoteModifier.weight(1f.rf))
        } else {
            RemoteSpacer(RemoteModifier.width(gap.rdp))
        }
        RemoteText("Right".rs)
    }
}

@Composable
@RemoteComposable
fun CollapsibleColumnPriorities(height: Int) {
    // Children that do not fit are dropped, lowest collapsiblePriority first.
    RemoteCollapsibleColumn(
        modifier = RemoteModifier.width(240.rdp).height(height.rdp).background(Color(0xFFF1F3F4).rc)
    ) {
        RemoteBox(
            RemoteModifier.fillMaxWidth().height(80.rdp).background(Color(0xFFEF5350).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("Always (no priority)".rs, color = Color.White.rc) }
        RemoteBox(
            RemoteModifier.collapsiblePriority(1f).fillMaxWidth().height(80.rdp).background(Color(0xFF42A5F5).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("Priority 1 (goes first)".rs, color = Color.White.rc) }
        RemoteBox(
            RemoteModifier.collapsiblePriority(5f).fillMaxWidth().height(80.rdp).background(Color(0xFF66BB6A).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("Priority 5".rs, color = Color.White.rc) }
    }
}

@Composable
@RemoteComposable
fun CollapsibleRowPriorities(width: Int) {
    RemoteCollapsibleRow(
        modifier = RemoteModifier.width(width.rdp).height(96.rdp).background(Color(0xFFF1F3F4).rc),
        horizontalArrangement = RemoteArrangement.Start,
        verticalAlignment = RemoteAlignment.CenterVertically,
    ) {
        RemoteBox(
            RemoteModifier.size(90.rdp, 80.rdp).background(Color(0xFFEF5350).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("Always".rs, color = Color.White.rc) }
        RemoteBox(
            RemoteModifier.collapsiblePriority(1f).size(90.rdp, 80.rdp).background(Color(0xFF42A5F5).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("Low".rs, color = Color.White.rc) }
        RemoteBox(
            RemoteModifier.collapsiblePriority(5f).size(90.rdp, 80.rdp).background(Color(0xFF66BB6A).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("Medium".rs, color = Color.White.rc) }
        RemoteBox(
            RemoteModifier.collapsiblePriority(3f).weight(1f).height(80.rdp).background(Color(0xFFAB47BC).rc),
            contentAlignment = RemoteAlignment.Center,
        ) { RemoteText("weight".rs, color = Color.White.rc) }
    }
}

@Composable
@RemoteComposable
fun FitBoxResponsive(containerSize: Int) {
    // RemoteFitBox shows the first child that fits the space it is given.
    RemoteBox(
        modifier = RemoteModifier.size(containerSize.rdp).background(Color(0xFFF1F3F4).rc),
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteFitBox(
            modifier = RemoteModifier.fillMaxSize(),
            horizontalAlignment = RemoteAlignment.CenterHorizontally,
            verticalArrangement = RemoteArrangement.Center,
        ) {
            RemoteBox(
                RemoteModifier.size(220.rdp).background(Color(0xFF1E88E5).rc),
                contentAlignment = RemoteAlignment.Center,
            ) { RemoteText("Large (220)".rs, color = Color.White.rc, fontSize = 22.rsp) }
            RemoteBox(
                RemoteModifier.size(140.rdp).background(Color(0xFF43A047).rc),
                contentAlignment = RemoteAlignment.Center,
            ) { RemoteText("Medium (140)".rs, color = Color.White.rc) }
            RemoteBox(
                RemoteModifier.size(70.rdp).background(Color(0xFFE53935).rc),
                contentAlignment = RemoteAlignment.Center,
            ) { RemoteText("S".rs, color = Color.White.rc) }
        }
    }
}
