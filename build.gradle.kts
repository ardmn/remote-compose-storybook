plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ktfmt) apply false
    alias(libs.plugins.detekt) apply false
}

// ktor-client-core, a transitive Kotlin dependency, declares ws as an npm dependency for its Node
// WebSocket client; the browser bundle never contains it, but kotlin-js-store/wasm/yarn.lock does.
// ws 8.20.1 has GHSA-96hv-2xvq-fx4p (fixed in 8.21.0): pin the fixed version so OSV-Scanner needs no
// ignore entry. Remove this once ktor depends on 8.21.0 or later.
plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnRootExtension>().resolution("ws", "8.21.0")
}
