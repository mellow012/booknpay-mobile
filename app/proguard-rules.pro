# ─── Firebase ────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ─── Firestore model classes ──────────────────────────────────────────────────
# Firestore deserialises documents into your data classes via reflection.
# Any model class annotated with @IgnoreExtraProperties or mapped from Firestore
# must be kept or fields will be stripped and objects will deserialise as nulls.
-keep class com.example.booknpay.data.model.** { *; }

# ─── Hilt ─────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ─── Retrofit / OkHttp ────────────────────────────────────────────────────────
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ─── Ktor ─────────────────────────────────────────────────────────────────────
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ─── Kotlinx serialization ────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }

# ─── Gson ─────────────────────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ─── ZXing (QR code) ──────────────────────────────────────────────────────────
-keep class com.google.zxing.** { *; }

# ─── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# ─── Coroutines ───────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ─── Timber ───────────────────────────────────────────────────────────────────
-dontwarn org.jetbrains.annotations.**

# ─── Suppress common warnings ────────────────────────────────────────────────
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe