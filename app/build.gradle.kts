plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  checkstyle
}

android {
    namespace = "ptiny.load"
    compileSdk = 36
    defaultConfig {
        applicationId = "ptiny.load"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.9.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}

checkstyle {
    toolVersion = "10.24.0"
}

tasks.register<org.gradle.api.plugins.quality.Checkstyle>("checkImport") {
    configFile = rootProject.file("config/checkstyle/unusedimport.xml")

    source = fileTree("src/main/java") {
        include("**/*.java")
    }

    classpath = files()
}
