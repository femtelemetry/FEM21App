// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.1" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false //added by Wee

}

buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("io.embrace:embrace-swazzler:6.5.0")
        classpath("com.google.gms:google-services:4.4.1")
        classpath("com.android.tools.build:gradle:8.1.4")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
    }
}
