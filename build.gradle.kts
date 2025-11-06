// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.googleServices) apply false
//    alias(libs.plugins.daggerHilt) apply false
    // alias(libs.plugins.compose.compiler) apply false // REMOVED this line
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.jetbrainsCompose) apply false // ADDED for central management
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.kotlin.native.cocoapods) apply false
}