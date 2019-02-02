import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(28)

    defaultConfig {
        applicationId = "de.salomax.tuck"
        minSdkVersion(21)
        targetSdkVersion(28)
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        // SemVer
        val major = 1
        val minor = 0
        val patch = 0
        versionCode = (major * 10000) + (minor * 100) + patch
        versionName = "$major ($major.$minor.$patch)"
    }

    signingConfigs {
        create("release") {
            storeFile     = File(getSecret("KEYSTORE_FILE"))
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
}

dependencies {
    val retrofitVersion = "2.5.0"
    val supportLibVersion = "28.0.0"

    // kotlin
    implementation(kotlin("stdlib-jdk7", version = rootProject.extra["kotlinVersion"] as String))
    // rx java
    implementation("io.reactivex.rxjava2:rxandroid:2.1.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion")
    // retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    // support libs
    implementation("com.android.support:support-compat:$supportLibVersion") // support.v4.app.NotificationCompat
    implementation("com.android.support:support-v4:$supportLibVersion")     // overwrite v27 referenced by kpermissions
    implementation("com.android.support:appcompat-v7:$supportLibVersion")   // overwrite v26 referenced by rxdownload
    // permissions
    implementation("com.github.fondesa:kpermissions:1.0.0")
    // downloader
    implementation("zlc.season:rxdownload3:1.2.7")
    // test
    androidTestImplementation("com.android.support.test:runner:1.0.2")
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
        "\"https://www.instagram.com/p/BrncKOOB83d?utm_source=ig_share_sheet&igshid=1jd5d0ezsv5ke\""
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
        "\"https://www.instagram.com/p/Brns9TgCq0Q/\""
    )
}

task("sendCarouselWithVideoIntent", Exec::class) {
    commandLine(
        "adb", "shell",
        "am", "start",
        "-a", "android.intent.action.SEND",
        "-t", "text/plain",
        "--es", "android.intent.extra.TEXT",
        "\"https://www.instagram.com/p/BrqDSekAdc-/\""
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
