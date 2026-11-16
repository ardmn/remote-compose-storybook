package rcstorybook.host

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import ee.schimke.composeai.rcplayer.compose.RcComposePlayer
import ee.schimke.composeai.rcplayer.compose.RcManifestTypefaceLoader
import ee.schimke.composeai.rcplayer.compose.RcPlayerTheme
import ee.schimke.composeai.rcplayer.compose.RcTypefaceLoader
import ee.schimke.composeai.rcplayer.embedded.RcEmbeddedPlayer
import ee.schimke.composeai.rcplayer.protocol.RcDocument
import ee.schimke.composeai.rcplayer.protocol.RcDocumentCodec
import ee.schimke.composeai.rcplayer.protocol.RcHostNamedAction
import ee.schimke.composeai.rcplayer.protocol.RcNamedVariable
import ee.schimke.composeai.rcplayer.protocol.RcTextData
import ee.schimke.composeai.rcplayer.runtime.RcNamedValue
import ee.schimke.composeai.rcplayer.runtime.RcPlayerEvent
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import rcstorybook.catalog.Args
import rcstorybook.catalog.Catalog
import rcstorybook.catalog.Story

/** What the stage shows: a story and the values of its args. */
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

/**
 * The faces the player draws text with, from `fonts/fonts.json` beside the page. The browser has no
 * system fonts: until these load (and if they fail to) every family falls back to one bundled face.
 */
private var typefaces by mutableStateOf<RcTypefaceLoader>(RcTypefaceLoader.Default)

/** The last document each story produced, for the Changes tab's structural diff. */
private val lastDocuments = mutableMapOf<String, List<String>>()

/** The last document report each story sent, to send again when nothing changed. */
private val lastReports = mutableMapOf<String, String>()

/**
 * Values the page sets "as the host app" (the Host values tab), by the document's own variable
 * names. While any is set the stage replays [hostDocument] — the same bytes, no new capture — with
 * these values, which is exactly what an app does with a document a server sent once.
 */
private val hostValues = mutableStateMapOf<String, RcNamedValue>()

/**
 * The current story's last document, for replaying it with [hostValues]. Deliberately not snapshot
 * state: the stage reads it only when a host value is set, so writing it never recomposes the stage
 * (which would capture again, report again, and loop).
 */
private var hostDocument: ByteArray? = null

private val storiesById: Map<String, Story> by lazy {
    Catalog.components.flatMap { it.stories }.associateBy { it.id }
}

fun main() {
    installApi(
        catalog = { catalogJson().toJsString() },
        show = { id, args, fresh -> show(id.toString(), args, fresh).toJsString() },
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
        hostValue = { name, type, value -> setHostValue(name.toString(), type, value.toString()) },
        hostReset = { hostValues.clear() },
    )
    ComposeViewport(viewportContainerId = "stage") {
        LaunchedEffect(Unit) {
            typefaces =
                runCatching { RcManifestTypefaceLoader(::fetchBytes).load("fonts/") }
                    .getOrElse { error ->
                        println("fonts: ${error.message}")
                        RcTypefaceLoader.Default
                    }
        }
        Stage()
    }
}

/**
 * Shows [id] with the args the page sent, and returns their canonical values (as JSON) for the
 * page's controls, URL and Code tab. [fresh] means the page has just switched to this story: the
 * Changes tab then starts from the next document rather than from an earlier visit.
 */
private fun show(id: String, rawArgs: JsAny, fresh: Boolean): String {
    val story =
        storiesById[id]
            ?: run {
                emit(
                    "rcstorybook:failure",
                    toJson(mapOf("story" to id, "message" to "Unknown story $id")),
                )
                return "{}"
            }
    val values =
        story.args.associate { arg ->
            arg.name to (jsProperty(rawArgs, arg.name)?.let { arg.parse(it) } ?: arg.default)
        }
    if (fresh) lastDocuments.remove(id)
    val next = Selection(story, Args(values))
    if (next == selection) {
        // Nothing to recompose, so no new document will come: send the page the current one again
        // (it may have been away from this story, on a page with no stage).
        lastReports[id]?.let { emit("rcstorybook:document", it) }
    } else {
        // A new document is coming; host values belonged to the previous one.
        hostValues.clear()
        hostDocument = null
        selection = next
    }
    return toJson(story.args.associate { it.name to it.canonicalOf(values[it.name]) })
}

private fun setHostValue(name: String, type: Int, raw: String) {
    val value: RcNamedValue? =
        when (type) {
            RcNamedVariable.STRING_TYPE -> RcNamedValue.Text(raw)
            RcNamedVariable.FLOAT_TYPE -> raw.toFloatOrNull()?.let { RcNamedValue.FloatValue(it) }
            RcNamedVariable.INT_TYPE -> raw.toIntOrNull()?.let { RcNamedValue.Integer(it) }
            RcNamedVariable.LONG_TYPE -> raw.toLongOrNull()?.let { RcNamedValue.LongValue(it) }
            RcNamedVariable.COLOR_TYPE ->
                raw.removePrefix("#")
                    .takeIf { it.length == 8 }
                    ?.toLongOrNull(16)
                    ?.let { RcNamedValue.Color(it.toInt()) }
            else -> null
        }
    if (value == null) hostValues.remove(name) else hostValues[name] = value
}

@Composable
private fun Stage() {
    val current = selection ?: return
    val config = stageConfig
    val replay = if (hostValues.isNotEmpty()) hostDocument else null
    // The canvas is larger than any viewport the page offers and never resizes (a Compose viewport
    // does not follow its container); the player gets exactly the viewport the page asked for, at
    // the top left, and the page's frame clips the rest.
    Box(Modifier.fillMaxSize()) {
        val density = LocalDensity.current.density
        val widthPx = (config.widthDp * density).toInt().coerceAtLeast(1)
        val heightPx = (config.heightDp * density).toInt().coerceAtLeast(1)
        val display =
            RemoteCreationDisplayInfo(widthPx, heightPx, (density * 160f).toInt().coerceAtLeast(1))
        if (replay != null) {
            RcComposePlayer(
                replay,
                modifier =
                    Modifier.size(config.widthDp.dp, config.heightDp.dp)
                        .background(if (config.dark) Color(0xFF121316) else Color.White),
                theme = if (config.dark) RcPlayerTheme.Dark else RcPlayerTheme.Light,
                namedValues = hostValues,
                onEvent = { event ->
                    emit("rcstorybook:event", toJson(describe(current.story.id, event)))
                },
                typefaces = typefaces,
            )
            return@Box
        }
        RcEmbeddedPlayer(
            display = display,
            modifier =
                Modifier.size(config.widthDp.dp, config.heightDp.dp)
                    .background(if (config.dark) Color(0xFF121316) else Color.White),
            theme = if (config.dark) RcPlayerTheme.Dark else RcPlayerTheme.Light,
            typefaces = typefaces,
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
    // A recomposition that changed nothing in the document (fonts arriving, say) is not news: keep
    // the last report, and with it the Changes tab's diff of the last real change.
    if (previous == lines) return
    val diff = previous?.let { unifiedDiff(it, lines) }?.takeIf { it.isNotEmpty() }
    lastDocuments[story.id] = lines
    val report =
        toJson(
            mapOf(
                "story" to story.id,
                "bytes" to bytes.size,
                "operations" to document.operations.size,
                "structure" to lines.joinToString("\n"),
                "diff" to diff,
                "base64" to Base64.encode(bytes),
                // The document's constant texts: the page offers them to screen readers, which
                // cannot read a canvas.
                "texts" to screenReaderTexts(document),
                // The variables a host can set by name, with the wire type the page edits them as.
                "named" to
                    document.operations.filterIsInstance<RcNamedVariable>().map {
                        mapOf("name" to it.name, "type" to it.type)
                    },
            ),
        )
    lastReports[story.id] = report
    if (story == selection?.story) hostDocument = bytes
    emit("rcstorybook:document", report)
}

/** The document's constant texts a reader would see; action names are wiring, not text. */
private fun screenReaderTexts(document: RcDocument): List<String> {
    val actionNames =
        document.operations.filterIsInstance<RcHostNamedAction>().map { it.nameTextId }.toSet()
    return document.operations
        .filterIsInstance<RcTextData>()
        .filter { it.id !in actionNames && it.text.isNotBlank() }
        .map { it.text }
        .distinct()
}

private fun describe(storyId: String, event: RcPlayerEvent): Map<String, Any?> =
    mapOf("story" to storyId, "type" to event::class.simpleName, "detail" to event.toString())

private fun encodePng(bitmap: ImageBitmap): ByteArray? =
    Image.makeFromBitmap(bitmap.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
