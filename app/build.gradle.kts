plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.rahul.symptoscan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rahul.symptoscan"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {

    //  CORE (stable)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    //  COMPOSE (stable)
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // NAVIGATION
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // VIEWMODEL
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    //  ROOM
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    //  RETROFIT
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //  OKHTTP
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    //  FIREBASE
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // GOOGLE SIGN-IN
    implementation("com.google.android.gms:play-services-auth:20.6.0")

    // COIL
    implementation("io.coil-kt:coil-compose:2.4.0")

    // TEST
    testImplementation("junit:junit:4.13.2")

    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}