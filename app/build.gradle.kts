plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "iss.nus.edu.sg.smartmartdeliveryapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "iss.nus.edu.sg.smartmartdeliveryapp"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
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
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:VERSION")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.android.material:material:VERSION")
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    implementation("aws.sdk.kotlin:s3:1.3.0")
}