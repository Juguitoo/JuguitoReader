plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.juguito.epubengine"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
