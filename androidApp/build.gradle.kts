plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
}
group = "biz.wolschon.wag"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    google()
    jcenter()
    mavenCentral()
}
dependencies {
    val lifecycleVersion = "2.2.0"
//    val roomVersion = "2.2.5"
    val kotlinVersion = "1.3.72"
    val navVersion = "2.3.0"

    implementation(project(":shared"))

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    // Annotation processor
    //kapt "androidx.lifecycle:lifecycle-compiler:$lifecycle_version"
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")


    // test
    //testImplementation("androidx.arch.core:core-testing:2.1.0")
    //testImplementation 'junit:junit:4.12'
    //androidTestImplementation 'com.android.support.test:runner:1.0.2'
    //androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'

    // ROOM
//    implementation "androidx.room:room-runtime:$room_version"
//    kapt "androidx.room:room-compiler:$room_version"
    // optional - Kotlin Extensions and Coroutines support for Room
//    implementation "androidx.room:room-ktx:$room_version"
    // optional - Test helpers
//    testImplementation "androidx.room:room-testing:$room_version"


    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.android.material:material:1.2.1")
    implementation("androidx.appcompat:appcompat:1.2.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    //androidTestImplementation "androidx.navigation:navigation-testing:$nav_version"



    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.0")
}
android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "biz.wolschon.wag"
        minSdkVersion(24)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        dataBinding = true
    }
}
