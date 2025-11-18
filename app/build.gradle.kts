plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}


android {
    namespace = "com.jhojan.school_project"
    compileSdk = 36 // ⚠️ lo más reciente estable, no existe aún 36 en SDK oficial

    defaultConfig {
        applicationId = "com.jhojan.school_project"
        minSdk = 26
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Firebase BOM
    implementation(platform(libs.firebase.bom))

    // Firebase usando BOM (sin versión)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.database.ktx)

    // Glide
    implementation(libs.glide)
    implementation(libs.google.firebase.database.ktx)
    implementation(libs.google.firebase.auth.ktx)
    annotationProcessor(libs.compiler)

    // CircleImageView
    implementation(libs.circleimageview)

    // AndroidX TOML
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.activity)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


apply(plugin = "com.google.gms.google-services")
