package rcstorybook.catalog

import androidx.compose.ui.graphics.Color
import rcstorybook.stories.*

private const val L = "androidx.compose.remote.creation.compose.layout"
private const val F = "androidx.compose.remote.foundation"

internal val layoutComponents =
    listOf(
        Component(
            id = "remote-box",
            title = "RemoteBox",
            category = "Layout",
            symbols = listOf("$L.RemoteBox", "$L.RemoteAlignment", "$L.RemoteAbsoluteAlignment"),
            description =
                "Stacks its children on top of each other and positions them with contentAlignment. " +
                    "The overload without content draws only its modifiers.",
            stories =
                listOf(
                    Story(
                        id = "remote-box--content-alignment",
                        name = "Content alignment",
                        function = "BoxContentAlignment",
                        description =
                            "Every RemoteAlignment and RemoteAbsoluteAlignment value applied to one child.",
                        args =
                            listOf(
                                choice("contentAlignment", Options.alignments, default = 4),
                                int("boxSize", 220, 80, 320, step = 10),
                                int("childSize", 72, 16, 200, step = 4),
                            ),
                    ) {
                        BoxContentAlignment(
                            it["contentAlignment"],
                            it.int("boxSize"),
                            it.int("childSize"),
                        )
                    },
                    Story(
                        id = "remote-box--layering",
                        name = "Layering",
                        function = "BoxLayering",
                        description = "Later children draw on top of earlier ones.",
                        args = listOf(int("offsetStep", 32, 0, 60, step = 2)),
                    ) {
                        BoxLayering(it.int("offsetStep"))
                    },
                    Story(
                        id = "remote-box--empty",
                        name = "Without content",
                        function = "EmptyBox",
                        description =
                            "RemoteBox(modifier) with no content lambda: a sized, painted rectangle.",
                        args =
                            listOf(
                                int("width", 160, 8, 320, step = 8),
                                int("height", 96, 8, 320, step = 8),
                                color("color", Color(0xFFFF7043)),
                            ),
                    ) {
                        EmptyBox(it.int("width"), it.int("height"), it.color("color"))
                    },
                ),
        ),
        Component(
            id = "remote-row",
            title = "RemoteRow",
            category = "Layout",
            symbols = listOf("$L.RemoteRow", "$L.RemoteRowScope", "$L.RemoteArrangement"),
            description =
                "Places children horizontally. horizontalArrangement distributes free space, verticalAlignment " +
                    "positions children across the row, and RemoteRowScope.weight shares the remaining width.",
            stories =
                listOf(
                    Story(
                        id = "remote-row--arrangement",
                        name = "Arrangement & alignment",
                        function = "RowArrangement",
                        description =
                            "Change how the row spends its free space and aligns children of different heights.",
                        args =
                            listOf(
                                choice(
                                    "horizontalArrangement",
                                    Options.horizontalArrangements,
                                    default = 3,
                                ),
                                choice(
                                    "verticalAlignment",
                                    Options.verticalAlignments,
                                    default = 1,
                                ),
                                int("itemCount", 3, 1, 5),
                            ),
                    ) {
                        RowArrangement(
                            it["horizontalArrangement"],
                            it["verticalAlignment"],
                            it.int("itemCount"),
                        )
                    },
                    Story(
                        id = "remote-row--weights",
                        name = "Weights",
                        function = "RowWeights",
                        description = "weight() splits the row's width in proportion.",
                        args =
                            listOf(
                                float("firstWeight", 1f, 0.1f, 5f),
                                float("secondWeight", 2f, 0.1f, 5f),
                                float("thirdWeight", 1f, 0.1f, 5f),
                            ),
                    ) {
                        RowWeights(
                            it.float("firstWeight"),
                            it.float("secondWeight"),
                            it.float("thirdWeight"),
                        )
                    },
                ),
        ),
        Component(
            id = "remote-column",
            title = "RemoteColumn",
            category = "Layout",
            symbols = listOf("$L.RemoteColumn", "$L.RemoteColumnScope", "$L.RemoteArrangement"),
            description =
                "Places children vertically, with verticalArrangement, horizontalAlignment and RemoteColumnScope.weight.",
            stories =
                listOf(
                    Story(
                        id = "remote-column--arrangement",
                        name = "Arrangement & alignment",
                        function = "ColumnArrangement",
                        description =
                            "Distribute free height and align children of different widths.",
                        args =
                            listOf(
                                choice(
                                    "verticalArrangement",
                                    Options.verticalArrangements,
                                    default = 5,
                                ),
                                choice(
                                    "horizontalAlignment",
                                    Options.horizontalAlignments,
                                    default = 1,
                                ),
                                int("itemCount", 3, 1, 5),
                            ),
                    ) {
                        ColumnArrangement(
                            it["verticalArrangement"],
                            it["horizontalAlignment"],
                            it.int("itemCount"),
                        )
                    },
                    Story(
                        id = "remote-column--weight",
                        name = "Weight fills the rest",
                        function = "ColumnWeight",
                        description =
                            "A weighted child takes the height its fixed-size siblings leave.",
                        args =
                            listOf(
                                int("headerHeight", 56, 0, 140, step = 4),
                                int("footerHeight", 40, 0, 140, step = 4),
                            ),
                    ) {
                        ColumnWeight(it.int("headerHeight"), it.int("footerHeight"))
                    },
                ),
        ),
        Component(
            id = "remote-arrangement",
            title = "RemoteArrangement.spacedBy",
            category = "Layout",
            symbols = listOf("$L.RemoteArrangement"),
            description = "A fixed gap between children of a row or a column.",
            stories =
                listOf(
                    Story(
                        id = "remote-arrangement--spaced-by",
                        name = "spacedBy",
                        function = "SpacedBy",
                        description = "The same gap for a row and for a column.",
                        args = listOf(int("space", 12, 0, 48, step = 2), bool("vertical", false)),
                    ) {
                        SpacedBy(it.int("space"), it.bool("vertical"))
                    },
                ),
        ),
        Component(
            id = "remote-spacer",
            title = "RemoteSpacer",
            category = "Layout",
            symbols = listOf("$F.layout.RemoteSpacer"),
            description = "Empty space with the size its modifier gives it (foundation).",
            stories =
                listOf(
                    Story(
                        id = "remote-spacer--between",
                        name = "Fixed or weighted",
                        function = "SpacerBetween",
                        description =
                            "A fixed-width spacer, or a weighted one that pushes the next child to the end.",
                        args = listOf(int("gap", 24, 0, 160, step = 4), bool("pushApart", false)),
                    ) {
                        SpacerBetween(it.int("gap"), it.bool("pushApart"))
                    },
                ),
        ),
        Component(
            id = "remote-collapsible",
            title = "RemoteCollapsibleColumn / Row",
            category = "Layout",
            symbols =
                listOf(
                    "$L.RemoteCollapsibleColumn",
                    "$L.RemoteCollapsibleColumnScope",
                    "$L.RemoteCollapsibleRow",
                    "$L.RemoteCollapsibleRowScope",
                ),
            description =
                "Like a column or a row, but children that do not fit are hidden, the lowest collapsiblePriority first. " +
                    "Shrink the container to watch them go.",
            stories =
                listOf(
                    Story(
                        id = "remote-collapsible--column",
                        name = "Column priorities",
                        function = "CollapsibleColumnPriorities",
                        description = "Three 80dp children in a column whose height you control.",
                        args = listOf(int("height", 250, 40, 320, step = 10)),
                    ) {
                        CollapsibleColumnPriorities(it.int("height"))
                    },
                    Story(
                        id = "remote-collapsible--row",
                        name = "Row priorities",
                        function = "CollapsibleRowPriorities",
                        description =
                            "Four children in a row whose width you control; the last one is weighted.",
                        args = listOf(int("width", 340, 80, 360, step = 10)),
                    ) {
                        CollapsibleRowPriorities(it.int("width"))
                    },
                ),
        ),
        Component(
            id = "remote-fit-box",
            title = "RemoteFitBox",
            category = "Layout",
            symbols = listOf("$L.RemoteFitBox"),
            description =
                "Shows the first child that fits the available space: responsive layouts decided by the player, not the server.",
            stories =
                listOf(
                    Story(
                        id = "remote-fit-box--responsive",
                        name = "Responsive",
                        function = "FitBoxResponsive",
                        description =
                            "Resize the container: the player picks the large, medium or small variant.",
                        args = listOf(int("containerSize", 240, 60, 320, step = 10)),
                    ) {
                        FitBoxResponsive(it.int("containerSize"))
                    },
                ),
        ),
    )
