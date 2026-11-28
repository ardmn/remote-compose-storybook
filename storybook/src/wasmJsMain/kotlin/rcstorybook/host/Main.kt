package rcstorybook.host

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.isSpecified
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
import ee.schimke.composeai.rcplayer.protocol.RcHostMetadataAction
import ee.schimke.composeai.rcplayer.protocol.RcHostNamedAction
import ee.schimke.composeai.rcplayer.protocol.RcNamedVariable
import ee.schimke.composeai.rcplayer.protocol.RcTextData
import ee.schimke.composeai.rcplayer.runtime.RcNamedValue
import ee.schimke.composeai.rcplayer.runtime.RcPlayerEvent
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import rcstorybook.catalog.ARGB_HEX_DIGITS
import rcstorybook.catalog.Args
import rcstorybook.catalog.Catalog
import rcstorybook.catalog.RGB_HEX_DIGITS
import rcstorybook.catalog.Story
import rcstorybook.catalog.hexOrNull

/** The stage size a page may ask for, in dp. */
private const val MIN_STAGE_DP = 16
private const val MAX_STAGE_DP = 4000

private const val OPAQUE = 0xFF000000L

/** The page's event for a document report (web/app.js listens for it). */
private const val DOCUMENT_EVENT = "rcstorybook:document"

/** The document report's args (as show() returns them) and its structural diff. */
private const val REPORT_ARGS = "args"
private const val REPORT_DIFF = "diff"

/** The document report's bytes, base64. */
private const val REPORT_BASE64 = "base64"

/**
 * What the stage shows: a story and the values of its args. [canonicalArgs] is the args as the page
 * gets them back (JSON); the document report carries it, so the page and the end-to-end test can
 * tell a document for these args from one for the story's previous args.
 */
private data class Selection(val story: Story, val args: Args, val canonicalArgs: String)

/** The stage as the page sizes it: the player's viewport in dp, and its theme. */
private data class StageConfig(
    val dark: Boolean = false,
    val widthDp: Int = 360,
    val heightDp: Int = 360,
    val centered: Boolean = true,
    /**
     * The stage's background, as the page paints the frame around it (web/app.js
     * `STAGE_BACKGROUNDS` is the one owner of these colours). Unspecified (no or a malformed colour
     * from the page) paints nothing: the player has no colour of its own to fall back to.
     */
    val background: Color = Color.Unspecified,
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
private val lastReports = mutableMapOf<String, Map<String, Any?>>()

/**
 * The selection the remote content last composed, set from inside it: the player has rendered it
 * when [RcEmbeddedPlayer]'s onRendered follows. Not snapshot state: nothing recomposes on it.
 */
private var composedSelection: Selection? = null

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
                        jsProperty(config, "width")
                            ?.toDoubleOrNull()
                            ?.toInt()
                            ?.coerceIn(MIN_STAGE_DP, MAX_STAGE_DP) ?: 360,
                    heightDp =
                        jsProperty(config, "height")
                            ?.toDoubleOrNull()
                            ?.toInt()
                            ?.coerceIn(MIN_STAGE_DP, MAX_STAGE_DP) ?: 360,
                    centered = jsProperty(config, "layout") != "topstart",
                    background = parseStageBackground(jsProperty(config, "background")),
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
            // Text drawn before this used the fallback face: tests wait for it.
            emit("rcstorybook:fonts", "")
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
    val canonical = toJson(story.args.associate { it.name to it.canonicalOf(values[it.name]) })
    val next = Selection(story, Args(values), canonical)
    if (next == selection) {
        // Nothing to recompose, so no new document will come: send the page the current one again
        // (it may have been away from this story, on a page with no stage).
        lastReports[id]?.let { emit(DOCUMENT_EVENT, toJson(it)) }
    } else {
        // A new document is coming; host values belonged to the previous one.
        hostValues.clear()
        hostDocument = null
        selection = next
    }
    return canonical
}

private fun Modifier.stageBackground(color: Color): Modifier =
    if (color.isSpecified) background(color) else this

/** `#RRGGBB` from the page, opaque; anything else is [Color.Unspecified]. */
private fun parseStageBackground(raw: String?): Color =
    raw?.removePrefix("#")
        ?.let { hexOrNull(it, RGB_HEX_DIGITS) }
        ?.let { Color((OPAQUE or it).toInt()) } ?: Color.Unspecified

private fun setHostValue(name: String, type: Int, raw: String) {
    val value: RcNamedValue? =
        when (type) {
            RcNamedVariable.STRING_TYPE -> RcNamedValue.Text(raw)
            RcNamedVariable.FLOAT_TYPE -> raw.toFloatOrNull()?.let { RcNamedValue.FloatValue(it) }
            RcNamedVariable.INT_TYPE -> raw.toIntOrNull()?.let { RcNamedValue.Integer(it) }
            RcNamedVariable.LONG_TYPE -> raw.toLongOrNull()?.let { RcNamedValue.LongValue(it) }
            RcNamedVariable.COLOR_TYPE ->
                hexOrNull(raw.removePrefix("#"), ARGB_HEX_DIGITS)?.let {
                    RcNamedValue.Color(it.toInt())
                }
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
                        .stageBackground(config.background),
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
                    .stageBackground(config.background),
            theme = if (config.dark) RcPlayerTheme.Dark else RcPlayerTheme.Light,
            typefaces = typefaces,
            encodePng = ::encodePng,
            onEvent = { event ->
                emit("rcstorybook:event", toJson(describe(current.story.id, event)))
            },
            onDocument = { document -> report(current, document) },
            onRendered = { composedSelection?.let(::reportSameDocument) },
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
            SideEffect { composedSelection = current }
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
private fun report(shown: Selection, document: RcDocument) {
    val story = shown.story
    val bytes = RcDocumentCodec.encode(document)
    val lines = document.structureLines()
    val previous = lastDocuments[story.id]
    val unchanged = previous == lines
    // A recomposition that changed nothing in the document for the same args (fonts arriving, say)
    // is not news: keep the last report, and with it the Changes tab's diff of the last real
    // change.
    // New args that make the same document are reported, with no diff: the page (and the
    // end-to-end test) waits for a document of the args it asked for.
    if (unchanged && lastReports[story.id]?.get(REPORT_ARGS) == shown.canonicalArgs) return
    val diff =
        if (unchanged) null
        else previous?.let { unifiedDiff(it, lines) }?.takeIf { it.isNotEmpty() }
    lastDocuments[story.id] = lines
    val report =
        mapOf(
            "story" to story.id,
            // The args this document was made from, as show() returned them.
            REPORT_ARGS to shown.canonicalArgs,
            "bytes" to bytes.size,
            "operations" to document.operations.size,
            "structure" to lines.joinToString("\n"),
            REPORT_DIFF to diff,
            REPORT_BASE64 to Base64.encode(bytes),
            // The document's constant texts: the page offers them to screen readers, which
            // cannot read a canvas.
            "texts" to screenReaderTexts(document),
            // The variables a host can set by name, with the wire type the page edits them as.
            "named" to
                document.operations.filterIsInstance<RcNamedVariable>().map {
                    mapOf("name" to it.name, "type" to it.type)
                },
        )
    lastReports[story.id] = report
    if (story == selection?.story) hostDocument = bytes
    emit(DOCUMENT_EVENT, toJson(report))
}

/**
 * The player rendered [shown] and made no new document (new args that draw the same thing): the
 * last report again, labelled with these args and with no diff, as the page waits for them.
 */
@OptIn(ExperimentalEncodingApi::class)
private fun reportSameDocument(shown: Selection) {
    val id = shown.story.id
    val last = lastReports[id] ?: return
    if (last[REPORT_ARGS] == shown.canonicalArgs) return
    val report = last + mapOf(REPORT_ARGS to shown.canonicalArgs, REPORT_DIFF to null)
    lastReports[id] = report
    // show() dropped the replay document for the new args; it is this same document, so host
    // values set from now on replay it, as they do after a new one.
    if (shown == selection)
        (last[REPORT_BASE64] as? String)?.let { hostDocument = Base64.decode(it) }
    emit(DOCUMENT_EVENT, toJson(report))
}

/**
 * The document's constant texts a reader would see; action names and metadata are wiring, not text.
 */
private fun screenReaderTexts(document: RcDocument): List<String> {
    val actionNames =
        document.operations.filterIsInstance<RcHostNamedAction>().map { it.nameTextId }.toSet() +
            document.operations.filterIsInstance<RcHostMetadataAction>().map { it.metadataTextId }
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
