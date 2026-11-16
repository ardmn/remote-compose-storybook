package rcstorybook.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable

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
 * [function] is the name of the story function; its source (rewritten to androidx imports) is
 * what the Code tab shows, and the call the Code tab prints is `function(arg = value, …)`.
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
    val content: @Composable @RemoteComposable (Args) -> Unit,
)

sealed class Arg<T>(val name: String, val default: T, val doc: String) {
    abstract val type: String

    /** Kotlin source for [value], as the story function's argument. */
    abstract fun code(value: T): String

    /** Parses the string the page sends back. */
    abstract fun parse(raw: String): T
}

class BoolArg(name: String, default: Boolean, doc: String = "") : Arg<Boolean>(name, default, doc) {
    override val type = "boolean"
    override fun code(value: Boolean) = value.toString()
    override fun parse(raw: String) = raw == "true"
}

class IntArg(name: String, default: Int, val min: Int, val max: Int, val step: Int = 1, doc: String = "") :
    Arg<Int>(name, default, doc) {
    override val type = "int"
    override fun code(value: Int) = value.toString()
    override fun parse(raw: String) = raw.toDoubleOrNull()?.toInt()?.coerceIn(min, max) ?: default
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
    override fun code(value: Float) = formatFloat(value) + "f"
    override fun parse(raw: String) = raw.toFloatOrNull()?.coerceIn(min, max) ?: default
}

class TextArg(name: String, default: String, doc: String = "") : Arg<String>(name, default, doc) {
    override val type = "text"
    override fun code(value: String) =
        "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$").replace("\n", "\\n") + "\""
    override fun parse(raw: String) = raw
}

class ColorArg(name: String, default: Color, doc: String = "") : Arg<Color>(name, default, doc) {
    override val type = "color"
    override fun code(value: Color) = "Color(0x" + argbHex(value) + ")"
    override fun parse(raw: String): Color {
        val hex = raw.removePrefix("#")
        val argb = when (hex.length) {
            6 -> ("FF" + hex).toLongOrNull(16)
            8 -> hex.toLongOrNull(16)
            else -> null
        } ?: return default
        return Color(argb.toInt())
    }
}

class Option<T>(val label: String, val code: String, val value: T)

class ChoiceArg<T>(name: String, val options: List<Option<T>>, defaultIndex: Int = 0, doc: String = "") :
    Arg<T>(name, options[defaultIndex].value, doc) {
    override val type = "choice"
    val defaultLabel = options[defaultIndex].label
    override fun code(value: T) = options.first { it.value == value }.code
    override fun parse(raw: String) = options.firstOrNull { it.label == raw }?.value ?: default
}

/** The current values of a story's args, by name. */
class Args(private val values: Map<String, Any?>) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(name: String): T = values.getValue(name) as T

    fun int(name: String): Int = get(name)
    fun float(name: String): Float = get(name)
    fun bool(name: String): Boolean = get(name)
    fun text(name: String): String = get(name)
    fun color(name: String): Color = get(name)

    override fun equals(other: Any?) = other is Args && other.values == values
    override fun hashCode() = values.hashCode()

    companion object {
        fun defaults(story: Story) = Args(story.args.associate { it.name to it.default })
    }

    fun with(updates: Map<String, Any?>) = Args(values + updates)
}

internal fun argbHex(color: Color): String {
    val argb = (color.value shr 32).toLong() and 0xFFFFFFFFL
    // Color.value packs sRGB colors as ARGB in the top 32 bits.
    return argb.toString(16).uppercase().padStart(8, '0')
}

internal fun formatFloat(value: Float): String {
    val rounded = kotlin.math.round(value * 1000f) / 1000f
    val text = rounded.toString()
    return if (text.contains('.') || text.contains('E')) text.trimEnd('0').trimEnd('.').let {
        if (it.contains('.')) it else "$it.0"
    } else "$text.0"
}

// Builders used by the catalog.
fun bool(name: String, default: Boolean, doc: String = "") = BoolArg(name, default, doc)
fun int(name: String, default: Int, min: Int, max: Int, step: Int = 1, doc: String = "") =
    IntArg(name, default, min, max, step, doc)
fun float(name: String, default: Float, min: Float, max: Float, step: Float = 0.1f, doc: String = "") =
    FloatArg(name, default, min, max, step, doc)
fun text(name: String, default: String, doc: String = "") = TextArg(name, default, doc)
fun color(name: String, default: Color, doc: String = "") = ColorArg(name, default, doc)
fun <T> option(label: String, code: String, value: T) = Option(label, code, value)
