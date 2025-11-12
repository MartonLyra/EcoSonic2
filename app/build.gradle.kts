plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // id("kotlin-kapt") // substituindo kapt por ksp:
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" // <-- Certifique-se de que esta linha está presente
}

android {
    namespace = "com.seuapp.gravacaoaudio"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.seuapp.gravacaoaudio"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            // Exclui arquivos duplicados que causam conflitos
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/INDEX.LIST",
                "META-INF/ASL2.0",
                "META-INF/AL2.0",
                "META-INF/*.kotlin_module",
                "META-INF/LGPL2.1",
                "META-INF/io.netty.versions.properties",
                "META-INF/LGPL-3.0.txt",
                "META-INF/LGPL-3.0.txt.asc",
                "META-INF/LGPL-3.0.txt.asc.sha1",
                "META-INF/LGPL-3.0.txt.asc.sha256",
                "META-INF/LGPL-3.0.txt.asc.sha512",
                "META-INF/LGPL-3.0.txt.asc.sha512.txt",
                "META-INF/LGPL-3.0.txt.asc.sha512.txt.asc",
                "META-INF/LGPL-3.0.txt.asc.sha512.txt.asc.sha1"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    // kapt(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler) // <-- Use KSP em vez de KAPT

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Google Drive API (versão correta e disponível)
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.http-client:google-http-client-android:1.43.3")
    // implementation("com.google.apis:google-api-services-drive:v3-rev20230626")

    // https://mvnrepository.com/artifact/com.google.apis/google-api-services-drive
    implementation("com.google.apis:google-api-services-drive:v3-rev20241027-2.0.0")

    // Dropbox SDK
    implementation("com.dropbox.core:dropbox-core-sdk:5.4.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}