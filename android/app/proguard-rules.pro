# ──────────────────────────────────────────────────────────────
# AlterCode ProGuard / R8 rules
# Keeps classes that are accessed via reflection, JNI, or
# serialization so R8 shrinking doesn't strip them at runtime.
# ──────────────────────────────────────────────────────────────

# ── Kotlin metadata ────────────────────────────────────────────
# Reflection-based libraries read @kotlin.Metadata to inspect
# class structure at runtime.
-keep class kotlin.Metadata { *; }
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations

# ── App model classes ──────────────────────────────────────────
# Keep data classes and enums used in serialization, database
# mapping, and navigation so their names/fields survive shrinking.
-keep class com.ai.altercode.data.** { *; }
-keepclassmembers class com.ai.altercode.data.** {
    <init>(...);
    <fields>;
}
-keep enum com.ai.altercode.data.** { *; }

# ── kotlinx.serialization ──────────────────────────────────────
# The compiler-generated $serializer classes and companion objects
# are looked up by reflection. If R8 renames or removes them,
# JSON encoding/decoding in AiCodeService will crash at runtime.
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep all @Serializable classes and their serializable fields
-keep @kotlinx.serialization.Serializable class **
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    <init>(...);
    <fields>;
}

# ── SQLCipher (net.zetetic) ────────────────────────────────────
# SQLCipher uses JNI to call into the native sqlcipher .so library.
# Field names and method signatures must match what the native
# layer expects, or the app will crash on database open.
-keep,includedescriptorclasses class net.zetetic.database.** { *; }
-keep,includedescriptorclasses interface net.zetetic.database.** { *; }
-dontwarn net.zetetic.database.**

# ── Ktor ───────────────────────────────────────────────────────
# Ktor's Android engine and content negotiation use reflection to
# load plugins and serializers.
-keep class io.ktor.** { *; }
-keep class io.ktor.**$* { *; }
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**

# ── Koin ───────────────────────────────────────────────────────
# Koin resolves dependencies by class/constructor reflection.
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ── Coil 3 ─────────────────────────────────────────────────────
# Coil uses reflection to load image loader components.
-keep class coil3.** { *; }
-dontwarn coil3.**
-dontwarn coil.**

# ── Google Play Services Ads (AdMob) ───────────────────────────
# The Ads SDK is heavily reflection-based and loads classes by name.
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.google.ads.**

# ── AndroidX Security Crypto ───────────────────────────────────
# EncryptedSharedPreferences uses reflection internally.
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ── Suppress harmless missing-class warnings ───────────────────
# These optional dependencies are not on the classpath but R8
# references them during static analysis.
-dontwarn javax.naming.**
-dontwarn org.slf4j.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ── Keep R8 / retrace mapping output ───────────────────────────
# Ensure the mapping file is generated (default behavior when
# minify is enabled, but explicit for clarity).
# ── Repackage classes ───────────────────────────────────────
# Let R8 move all obfuscated classes into the root package (enables the
# "Repackage Classes" optimization Play Console checks for).
-repackageclasses ''

-printconfiguration
