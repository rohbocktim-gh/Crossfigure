plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "app.crossfigure"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.crossfigure"
        minSdk = 26
        targetSdk = 36
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = "1.1"
    }

    val keystorePath: String? = System.getenv("KEYSTORE_FILE")

    // Google's public test ad IDs. Debug builds always use these.
    val testAppId = "ca-app-pub-3940256099942544~3347511713"
    val testBannerId = "ca-app-pub-3940256099942544/6300978111"

    // Release builds use your real AdMob IDs if the ADMOB_APP_ID and
    // ADMOB_BANNER_ID repository variables are set; otherwise test IDs.
    val liveAppId = System.getenv("ADMOB_APP_ID")?.takeIf { it.isNotBlank() } ?: testAppId
    val liveBannerId = System.getenv("ADMOB_BANNER_ID")?.takeIf { it.isNotBlank() } ?: testBannerId

    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = testAppId
            buildConfigField("String", "ADMOB_BANNER_ID", "" + '"' + testBannerId + '"')
        }
        release {
            isMinifyEnabled = false
            manifestPlaceholders["admobAppId"] = liveAppId
            buildConfigField("String", "ADMOB_BANNER_ID", "" + '"' + liveBannerId + '"')
            if (keystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.05.01"))
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.gms:play-services-ads:24.4.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")
}
