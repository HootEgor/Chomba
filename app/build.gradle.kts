import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.daggerHilt)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.egorhoot.chomba"
    compileSdk = 35
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
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.coreKtx)
    implementation(libs.lifecycleRuntimeKtx)
    implementation(libs.activityCompose)
    implementation(platform(libs.composeBom))
    implementation(libs.lifecycleViewmodelCompose)
    implementation(libs.firebaseFirestore)
    implementation(libs.firebaseDatabase)
    implementation(libs.appCompat)
    implementation(libs.wearComposeMaterial)
    implementation(libs.junit)
    implementation(libs.androidxJunit)
    implementation(libs.espressoCore)
    implementation(libs.colorPickerCompose)
    implementation(platform(libs.firebaseBom))
    implementation(libs.firebaseAuthKtx)
    implementation(libs.runtimeLivedata)
    implementation(libs.coilCompose)
    implementation(libs.gson)
    implementation(libs.accompanistPermissions)

    // Retrofit
    implementation(libs.retrofit)
    // Moshi
    implementation (libs.moshi.kotlin)
    implementation(libs.material3.android)
    implementation(libs.ui.tooling.preview.android)
    implementation(libs.play.services.auth)
    //noinspection KaptUsageInsteadOfKsp
    kapt (libs.moshi.kotlin.codegen)

    implementation (libs.hilt.android)
    kapt (libs.hilt.compiler)
    implementation(libs.hiltNavigationCompose)
}