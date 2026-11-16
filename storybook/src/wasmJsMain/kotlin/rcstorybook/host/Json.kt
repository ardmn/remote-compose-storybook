package rcstorybook.host

/**
 * Serializes maps, lists, strings, numbers, booleans and null as JSON: the catalog and the
 * document reports are the only JSON this app writes, so a tree serializer is all it needs.
 */
internal fun toJson(value: Any?): String = StringBuilder().also { it.appendJson(value) }.toString()

private fun StringBuilder.appendJson(value: Any?) {
    when (value) {
        null -> append("null")
        is String -> appendJsonString(value)
        is Boolean -> append(value.toString())
        is Float -> append(if (value.isFinite()) value.toString() else "null")
        is Double -> append(if (value.isFinite()) value.toString() else "null")
        is Number -> append(value.toString())
        is Map<*, *> -> {
            append('{')
            var first = true
            for ((k, v) in value) {
                if (!first) append(',')
                first = false
                appendJsonString(k.toString())
                append(':')
                appendJson(v)
            }
            append('}')
        }
        is Iterable<*> -> {
            append('[')
            var first = true
            for (v in value) {
                if (!first) append(',')
                first = false
                appendJson(v)
            }
            append(']')
        }
        else -> appendJsonString(value.toString())
    }
}

private fun StringBuilder.appendJsonString(s: String) {
    append('"')
    for (c in s) {
        when {
            c == '"' -> append("\\\"")
            c == '\\' -> append("\\\\")
            c == '\n' -> append("\\n")
            c == '\r' -> append("\\r")
            c == '\t' -> append("\\t")
            c < ' ' -> append("\\u").append(c.code.toString(16).padStart(4, '0'))
            else -> append(c)
        }
    }
    append('"')
}
