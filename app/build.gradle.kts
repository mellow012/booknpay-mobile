
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "com.example.booknpay"
    compileSdk = 36 // Cleaned up the release(36) syntax if applicable

    defaultConfig {
        applicationId = "com.example.booknpay"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended:")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-storage-ktx:21.0.2")
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.2")

    // Google Sign-In (NEW)
    implementation("com.google.android.gms:play-services-auth:21.5.0")


    implementation("com.google.accompanist:accompanist-swiperefresh:0.36.0")

    // Coroutines for Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation(libs.firebase.analytics)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0") // Added this line back

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // For Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")
    implementation("androidx.core:core-splashscreen:1.2.0")

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
// DataStore (Preferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
// QR Code
    implementation("com.google.zxing:core:3.5.2")
// PDF Generation
    implementation("com.itextpdf:itext7-core:7.2.5")
// Animations
    implementation("com.airbnb.android:lottie-compose:6.3.0")
// Biometric Authentication
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
// Phone Number
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.27")
// Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation("androidx.hilt:hilt-work:1.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
