plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.charlyghislain.openopenradioautomotive"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.charlyghislain.openopenradioautomotive"
        minSdk = 28
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(project(":openopenradioservice"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.hilt.work)
    implementation(libs.hilt)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)

    kapt(libs.hilt.compiler)
    kapt(libs.androidx.hilt.compiler)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}