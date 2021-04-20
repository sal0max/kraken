import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(30)

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig {
        applicationId = "de.salomax.kraken"
        minSdkVersion(21)
        targetSdkVersion(30)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // SemVer
        val major = 1
        val minor = 0
        val patch = 3
        versionCode = (major * 10000) + (minor * 100) + patch
        versionName = "$major.$minor.$patch"
        base.archivesBaseName = "$applicationId-v$versionCode"
    }

    signingConfigs {
        create("release") {
            storeFile     = File(getSecret("KEYSTORE_FILE")!!)
            storePassword = getSecret("KEYSTORE_PASSWORD")
            keyAlias      = getSecret("KEYSTORE_KEY_ALIAS")
            keyPassword   = getSecret("KEYSTORE_KEY_PASSWORD")
        }
    }

    android {
        sourceSets["main"].java.srcDir("src/main/kotlin")
        sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isZipAlignEnabled = true
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = " [DEBUG]"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    // kotlin
    implementation(kotlin("stdlib-jdk7", version = rootProject.extra["kotlinVersion"] as String))
    // support libs
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.work:work-runtime-ktx:2.5.0")
    // downloader
    val fuelVersion = "2.3.1"
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-android:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-moshi:$fuelVersion")
    val moshiVersion = "1.12.0"
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
    // permissions
    implementation("com.github.fondesa:kpermissions:3.2.1")
    // test
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
}

task("sendErrorIntent", Exec::class) {
    commandLine(
        "adb", "shell",
        "am", "start",
        "-a", "android.intent.action.SEND",
        "-t", "text/plain",
        "--es", "android.intent.extra.TEXT",
        "\"Hallo Welt\""
    )
}

task("sendSingleIntent", Exec::class) {
    commandLine(
        "adb", "shell",
        "am", "start",
        "-a", "android.intent.action.SEND",
        "-t", "text/plain",
        "--es", "android.intent.extra.TEXT",
        "\"https://www.instagram.com/p/B68xZD3HI57?utm_source=ig_share_sheet&igshid=1jd5d0ezsv5ke\""
    )
}

task("sendSingleIntentNew", Exec::class) {
    commandLine(
        "adb", "shell",
        "am", "start",
        "-a", "android.intent.action.SEND",
        "-t", "text/plain",
        "--es", "android.intent.extra.TEXT",
        "\"https://www.instagram.com/hildeee/p/BtLzwwjBTNP/?utm_source=ig_sheet\""
    )
}

task("sendCarouselIntent", Exec::class) {
    commandLine(
        "adb", "shell",
        "am", "start",
        "-a", "android.intent.action.SEND",
        "-t", "text/plain",
        "--es", "android.intent.extra.TEXT",
        "\"https://www.instagram.com/p/BrnxhiIhsuu?utm_source=ig_share_sheet\""
    )
}

task("sendVideoIntent", Exec::class) {
    commandLine(
        "adb", "shell",
        "am", "start",
        "-a", "android.intent.action.SEND",
        "-t", "text/plain",
        "--es", "android.intent.extra.TEXT",
        "\"https://www.instagram.com/p/B8qdFfShKfs/\""
    )
}

task("sendCarouselWithVideoIntent", Exec::class) {
    commandLine(
        "adb", "shell",
        "am", "start",
        "-a", "android.intent.action.SEND",
        "-t", "text/plain",
        "--es", "android.intent.extra.TEXT",
        "\"https://www.instagram.com/p/B8rUvYWHLR7/\""
    )
}

fun getSecret(key: String): String? {
    val secretsFile = rootProject.file("secrets.properties")
    return if (secretsFile.exists()) {
        val props = Properties()
        props.load(FileInputStream(secretsFile))
        props.getProperty(key)
    } else {
        null
    }
}
