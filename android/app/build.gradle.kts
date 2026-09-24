plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.ai.altercode"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ai.altercode"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "alter 2.7"

        // Extract native debug symbols (from SQLCipher's .so) into the AAB
        // so Google Play Console can symbolicate native crash traces.
        ndk {
            debugSymbolLevel = "FULL"
        }

        // Public environment variables are injected into BuildConfig so the
        // app can reach backend services without storing secrets in source code.
        val functionsUrl = providers.environmentVariable("EXPO_PUBLIC_RORK_FUNCTIONS_URL").orNull
            ?: project.findProperty("EXPO_PUBLIC_RORK_FUNCTIONS_URL")?.toString()
            ?: "https://codewise-ai-y3dvf84-backend.rork.app"

        buildConfigField("String", "EXPO_PUBLIC_RORK_FUNCTIONS_URL", "\"$functionsUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
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
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.koin.androidx.compose)
    implementation(libs.play.services.ads)
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.security.crypto)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test"))
}
