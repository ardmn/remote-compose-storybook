package rcstorybook.host

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import ee.schimke.composeai.rccreation.compose.capture.RemoteCreationDisplayInfo
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxSize as remoteFillMaxSize
import ee.schimke.composeai.rcplayer.compose.RcPlayerTheme
import ee.schimke.composeai.rcplayer.embedded.RcEmbeddedPlayer
import ee.schimke.composeai.rcplayer.protocol.RcDocument
import ee.schimke.composeai.rcplayer.protocol.RcDocumentCodec
import ee.schimke.composeai.rcplayer.runtime.RcPlayerEvent
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import rcstorybook.catalog.Args
import rcstorybook.catalog.Catalog
import rcstorybook.catalog.Story

/** What the stage shows: a story, the values of its args, and a counter the page bumps. */
private data class Selection(val story: Story, val args: Args)

/** The stage as the page sizes it: the player's viewport in dp, and its theme. */
private data class StageConfig(
    val dark: Boolean = false,
    val widthDp: Int = 360,
    val heightDp: Int = 360,
    val centered: Boolean = true,
)

private var selection by mutableStateOf<Selection?>(null)
private var stageConfig by mutableStateOf(StageConfig())

/** The last document each story produced, for the Changes tab's structural diff. */
private val lastDocuments = mutableMapOf<String, List<String>>()

private val storiesById: Map<String, Story> by lazy {
    Catalog.components.flatMap { it.stories }.associateBy { it.id }
}

fun main() {
    installApi(
        catalog = { catalogJson().toJsString() },
        show = { id, args -> show(id.toString(), args) },
        stage = { config ->
            stageConfig =
                StageConfig(
                    dark = jsProperty(config, "theme") == "dark",
                    widthDp =
                        jsProperty(config, "width")?.toDoubleOrNull()?.toInt()?.coerceIn(16, 4000)
                            ?: 360,
                    heightDp =
                        jsProperty(config, "height")?.toDoubleOrNull()?.toInt()?.coerceIn(16, 4000)
                            ?: 360,
                    centered = jsProperty(config, "layout") != "topstart",
                )
        },
    )
    ComposeViewport(viewportContainerId = "stage") { Stage() }
}

private fun show(id: String, rawArgs: JsAny) {
    val story =
        storiesById[id]
            ?: run {
                emit(
                    "rcstorybook:failure",
                    toJson(mapOf("story" to id, "message" to "Unknown story $id")),
                )
                return
            }
    val values =
        story.args.associate { arg ->
            arg.name to (jsProperty(rawArgs, arg.name)?.let { arg.parse(it) } ?: arg.default)
        }
    selection = Selection(story, Args(values))
}

@Composable
private fun Stage() {
    val current = selection ?: return
    val config = stageConfig
    // The canvas is larger than any viewport the page offers and never resizes (a Compose viewport
    // does not follow its container); the player gets exactly the viewport the page asked for, at
    // the top left, and the page's frame clips the rest.
    Box(Modifier.fillMaxSize()) {
        val density = LocalDensity.current.density
        val widthPx = (config.widthDp * density).toInt().coerceAtLeast(1)
        val heightPx = (config.heightDp * density).toInt().coerceAtLeast(1)
        val display =
            RemoteCreationDisplayInfo(widthPx, heightPx, (density * 160f).toInt().coerceAtLeast(1))
        RcEmbeddedPlayer(
            display = display,
            modifier =
                Modifier.size(config.widthDp.dp, config.heightDp.dp)
                    .background(if (config.dark) Color(0xFF121316) else Color.White),
            theme = if (config.dark) RcPlayerTheme.Dark else RcPlayerTheme.Light,
            encodePng = ::encodePng,
            onEvent = { event ->
                emit("rcstorybook:event", toJson(describe(current.story.id, event)))
            },
            onDocument = { document -> report(current.story, document) },
            onFailure = { error ->
                emit(
                    "rcstorybook:failure",
                    toJson(
                        mapOf(
                            "story" to current.story.id,
                            "message" to (error.message ?: error.toString()),
                        ),
                    ),
                )
            },
            key = current,
        ) {
            if (config.centered) {
                // Storybook's "centered" layout: a full-size box around the story. It is part of
                // the
                // document the Document tab shows; choose "Top start" to see the story alone.
                RemoteBox(
                    RemoteModifier.remoteFillMaxSize(),
                    contentAlignment = RemoteAlignment.Center,
                ) {
                    current.story.content(current.args)
                }
            } else {
                current.story.content(current.args)
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
private fun report(story: Story, document: RcDocument) {
    val bytes = RcDocumentCodec.encode(document)
    val lines = document.structureLines()
    val previous = lastDocuments[story.id]
    val diff = previous?.let { unifiedDiff(it, lines) }?.takeIf { it.isNotEmpty() }
    lastDocuments[story.id] = lines
    emit(
        "rcstorybook:document",
        toJson(
            mapOf(
                "story" to story.id,
                "bytes" to bytes.size,
                "operations" to document.operations.size,
                "structure" to lines.joinToString("\n"),
                "diff" to diff,
                "base64" to Base64.encode(bytes),
            ),
        ),
    )
}

private fun describe(storyId: String, event: RcPlayerEvent): Map<String, Any?> =
    mapOf("story" to storyId, "type" to event::class.simpleName, "detail" to event.toString())

private fun encodePng(bitmap: ImageBitmap): ByteArray? =
    Image.makeFromBitmap(bitmap.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
