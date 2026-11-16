package rcstorybook.catalog

import androidx.compose.ui.graphics.Color
import ee.schimke.composeai.rccreation.compose.layout.RemoteAbsoluteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteArrangement

/** Shared option lists, each option carrying the Kotlin the Code tab prints for it. */
internal object Options {
    val alignments = listOf(
        option("TopStart", "RemoteAlignment.TopStart", RemoteAlignment.TopStart),
        option("TopCenter", "RemoteAlignment.TopCenter", RemoteAlignment.TopCenter),
        option("TopEnd", "RemoteAlignment.TopEnd", RemoteAlignment.TopEnd),
        option("CenterStart", "RemoteAlignment.CenterStart", RemoteAlignment.CenterStart),
        option("Center", "RemoteAlignment.Center", RemoteAlignment.Center),
        option("CenterEnd", "RemoteAlignment.CenterEnd", RemoteAlignment.CenterEnd),
        option("BottomStart", "RemoteAlignment.BottomStart", RemoteAlignment.BottomStart),
        option("BottomCenter", "RemoteAlignment.BottomCenter", RemoteAlignment.BottomCenter),
        option("BottomEnd", "RemoteAlignment.BottomEnd", RemoteAlignment.BottomEnd),
        option("Absolute TopLeft", "RemoteAbsoluteAlignment.TopLeft", RemoteAbsoluteAlignment.TopLeft),
        option("Absolute TopRight", "RemoteAbsoluteAlignment.TopRight", RemoteAbsoluteAlignment.TopRight),
        option("Absolute CenterLeft", "RemoteAbsoluteAlignment.CenterLeft", RemoteAbsoluteAlignment.CenterLeft),
        option("Absolute CenterRight", "RemoteAbsoluteAlignment.CenterRight", RemoteAbsoluteAlignment.CenterRight),
        option("Absolute BottomLeft", "RemoteAbsoluteAlignment.BottomLeft", RemoteAbsoluteAlignment.BottomLeft),
        option("Absolute BottomRight", "RemoteAbsoluteAlignment.BottomRight", RemoteAbsoluteAlignment.BottomRight),
    )

    val horizontalArrangements = listOf<Option<RemoteArrangement.Horizontal>>(
        option("Start", "RemoteArrangement.Start", RemoteArrangement.Start),
        option("Center", "RemoteArrangement.Center", RemoteArrangement.Center),
        option("End", "RemoteArrangement.End", RemoteArrangement.End),
        option("SpaceBetween", "RemoteArrangement.SpaceBetween", RemoteArrangement.SpaceBetween),
        option("SpaceAround", "RemoteArrangement.SpaceAround", RemoteArrangement.SpaceAround),
        option("SpaceEvenly", "RemoteArrangement.SpaceEvenly", RemoteArrangement.SpaceEvenly),
    )

    val verticalArrangements = listOf<Option<RemoteArrangement.Vertical>>(
        option("Top", "RemoteArrangement.Top", RemoteArrangement.Top),
        option("Center", "RemoteArrangement.Center", RemoteArrangement.Center),
        option("Bottom", "RemoteArrangement.Bottom", RemoteArrangement.Bottom),
        option("SpaceBetween", "RemoteArrangement.SpaceBetween", RemoteArrangement.SpaceBetween),
        option("SpaceAround", "RemoteArrangement.SpaceAround", RemoteArrangement.SpaceAround),
        option("SpaceEvenly", "RemoteArrangement.SpaceEvenly", RemoteArrangement.SpaceEvenly),
    )

    val verticalAlignments = listOf<Option<RemoteAlignment.Vertical>>(
        option("Top", "RemoteAlignment.Top", RemoteAlignment.Top),
        option("CenterVertically", "RemoteAlignment.CenterVertically", RemoteAlignment.CenterVertically),
        option("Bottom", "RemoteAlignment.Bottom", RemoteAlignment.Bottom),
    )

    val horizontalAlignments = listOf<Option<RemoteAlignment.Horizontal>>(
        option("Start", "RemoteAlignment.Start", RemoteAlignment.Start),
        option("CenterHorizontally", "RemoteAlignment.CenterHorizontally", RemoteAlignment.CenterHorizontally),
        option("End", "RemoteAlignment.End", RemoteAlignment.End),
        option("Absolute Left", "RemoteAbsoluteAlignment.Left", RemoteAbsoluteAlignment.Left),
        option("Absolute Right", "RemoteAbsoluteAlignment.Right", RemoteAbsoluteAlignment.Right),
    )
}

fun <T> choice(name: String, options: List<Option<T>>, default: Int = 0, doc: String = "") =
    ChoiceArg(name, options, default, doc)

internal val Blue = Color(0xFF1E88E5)
