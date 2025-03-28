plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.cardview)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.zxing.android.embedded)
    implementation(libs.core)
    implementation (libs.firebase.database)
    implementation (libs.nanohttpd)
    implementation(libs.play.services.auth)
    implementation (libs.firebase.bom)
    implementation("com.hbb20:ccp:2.7.2")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.24")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.airbnb.android:lottie:6.4.0")
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation ("com.google.android.gms:play-services-safetynet:18.1.0")

}