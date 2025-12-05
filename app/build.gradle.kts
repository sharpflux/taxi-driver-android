plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.sharpflux.logomobility"
    compileSdk = 35
    ndkVersion = "29.0.14206865"

    defaultConfig {
        applicationId = "com.sharpflux.logomobility"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.clear()
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
        }
    }

    packagingOptions {
        // Prevent accidental packaging of old native .so files from transitive AARs.
        jniLibs {
            useLegacyPackaging = false

            // Explicitly exclude the old binaries that break 16KB alignment.
            // If a new, aligned copy exists in a newer AAR it will still be packaged.
            excludes += listOf(
                "lib/arm64-v8a/libbarhopper_v3.so",
                "lib/armeabi-v7a/libbarhopper_v3.so",
                "lib/arm64-v8a/libimage_processing_util_jni.so",
                "lib/armeabi-v7a/libimage_processing_util_jni.so"
            )
        }
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

// Force resolved versions so no transitive older AAR can slip in.
configurations.all {
    resolutionStrategy {
        // Force the 16KB-compatible ML Kit. Keep this aligned with the latest available stable
        // — 17.3.0 was the minimum known to include 16 KB alignment for the barcode native libs.
        force("com.google.mlkit:barcode-scanning:17.3.0")

        // If you have play-services-mlkit added transitively, you can force a compatible version too.
        // adjust the version if you need to match your project's other Google Play Services versions.
        // force("com.google.android.gms:play-services-mlkit-barcode-scanning:18.0.0")
    }
}

dependencies {
    // CameraX
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("androidx.camera:camera-extensions:1.3.0")

    // ----- IMPORTANT: ML Kit barcode (16KB-compatible) -----
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("androidx.fragment:fragment-ktx:1.8.4")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.code.gson:gson:2.10.1")

    // SignalR + rx + okhttp
    implementation("com.microsoft.signalr:signalr:7.0.0")
    implementation("io.reactivex.rxjava3:rxjava:3.1.5")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Razorpay
    implementation("com.razorpay:checkout:1.6.40")

    // Swipe refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation(libs.appcompat)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
