plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.detekt)
}

// AndroidX's own style: the stories are shown to users as AndroidX code.
ktfmt { kotlinLangStyle() }

// Static analysis without type resolution: detekt resolves types only for JVM and Android
// targets, and the storybook is Kotlin/Wasm only. CI runs :storybook:detektWasmJsMainSourceSet;
// baselined findings are in detekt-baseline-wasmJsMainSourceSet.xml (pre-existing ones, plus
// TooManyFunctions on the story files, which hold one function per story by design).
detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

// The plain `detekt` task (and so `check`) only sees the JVM-style src/main/kotlin, which this
// project does not have, and ends NO-SOURCE: make it run the Wasm source set's analysis.
tasks.named("detekt") { dependsOn("detektWasmJsMainSourceSet") }

// Lock what ships in storybook.wasm in gradle.lockfile, so OSV-Scanner (security.yml) scans the
// Kotlin and Compose libraries the site runs. An upgrade fails until the lock is rewritten; CI's
// lint
// job checks that the lock matches the declared dependencies (a downgrade or removal would
// otherwise
// be held silently). Build tools (ktfmt, detekt) are not locked: they never reach the site.
// After changing a dependency: ./gradlew :storybook:dependencies --write-locks
val shippedConfigurations =
    setOf("wasmJsCompileClasspath", "wasmJsRuntimeClasspath", "wasmJsNpmAggregated")

configurations
    .matching { it.name in shippedConfigurations }
    .configureEach { resolutionStrategy.activateDependencyLocking() }

kotlin {
    compilerOptions { allWarningsAsErrors.set(true) }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("storybook")
        browser()
        binaries.executable()
    }
    sourceSets {
        all {
            languageSettings.optIn(
                "ee.schimke.composeai.rccreation.bridge.ExperimentalRcCreationApi",
            )
            languageSettings.optIn("ee.schimke.composeai.rcplayer.compose.ExperimentalRcPlayerApi")
            languageSettings.optIn("kotlin.js.ExperimentalWasmJsInterop")
            languageSettings.optIn("androidx.compose.ui.ExperimentalComposeUiApi")
        }
        wasmJsMain.dependencies {
            implementation(project.dependencies.platform(libs.rc.players.bom))
            implementation(libs.rc.creation.compose)
            implementation(libs.rc.creation.foundation)
            implementation(libs.rc.player.compose)
            implementation(libs.rc.player.embedded)
            implementation(libs.kotlinx.coroutines.core)
            @Suppress("DEPRECATION") implementation(compose.runtime)
            @Suppress("DEPRECATION") implementation(compose.foundation)
            @Suppress("DEPRECATION") implementation(compose.ui)
        }
    }
}
