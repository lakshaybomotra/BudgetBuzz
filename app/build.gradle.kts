plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
}

android {
    namespace = "com.lbdev.budgetbuzz"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lbdev.budgetbuzz"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

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

        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //preferences
    implementation("androidx.preference:preference-ktx:1.2.1")
    //country code picker
    implementation("com.hbb20:ccp:2.7.0")
    //firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    //pinview
    implementation("io.github.chaosleung:pinview:1.4.4")
    //Biometric
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")

    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    //coil
    implementation("io.coil-kt:coil:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")

    implementation("ru.cleverpumpkin:crunchycalendar:2.6.1")
    implementation("com.kizitonwose.calendar:view:2.5.0")

//    implementation("com.github.AnyChart:AnyChart-Android:1.1.5")
//    implementation("com.github.imaNNeoFighT:ArcChartView:1.0.3.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

}