import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildKonfig)
}

buildkonfig {
    packageName = "com.cyanotic.olpaka"

    val versionName: String = System.getenv("VERSION_NAME") ?: "local"

    defaultConfigs {
        buildConfigField(BOOLEAN, "allowClearPreferences", "false")
        buildConfigField(STRING, "appVersion", versionName)
        buildConfigField(STRING, "appVariant", "release")
        buildConfigField(STRING, "loggingLevel", "verbose")
    }
    defaultConfigs("debug"){
        buildConfigField(STRING, "appVariant", "debug")
        buildConfigField(STRING, "loggingLevel", "verbose")
        buildConfigField(BOOLEAN, "allowClearPreferences", "true")
    }
    defaultConfigs("release"){
        buildConfigField(STRING, "appVariant", "release")
    }
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }
    
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm("desktop")
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.ktor.client.okhttp)

            implementation(libs.kotlinx.coroutines.android)
        }
        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.runtime)
            implementation(compose.ui)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.content)
            implementation(libs.ktor.content.json)
            implementation(libs.ktor.logging)

            implementation(libs.markdownRenderer)
            implementation(libs.markdownRenderer.m3)

            implementation(libs.materialKolor)

            implementation(libs.napier)

            implementation(libs.settings)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(libs.kotlinx.coroutines.swing)

            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        wasmJsMain.dependencies {

        }
    }
}

android {
    namespace = "org.cyanotic.olpaka"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "org.cyanotic.olpaka"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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

compose.desktop {
    application {
        mainClass = "org.cyanotic.olpaka.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.cyanotic.olpaka"
            packageVersion = "1.0.0"
        }
    }
}

tasks.register("replaceBaseHref") {
    doLast {
        val buildDirectory = layout.buildDirectory.get()
        val indexHtmlFile = File("$buildDirectory/dist/wasmJs/productionExecutable/index.html")
        val baseHref: String = System.getenv("BASE_HREF") ?: "/wasmJs"
        if (indexHtmlFile.exists()) {
            val content = indexHtmlFile.readText()
            val updatedContent = content.replace("\$BASE_HREF", baseHref)
            indexHtmlFile.writeText(updatedContent)
        } else {
            println("index.html not found!")
        }
    }
}

// Make sure the replacement runs after wasmJsBrowserDistribution is generated
tasks.named("wasmJsBrowserDistribution") {
    finalizedBy("replaceBaseHref")
}
