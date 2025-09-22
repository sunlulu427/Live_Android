// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        mavenCentral()
        google()
        @Suppress("DEPRECATION")
        jcenter()
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        flatDir {
            dirs("libs")
            dirs(project(":App").file("libs"))
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

extra.apply {
    set("compileSdkVersion", 34)
    set("buildToolsVersion", "29.0.3")
    set("minSdkVersion", 19)
    set("targetSdkVersion", 26)
    set("supportSdkVersion", "29.0.3")
    set("liteavSdk", "com.tencent.liteav:LiteAVSDK_Professional:latest.release")
    set("versionCode", 1)
    set("versionName", "v1.0")
    set("ndkAbi", "armeabi") // ,'armeabi-v7a', 'arm64-v8a'
    set("aekit_version", "1.0.10-cloud")
}