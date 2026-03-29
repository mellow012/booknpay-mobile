// Top-level build file
plugins {
    // 1. Core Android Application Plugin
    alias(libs.plugins.android.application) apply false

    // 2. Kotlin Plugins (Compose, Serialization, etc. are now top-level)
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false

    // 3. Developer Tools (Hilt, KSP, Firebase)
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
}