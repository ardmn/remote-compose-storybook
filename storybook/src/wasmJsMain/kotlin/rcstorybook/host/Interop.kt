package rcstorybook.host

/** The page side of the bridge: `window.rcStorybook`, and events back to the page. */
internal fun installApi(
    catalog: () -> JsString,
    show: (JsString, JsAny, Boolean) -> JsString,
    stage: (JsAny) -> Unit,
): Unit =
    js(
        """{
  window.rcStorybook = {
    catalog: () => catalog(),
    show: (id, args, fresh) => show(id, args, !!fresh),
    stage: (config) => stage(config),
  };
  window.dispatchEvent(new Event('rcstorybook:ready'));
}""",
    )

/** `obj[key]` as a string, or null when absent. */
internal fun jsProperty(obj: JsAny, key: String): String? =
    js("(obj[key] === undefined || obj[key] === null) ? null : String(obj[key])")

internal fun emit(type: String, detail: String): Unit =
    js("window.dispatchEvent(new CustomEvent(type, { detail: detail }))")
