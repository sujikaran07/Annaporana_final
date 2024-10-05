plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.recipeapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.recipeapp"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX and Material Design libraries
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.activity:activity:1.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth:21.0.1")

    // Firebase Firestore (optional if using Firestore for other purposes)
    implementation("com.google.firebase:firebase-firestore:23.0.3")

    // Add Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:19.7.0")

    // Add Firebase Storage (for video uploads)
    implementation("com.google.firebase:firebase-storage:20.0.1")

    // Testing libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
