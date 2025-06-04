plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.calendarapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.calendarapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.google.android.material:material:1.5.0")
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth:22.1.2")
    implementation ("com.facebook.android:facebook-android-sdk:16.0.1")
    implementation ("com.google.android.gms:play-services-auth:20.6.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")
    // Google Play Billing Library
    implementation ("com.android.billingclient:billing:6.1.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    // QR Code generation
    implementation ("com.google.zxing:core:3.5.2")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    // Google AI SDK (Gemini) - Cập nhật version mới
    implementation ("com.google.ai.client.generativeai:generativeai:0.7.0")
    implementation ("com.google.guava:guava:31.0.1-android")
    implementation ("org.reactivestreams:reactive-streams:1.0.4")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}