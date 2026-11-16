plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("storybook")
        browser()
        binaries.executable()
    }
    sourceSets {
        all {
            languageSettings.optIn("ee.schimke.composeai.rccreation.bridge.ExperimentalRcCreationApi")
            languageSettings.optIn("ee.schimke.composeai.rcplayer.compose.ExperimentalRcPlayerApi")
            languageSettings.optIn("kotlin.js.ExperimentalWasmJsInterop")
            languageSettings.optIn("androidx.compose.ui.ExperimentalComposeUiApi")
        }
        wasmJsMain.dependencies {
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
