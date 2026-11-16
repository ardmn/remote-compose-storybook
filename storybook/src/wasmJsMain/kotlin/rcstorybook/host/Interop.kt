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

/** `fetch(url)` as base64, so the bytes cross into Wasm as one string. */
private fun fetchAsBase64(url: String): kotlin.js.Promise<JsString> =
    js(
        """fetch(url).then(function (response) {
  if (!response.ok) throw new Error('HTTP ' + response.status + ' ' + url);
  return response.arrayBuffer();
}).then(function (buffer) {
  var bytes = new Uint8Array(buffer), chunks = [], size = 0x8000;
  for (var i = 0; i < bytes.length; i += size) chunks.push(String.fromCharCode.apply(null, bytes.subarray(i, i + size)));
  return btoa(chunks.join(''));
})""",
    )

@OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
internal suspend fun fetchBytes(url: String): ByteArray =
    kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        fetchAsBase64(url)
            .then { encoded ->
                if (continuation.isActive) {
                    continuation.resumeWith(
                        Result.success(kotlin.io.encoding.Base64.decode(encoded.toString())),
                    )
                }
                null
            }
            .catch { failure ->
                if (continuation.isActive) {
                    continuation.resumeWith(
                        Result.failure(IllegalStateException(failure.toString())),
                    )
                }
                null
            }
    }
