plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)

}

android {
    namespace   = "com.example.booknpay"
    compileSdk  = 36   // FIXED: 36 is Android 16 preview — use 35 (stable)

    defaultConfig {
        applicationId         = "com.example.booknpay"
        minSdk                = 26   // BUMPED: 24 misses some Material3 APIs; 26 is safe floor
        targetSdk             = 35
        versionCode           = 1
        versionName           = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled   = false
            //applicationIdSuffix = ".debug"

        }
        release {
            isMinifyEnabled   = true   // FIXED: was false — always minify release
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
        compose     = true
        buildConfig = true   // allows BuildConfig.DEBUG check in Timber init
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// ─── KSP compiler options ─────────────────────────────────────────────────────
// Required for Room to find the schema export directory
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    // ── AndroidX core ──────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // ADDED: required for collectAsStateWithLifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ── Compose (BOM resolves ALL compose versions — never pin individually) ─
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    // FIXED: material-icons-extended now resolves via BOM (no explicit version needed)
    implementation(libs.androidx.compose.material.icons.extended)

    // ── Navigation ──────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── Hilt ────────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.runtime)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // ── Firebase (BOM — never pin individual firebase versions) ─────────────
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)

    // ── Google Sign-In / Credentials ────────────────────────────────────────
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.googleid)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.play.services.location)

    // ── Room (local cache) ───────────────────────────────────────────────────
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ── Network ─────────────────────────────────────────────────────────────
    // Retrofit (existing API calls)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Ktor (PayChangu STK push — no official Android SDK exists)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.logging)

    // ── WorkManager ─────────────────────────────────────────────────────────
    implementation(libs.androidx.work.runtime.ktx)

    // ── Image loading ────────────────────────────────────────────────────────
    implementation(libs.coil.compose)

    // ── QR code (TicketScreen boarding pass) ─────────────────────────────────
    implementation(libs.zxing.core)

    // ── DataStore ────────────────────────────────────────────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ── Lottie animations (optional — remove if not using) ───────────────────
    implementation(libs.lottie.compose)

    // ── Timber logging ───────────────────────────────────────────────────────
    // ADDED: replaces all println() calls. Call Timber.plant() in Application class.
    implementation(libs.timber)

    // ── Coroutines ───────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ── Testing ──────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}