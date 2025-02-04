//import jdk.internal.org.jline.utils.Log.debug
import org.gradle.internal.impldep.bsh.commands.dir

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.workshiftapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.workshiftapp"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.credentials)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.credentials.v122)
    implementation("com.google.apis:google-api-services-calendar:v3-rev411-1.25.0") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
    }
    implementation(libs.play.services.auth)
    implementation("com.google.api-client:google-api-client-android:2.7.1") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
    }

    implementation(libs.glide)
    implementation(libs.play.services.identity)
    implementation(libs.gson)
    implementation(libs.kernel.v900)

    implementation(libs.layout.v900)

    implementation (libs.itext7.core.v722)
    implementation(libs.itextpdf)


}
