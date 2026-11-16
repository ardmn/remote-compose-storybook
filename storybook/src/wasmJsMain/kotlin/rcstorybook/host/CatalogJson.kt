package rcstorybook.host

import rcstorybook.catalog.Arg
import rcstorybook.catalog.BoolArg
import rcstorybook.catalog.Catalog
import rcstorybook.catalog.ChoiceArg
import rcstorybook.catalog.ColorArg
import rcstorybook.catalog.FloatArg
import rcstorybook.catalog.IntArg
import rcstorybook.catalog.TextArg
import rcstorybook.catalog.argbHex

/** The catalog as the page reads it: components, their stories, and each arg's control. */
internal fun catalogJson(): String = toJson(
    mapOf(
        "components" to Catalog.components.map { component ->
            mapOf(
                "id" to component.id,
                "title" to component.title,
                "category" to component.category,
                "symbols" to component.symbols,
                "description" to component.description,
                "plain" to rcstorybook.catalog.plainLanguage[component.id]?.let {
                    mapOf("what" to it.what, "useWhen" to it.useWhen)
                },
                "stories" to component.stories.map { story ->
                    mapOf(
                        "id" to story.id,
                        "name" to story.name,
                        "function" to story.function,
                        "description" to story.description,
                        "width" to story.width,
                        "height" to story.height,
                        "args" to story.args.map(::argJson),
                    )
                },
            )
        }
    )
)

@Suppress("UNCHECKED_CAST")
private fun argJson(arg: Arg<*>): Map<String, Any?> {
    val base = mapOf("name" to arg.name, "type" to arg.type, "doc" to arg.doc)
    return base + when (arg) {
        is BoolArg -> mapOf("default" to arg.default.toString(), "codes" to mapOf("true" to "true", "false" to "false"))
        is IntArg -> mapOf("default" to arg.default.toString(), "min" to arg.min, "max" to arg.max, "step" to arg.step)
        is FloatArg -> mapOf("default" to arg.default.toString(), "min" to arg.min, "max" to arg.max, "step" to arg.step)
        is TextArg -> mapOf("default" to arg.default)
        is ColorArg -> mapOf("default" to "#" + argbHex(arg.default))
        is ChoiceArg<*> -> mapOf(
            "default" to arg.defaultLabel,
            "options" to arg.options.map { mapOf("label" to it.label, "code" to it.code) },
        )
    }
}
