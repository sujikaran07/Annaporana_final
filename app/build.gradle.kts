plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("kotlin-android") // Kotlin plugin for Android
    id("kotlin-kapt") // For Kotlin annotation processing if needed
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
        // Target Java 17
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        // Use Java 17 toolchain if available
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }
    }

    kotlinOptions {
        // Ensure Kotlin is also targeting Java 17
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Kotlin Standard Library - make sure to use the same version consistently
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")

    // AndroidX and Material Design libraries
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.activity:activity-ktx:1.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth:21.0.1")

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore:23.0.3")

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:19.7.0")

    // Firebase Storage (for video uploads)
    implementation("com.google.firebase:firebase-storage:20.0.1")

    // Picasso library for image loading
    implementation("com.squareup.picasso:picasso:2.71828")

    // Add Navigation components
    val nav_version = "2.7.0" // Or latest stable version
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Testing libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
