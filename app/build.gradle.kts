import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

@DisableCachingByDefault(because = "Each successful build must create a new timestamped APK archive.")
abstract class ArchiveDebugApkTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceApk: RegularFileProperty

    @get:Internal
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun archive() {
        val source = sourceApk.get().asFile
        check(source.isFile) { "Debug APK was not generated: ${source.absolutePath}" }

        val destinationDirectory = outputDirectory.get().asFile
        check(destinationDirectory.isDirectory || destinationDirectory.mkdirs()) {
            "Unable to create APK output directory: ${destinationDirectory.absolutePath}"
        }
        val existingApks = destinationDirectory.listFiles { file ->
            file.isFile && file.extension.equals("apk", ignoreCase = true)
        }.orEmpty()
        check(existingApks.isEmpty()) {
            "APK output directory must be empty before archiving: ${destinationDirectory.absolutePath}"
        }

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val destination = destinationDirectory.resolve("C11005_Debug_${timestamp}.apk")
        val temporaryDestination = destinationDirectory.resolve(".${destination.name}.tmp")
        check(!destination.exists()) { "Timestamped APK already exists: ${destination.absolutePath}" }
        check(!temporaryDestination.exists()) {
            "Temporary APK archive already exists: ${temporaryDestination.absolutePath}"
        }
        try {
            source.copyTo(temporaryDestination, overwrite = false)
            Files.move(
                temporaryDestination.toPath(),
                destination.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
            )
        }
        catch (error: Exception) {
            if (temporaryDestination.exists() && !temporaryDestination.delete()) {
                error.addSuppressed(
                    IllegalStateException("Unable to remove incomplete APK archive: ${temporaryDestination.absolutePath}"),
                )
            }
            throw error
        }
        val archivedApks = destinationDirectory.listFiles { file ->
            file.isFile && file.extension.equals("apk", ignoreCase = true)
        }.orEmpty()
        check(archivedApks.size == 1 && archivedApks.single().canonicalFile == destination.canonicalFile) {
            "APK output directory must contain exactly one completed APK."
        }
        println("Archived Debug APK: ${destination.absolutePath}")
    }
}

@DisableCachingByDefault(because = "Historical APK archives must be removed before each Debug build.")
abstract class ClearApkOutputTask : DefaultTask() {
    @get:Internal
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun clear() {
        val directory = outputDirectory.get().asFile
        check(directory.isDirectory || directory.mkdirs()) {
            "Unable to create APK output directory: ${directory.absolutePath}"
        }
        val targets = directory.listFiles { file ->
            file.isFile &&
                (file.extension.equals("apk", ignoreCase = true) || file.name.endsWith(".apk.tmp"))
        }.orEmpty()
        targets.forEach { target ->
            check(target.delete()) { "Unable to delete historical APK: ${target.absolutePath}" }
        }
        check(directory.listFiles { file ->
            file.isFile && file.extension.equals("apk", ignoreCase = true)
        }.orEmpty().isEmpty()) {
            "APK output directory still contains historical APK files."
        }
        println("Cleared ${targets.size} historical APK archive(s) from ${directory.absolutePath}")
    }
}

val signingDirectory = rootProject.file("signing").canonicalFile
val signingPropertiesFile = signingDirectory.resolve("keystore.properties")
val signingProperties = Properties().apply {
    if (signingPropertiesFile.isFile) {
        signingPropertiesFile.inputStream().use { load(it) }
    }
}
val debugStoreFileName = signingProperties.getProperty("storeFile")
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: "C11005-debug.keystore"
val debugKeystore = signingDirectory.resolve(debugStoreFileName).canonicalFile
require(debugKeystore.toPath().startsWith(signingDirectory.toPath())) {
    "The debug keystore must be stored inside the signing directory."
}
val debugStorePassword = signingProperties.getProperty("storePassword")
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: providers.gradleProperty("C11005_DEBUG_STORE_PASSWORD")
    .orElse(providers.environmentVariable("C11005_DEBUG_STORE_PASSWORD"))
    .orNull
val debugKeyAlias = signingProperties.getProperty("keyAlias")
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: providers.gradleProperty("C11005_DEBUG_KEY_ALIAS")
    .orElse(providers.environmentVariable("C11005_DEBUG_KEY_ALIAS"))
    .orNull
val debugKeyPassword = signingProperties.getProperty("keyPassword")
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: providers.gradleProperty("C11005_DEBUG_KEY_PASSWORD")
    .orElse(providers.environmentVariable("C11005_DEBUG_KEY_PASSWORD"))
    .orNull
val hasCustomDebugSigning = debugKeystore.isFile &&
    listOf(debugStorePassword, debugKeyAlias, debugKeyPassword).all { !it.isNullOrBlank() }

if (signingPropertiesFile.isFile && !debugKeystore.isFile) {
    logger.warn("Custom debug keystore is not present in the signing directory; using the Android default debug signing key.")
}
if (debugKeystore.isFile && !hasCustomDebugSigning) {
    throw GradleException("The debug keystore exists, but its local signing configuration is incomplete.")
}

android {
    namespace = "com.example.money_log"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.money_log"
        minSdk = 23
        targetSdk = 36
        versionCode = 5
        versionName = "1.0.5_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            if (hasCustomDebugSigning) {
                storeFile = debugKeystore
                storePassword = debugStorePassword
                keyAlias = debugKeyAlias
                keyPassword = debugKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
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
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

val cleanApkOutput = tasks.register<ClearApkOutputTask>("cleanApkOutput") {
    group = "build"
    description = "Removes historical APK archives before a new Debug build starts."
    outputDirectory.set(rootProject.layout.projectDirectory.dir("Apk_Output"))
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
    dependsOn(cleanApkOutput)
}

tasks.register<ArchiveDebugApkTask>("archiveDebugApk") {
    group = "build"
    description = "Builds and archives a timestamped Debug APK without deleting older archives."
    dependsOn("assembleDebug")
    sourceApk.set(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
    outputDirectory.set(rootProject.layout.projectDirectory.dir("Apk_Output"))
}
