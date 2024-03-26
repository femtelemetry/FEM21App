// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.1" apply false
    //id("embrace-swazzler") //added 0322
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("io.embrace:embrace-swazzler:6.5.0")
    }
}
