plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-kapt")
}
group = "biz.wolschon.wag"
version = "1.2-SNAPSHOT"

repositories {
    gradlePluginPortal()
    google()
    jcenter()
    mavenCentral()
}
dependencies {
    val lifecycleVersion = "2.3.0-beta01"
//    val roomVersion = "2.2.5"
    val kotlinVersion = "1.4.0"
    val navVersion = "2.3.2"

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

    // must be the same as our gradle version
    kapt("com.android.databinding:compiler:5.4.1")

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
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.appcompat:appcompat:1.3.0-beta01")

    // asking for permissions in a  new way
    implementation("androidx.activity:activity-ktx:1.2.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    //androidTestImplementation "androidx.navigation:navigation-testing:$nav_version"
}
android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "biz.wolschon.wag"
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode = 3
        versionName = "1.2"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility("1.8")
        targetCompatibility("1.8")
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
