plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrainsCompose) // Added Compose Multiplatform plugin
    alias(libs.plugins.android.lint)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinxSerialization) // Added Kotlinx Serialization plugin
    alias(libs.plugins.kotlin.native.cocoapods)
}

kotlin {

    androidLibrary {
        namespace = "com.egorhoot.shared"
        compileSdk = 36
        minSdk = 26

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    val xcfName = "sharedKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    // Enable Compose for the project
    compose {

    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.stdlib)
            // Add KMP dependencies here
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.resources)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.core) // Added KMP coroutines

            // Ktor for networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Kotlinx Serialization for JSON
            implementation(libs.kotlinx.serialization.json)

            // Koin DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Coil3 for KMP image loading
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network.ktor)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test) // Koin for testing
        }

        androidMain.dependencies {

            implementation(libs.ktor.client.android)

            // Firebase
            implementation(project.dependencies.platform(libs.firebaseBom))
            implementation(libs.firebase.common)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.database)

            //permissions
            implementation(libs.accompanistPermissions)
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidxJunit)
            }
        }

        iosMain.dependencies {

            implementation(libs.ktor.client.darwin)

        }
    }

    cocoapods {
        version = "10.22.0"
        summary = "Shared module for Chomba"
        homepage = "your.project.url" // Replace with actual URL
        ios.deploymentTarget = "13.0" // Match your project's target
        framework {
            baseName = "sharedKit" // Matches your xcfName
        }

        pod("FirebaseAuth")
        pod("FirebaseFirestore")
        pod("FirebaseDatabase")
    }
}
