
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.ncorti.ktfmt.gradle") version "0.16.0"
    id("com.google.gms.google-services")
    id("com.google.secrets_gradle_plugin") version "0.6"
    id("kotlin-kapt")
    kotlin("plugin.serialization") version "1.8.0"
}
android {
    namespace = "com.github.se.studybuddies"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.github.se.studybuddies"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        //load the values from .properties file
        val mapsKeyFile = rootProject.file("./local.properties")
        val properties = Properties()
        properties.load(mapsKeyFile.inputStream())

        //fetch the map key
        val apiKey = properties.getProperty("MAPS_API_KEY") ?: ""

        //inject the key dynamically into the manifest
        manifestPlaceholders["MAPS_API_KEY"] = apiKey
        manifestPlaceholders["STREAM"] = properties.getProperty("STREAM", "")
    }


    configurations.configureEach {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }

    signingConfigs {
        create("release") {
            val properties = Properties()
            properties.load(FileInputStream("app/keystore.properties"))
            storeFile = file(properties["storeFile"] as String)
            storePassword = properties["storePassword"] as String
            keyAlias = properties["keyAlias"] as String
            keyPassword = properties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }
    testOptions {
        packagingOptions {
            jniLibs {
                useLegacyPackaging = true
            }
        }
        unitTests.isReturnDefaultValues = true
    }
}



dependencies {

    androidTestImplementation ("androidx.test.uiautomator:uiautomator:2.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    implementation("com.google.code.gson:gson:2.8.8")
    implementation( "androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.4.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.wear.compose:compose-material:1.3.1")
    implementation("androidx.room:room-common:2.6.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    implementation("androidx.compose.material:material:1.1.1")
    //implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material3:material3:1.2.0-rc01")
    implementation("androidx.navigation:navigation-compose:2.6.0-rc01")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.google.accompanist:accompanist-permissions:0.31.0-alpha")
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.maps.android:maps-compose-utils:4.3.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
// Google API client library for Android



    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui-graphics")
    implementation("com.google.android.gms:play-services-auth:20.6.0")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation ("com.firebaseui:firebase-ui-auth:7.2.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.3")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("com.google.firebase:firebase-database:20.3.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.7")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.7")
    androidTestImplementation("com.kaspersky.android-components:kaspresso:1.4.3")
    // Allure support
    androidTestImplementation("com.kaspersky.android-components:kaspresso-allure-support:1.4.3")
    // Jetpack Compose support
    androidTestImplementation("com.kaspersky.android-components:kaspresso-compose-support:1.4.1")
    implementation("androidx.fragment:fragment:1.5.5")
    implementation("com.squareup.okhttp3:okhttp:3.10.0")
    testImplementation("org.mockito:mockito-core:3.11.2")
    testImplementation("org.mockito:mockito-inline:2.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    // Dependency for using Intents in instrumented tests
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")

    testImplementation("org.robolectric:robolectric:4.11.1")

    // Dependencies for using MockK in instrumented tests
    testImplementation("io.mockk:mockk:1.13.7")
    androidTestImplementation("io.mockk:mockk:1.13.7")
    androidTestImplementation("io.mockk:mockk-android:1.13.7")
    androidTestImplementation("io.mockk:mockk-agent:1.13.7")

    //Location service
    implementation("com.google.android.gms:play-services-location:20.0.0")

    // Stream Video Compose SDK
    implementation("io.getstream:stream-video-android-ui-compose:0.5.1")

    // For Robolectric tests.
    testImplementation("com.google.dagger:hilt-android-testing:2.44")
    // ...with Kotlin.
    kaptTest("com.google.dagger:hilt-android-compiler:2.44")
    // ...with Java.
    testAnnotationProcessor("com.google.dagger:hilt-android-compiler:2.44")

    // For instrumented tests.
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.44")
    // ...with Kotlin.
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.44")
    // ...with Java.
    androidTestAnnotationProcessor("com.google.dagger:hilt-android-compiler:2.44")
    androidTestImplementation("org.mockito:mockito-android:3.12.4")



}
tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")
    reports {
        xml.required = true
        html.required = true
    }
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**SignatureChecks.*"
    )
    val debugTree = fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.buildDir) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
    })
}
// Avoid redundant tests, debug is sufficient
tasks.withType<Test> {
    onlyIf {
        !name.toLowerCaseAsciiOnly().contains("release")
    }
}

secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "local.properties"
}