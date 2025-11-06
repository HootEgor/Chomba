import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.googleServices)
    // alias(libs.plugins.daggerHilt) // Removed for Koin migration
    alias(libs.plugins.compose.compiler) // Added back for app module
    alias(libs.plugins.ksp)
    // alias(libs.plugins.kotlinxSerialization) // This plugin should be applied in the shared module
}

android {
    namespace = "com.egorhoot.chomba"
    compileSdk = 36
    val patch: Int
    val versionPropsFile = file("version.properties")
    if (versionPropsFile.canRead()) {
        val versionProps = Properties()
        versionProps.load(FileInputStream(versionPropsFile))
        patch = versionProps.getProperty("PATCH").toInt()+1
        versionProps.setProperty("PATCH", patch.toString())
        versionProps.store(versionPropsFile.writer(), null)
    } else {
        throw Exception("Could not read version.properties!")
    }
    val versionN = "0.0.$patch"

    defaultConfig {
        applicationId = "com.egorhoot.chomba"
        minSdk = 26
        targetSdk = 36
        versionCode = patch
        versionName = versionN

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true // Keep this so AGP enables Compose for the Android app
    }
//    kotlinOptions { // These were already commented
//        jvmTarget = "17"
//    }
//    composeOptions { // These were already commented and Jetpack Compose specific
//        kotlinCompilerExtensionVersion = "1.5.8"
//    }
//    packaging {
//        resources {
//            excludes += "/META-INF/{AL2.0,LGPL2.1}"
//        }
//    }
}

dependencies {
    implementation(project(":shared")) // This is keys

    implementation(libs.coreKtx)
    implementation(libs.lifecycleRuntimeKtx)
    implementation(libs.activityCompose) // Still needed for setContent in Activity
    // implementation(platform(libs.composeBom)) // Removed: Compose versions managed by shared module
    // implementation(libs.lifecycleViewmodelCompose) // May keep if Android ViewModels used in app layer - REMOVED FOR KMP
    // implementation(libs.firebaseFirestore) // Replaced by KMP version in shared module
    // implementation(libs.firebaseDatabase) // Replaced by KMP version in shared module
    implementation(libs.appCompat)
    // implementation(libs.wearComposeMaterial) // Assuming this is not for the main app UI, if it is, it needs KMP alternative or to stay Android specific
    implementation(libs.junit)
    implementation(libs.androidxJunit)
    implementation(libs.espressoCore)
    implementation(libs.colorPickerCompose) // This is a Jetpack Compose library. If used in shared UI, you'll need a KMP alternative or expect/actual.
//    implementation(platform(libs.firebaseBom)) // Keep for other Android Google/Firebase services
    // implementation(libs.firebaseAuthKtx) // Replaced by KMP version in shared module
    // implementation(libs.runtimeLivedata) // REMOVED FOR KMP
    // implementation(libs.coilCompose) // Removed: Needs KMP replacement (e.g., Kamel) in shared module
    // implementation(libs.coilGif) // GIF support for the KMP image loader will also need consideration
    // implementation(libs.gson) // Replaced by kotlinx.serialization in shared module
    // implementation(libs.accompanistPermissions) // Accompanist libraries are Jetpack Compose specific. Needs KMP alternative or expect/actual.

    // Retrofit
    // implementation(libs.retrofit) // Replaced by Ktor in shared module
    // Moshi
    // implementation (libs.moshi.kotlin) // Replaced by kotlinx.serialization in shared module
    // implementation(libs.material3.android) // Removed: Provided by shared module
    // implementation(libs.ui.tooling.preview.android) // Removed: Provided by shared module
    implementation(libs.play.services.auth)

    //noinspection KaptUsageInsteadOfKsp
    // ksp (libs.moshi.kotlin.codegen) // Replaced by kotlinx.serialization in shared module

    // implementation (libs.hilt.android) // Removed for Koin migration
    // ksp (libs.hilt.compiler) // Removed for Koin migration
    // implementation(libs.hiltNavigationCompose) // Removed: Navigation should be handled by a KMP library in shared module

    implementation(libs.zxing)

    //camera
    implementation(libs.play.services.mlkit.barcode.scanning)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.guava)

    //video player
    implementation(libs.exoplayer)
    implementation(libs.media3.ui)
}