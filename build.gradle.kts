// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    id("com.autonomousapps.dependency-analysis") version "3.5.1"
    id("com.google.android.gms.oss-licenses-plugin") version "0.10.10" apply false
}