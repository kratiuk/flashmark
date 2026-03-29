plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.kratiuk.flashmark"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kratiuk.flashmark"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.1.1"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("$rootDir/flashmark-release.keystore")
            storePassword = System.getenv("FLASHMARK_STORE_PWD")
            keyAlias = "flashmark"
            keyPassword = System.getenv("FLASHMARK_KEY_PWD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

afterEvaluate {
    tasks.named("installDebug") { mustRunAfter("uninstallDebug") }

    tasks.register<Exec>("debugUpdate") {
        dependsOn("installDebug")
        commandLine("adb", "shell", "am", "start", "-n", "com.kratiuk.flashmark/.MainActivity")
    }
    tasks.register<Exec>("debugReinstall") {
        dependsOn("uninstallDebug", "installDebug")
        commandLine("adb", "shell", "am", "start", "-n", "com.kratiuk.flashmark/.MainActivity")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    debugImplementation(libs.compose.ui.tooling)
}
