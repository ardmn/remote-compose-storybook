package rcstorybook.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable

private const val HEX_RADIX = 16

/** `AARRGGBB`. */
private const val ARGB_HEX_DIGITS = 8

/** One building block of the public `@RemoteComposable` API, with its stories. */
class Component(
    val id: String,
    val title: String,
    val category: String,
    /** Fully-qualified androidx symbols this component documents (used for API signatures). */
    val symbols: List<String>,
    val description: String,
    val stories: List<Story>,
)

/**
 * One story: a `@RemoteComposable` function from `rcstorybook.stories` shown with live [args].
 *
 * [function] is the name of the story function; its source (rewritten to androidx imports) is what
 * the Code tab shows, and the call the Code tab prints is `function(arg = value, …)`.
 */
class Story(
    val id: String,
    val name: String,
    val function: String,
    val description: String,
    val args: List<Arg<*>>,
    /** Hints for the stage: the preferred viewport, in dp. */
    val width: Int = 360,
    val height: Int = 360,
    /** The stage is a round watch screen ([width] is its diameter): Wear OS stories. */
    val round: Boolean = false,
    val content: @Composable @RemoteComposable (Args) -> Unit,
)

sealed class Arg<T>(val name: String, val default: T, val doc: String) {
    abstract val type: String

    /**
     * Parses the string the page sends (from a control or a shared URL). Anything out of range or
     * malformed becomes the nearest valid value or the default, never an error.
     */
    abstract fun parse(raw: String): T

    /**
     * The canonical string for [value]: what the page shows in its control and URL and turns into
     * the Code tab's Kotlin, so the code always matches what the player is drawing.
     */
    abstract fun canonical(value: T): String

    @Suppress("UNCHECKED_CAST") fun canonicalOf(value: Any?): String = canonical(value as T)
}

class BoolArg(name: String, default: Boolean, doc: String = "") : Arg<Boolean>(name, default, doc) {
    override val type = "boolean"

    override fun parse(raw: String) = raw == "true"

    override fun canonical(value: Boolean) = value.toString()
}

class IntArg(
    name: String,
    default: Int,
    val min: Int,
    val max: Int,
    val step: Int = 1,
    doc: String = "",
) : Arg<Int>(name, default, doc) {
    override val type = "int"

    override fun parse(raw: String) =
        raw.toDoubleOrNull()
            ?.takeIf { it.isFinite() }
            ?.let { kotlin.math.round(it).toInt().coerceIn(min, max) } ?: default

    override fun canonical(value: Int) = value.toString()
}

class FloatArg(
    name: String,
    default: Float,
    val min: Float,
    val max: Float,
    val step: Float = 0.1f,
    doc: String = "",
) : Arg<Float>(name, default, doc) {
    override val type = "float"

    override fun parse(raw: String) =
        raw.toFloatOrNull()?.takeIf { it.isFinite() }?.coerceIn(min, max) ?: default

    override fun canonical(value: Float) = value.toString()
}

class TextArg(name: String, default: String, doc: String = "") : Arg<String>(name, default, doc) {
    override val type = "text"

    override fun parse(raw: String) = raw

    override fun canonical(value: String) = value
}

class ColorArg(name: String, default: Color, doc: String = "") : Arg<Color>(name, default, doc) {
    override val type = "color"

    override fun canonical(value: Color) = "#" + argbHex(value)

    override fun parse(raw: String): Color {
        val hex = raw.removePrefix("#")
        val argb =
            when (hex.length) {
                6 -> hex.takeIf { it.all(Char::isHexDigit) }?.let { ("FF$it").toLong(HEX_RADIX) }
                ARGB_HEX_DIGITS -> hex.takeIf { it.all(Char::isHexDigit) }?.toLong(HEX_RADIX)
                else -> null
            } ?: return default
        return Color(argb.toInt())
    }
}

class Option<T>(val label: String, val code: String, val value: T)

class ChoiceArg<T>(
    name: String,
    val options: List<Option<T>>,
    defaultIndex: Int = 0,
    doc: String = "",
) : Arg<T>(name, options[defaultIndex].value, doc) {
    override val type = "choice"

    override fun parse(raw: String) = options.firstOrNull { it.label == raw }?.value ?: default

    override fun canonical(value: T) = options.first { it.value == value }.label
}

/** The current values of a story's args, by name. */
class Args(private val values: Map<String, Any?>) {
    @Suppress("UNCHECKED_CAST") operator fun <T> get(name: String): T = values.getValue(name) as T

    fun int(name: String): Int = get(name)

    fun float(name: String): Float = get(name)

    fun bool(name: String): Boolean = get(name)

    fun text(name: String): String = get(name)

    fun color(name: String): Color = get(name)

    override fun equals(other: Any?) = other is Args && other.values == values

    override fun hashCode() = values.hashCode()
}

private fun Char.isHexDigit() = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

internal fun argbHex(color: Color): String {
    val argb = (color.value shr 32).toLong() and 0xFFFFFFFFL
    // Color.value packs sRGB colors as ARGB in the top 32 bits.
    return argb.toString(HEX_RADIX).uppercase().padStart(ARGB_HEX_DIGITS, '0')
}

// Builders used by the catalog.
fun bool(name: String, default: Boolean, doc: String = "") = BoolArg(name, default, doc)

fun int(name: String, default: Int, min: Int, max: Int, step: Int = 1, doc: String = "") =
    IntArg(name, default, min, max, step, doc)

fun float(
    name: String,
    default: Float,
    min: Float,
    max: Float,
    step: Float = 0.1f,
    doc: String = "",
) = FloatArg(name, default, min, max, step, doc)

fun text(name: String, default: String, doc: String = "") = TextArg(name, default, doc)

fun color(name: String, default: Color, doc: String = "") = ColorArg(name, default, doc)

fun <T> option(label: String, code: String, value: T) = Option(label, code, value)
