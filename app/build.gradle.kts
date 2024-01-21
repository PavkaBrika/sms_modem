plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.breakneck.sms_modem"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.breakneck.sms_modem"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        setProperty("archivesBaseName", "SMS-modem-v$versionCode($versionName)")
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
    packaging {
        jniLibs {
            excludes.add("META-INF/*")
            excludes.add("META-INF/licenses/*")
        }
        resources {
            excludes.add("META-INF/*")
            excludes.add("META-INF/licenses/*")
            excludes.add("**/attach_hotspot_windows.dll")
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    //Ktor Server
    implementation("io.ktor:ktor-server-core:1.6.4")
    implementation("io.ktor:ktor-gson:1.6.4")
    implementation("io.ktor:ktor-server-netty:1.6.2")
    implementation("io.ktor:ktor-websockets:1.6.2")

    //Network
    val okhttpVersion = "4.11.0"
    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.retrofit2:adapter-rxjava3:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    //MVVM
    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    //Koin
    val koinVersion = "3.5.3"
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-android:$koinVersion")

    //Dagger2
    implementation("com.google.dagger:dagger:2.50")
    kapt("com.google.dagger:dagger-compiler:2.50")
    kapt("com.google.dagger:dagger-android-processor:2.50")
    implementation("com.google.dagger:dagger-android-support:2.50")
    implementation("com.google.dagger:dagger-android:2.50")
    implementation("javax.inject:javax.inject:1")
    kapt("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.5.0")

    //Clean architecture
    implementation(project(path = ":domain"))
    implementation(project(path = ":data"))

    //Appodeal ads
    implementation("com.appodeal.ads.sdk:core:3.2.0")
    implementation("com.appodeal.ads.sdk.networks:bidmachine:3.2.0.0")
    implementation("com.appodeal.ads.sdk.networks:bidon:3.2.0.0")
    implementation("com.appodeal.ads.sdk.services:sentry_analytics:3.2.0.0")
    implementation("com.appodeal.ads.sdk.services:stack_analytics:3.2.0.0")
    implementation("com.appodeal.ads.sdk.networks:yandex:3.2.0.0")
    implementation("com.appodeal.ads.sdk.networks:iab:3.2.0.0")
//    implementation("com.appodeal.ads:sdk:3.2.0.+") {
//        exclude(group = "com.appodeal.ads.sdk.services", module = "adjust")
//        exclude(group = "com.appodeal.ads.sdk.services", module = "appsflyer")
//        exclude(group = "com.appodeal.ads.sdk.services", module = "firebase")
//        exclude(group = "com.appodeal.ads.sdk.services", module = "facebook_analytics")
//    }

    //Default
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}