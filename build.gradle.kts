// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Projenin build.gradle dosyası
buildscript {
    dependencies {
e        classpath("")
    }
}





plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

}