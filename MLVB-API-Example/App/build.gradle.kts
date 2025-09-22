plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    buildToolsVersion = rootProject.extra["buildToolsVersion"] as String

    defaultConfig {
        applicationId = "com.tencent.mlvb.apiexample"
        minSdk = rootProject.extra["minSdkVersion"] as Int
        targetSdk = rootProject.extra["targetSdkVersion"] as Int
        versionCode = 24
        versionName = "12.8.0.5574"
        multiDexEnabled = true
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
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
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.multidex:multidex:2.0.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
    implementation(rootProject.extra["liteavSdk"] as String)
    implementation(project(":Debug"))
    implementation(project(":Basic:LivePushCamera"))
    implementation(project(":Basic:LivePushScreen"))
    implementation(project(":Basic:LivePlay"))
    implementation(project(":Basic:LebPlay"))
    implementation(project(":Basic:LiveLink"))
    implementation(project(":Basic:LivePK"))
    implementation(project(":Advanced:SwitchRenderView"))
    implementation(project(":Advanced:ThirdBeauty"))
    implementation(project(":Advanced:RTCPushAndPlay"))
    implementation(project(":Advanced:CustomVideoCapture"))
    implementation(project(":Advanced:LebAutoBitrate"))
    implementation(project(":Advanced:HlsAutoBitrate"))
    implementation(project(":Advanced:TimeShift"))
    implementation(project(":Advanced:NewTimeShiftSprite"))
    implementation(project(":Advanced:PictureInPicture"))
}