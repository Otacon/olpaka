import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.util.Base64

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.sqldelight)
}

buildkonfig {
    packageName = "com.cyanotic.olpaka"

    val versionName = System.getenv("VERSION_NAME") ?: "local"
    val analyticsMeasurementId = System.getenv("ANALYTICS_MEASUREMENT_ID") ?: ""
    val analyticsApiSecret = System.getenv("ANALYTICS_API_SECRET") ?: ""
    val firebaseWebConfig = System.getenv("FIREBASE_WEB_CONFIG_JSON") ?: ""

    val decodedWebConfig = Base64.getDecoder().decode(firebaseWebConfig).decodeToString()

    defaultConfigs {
        buildConfigField(BOOLEAN, "allowClearPreferences", "false")
        buildConfigField(STRING, "appVersion", versionName)
        buildConfigField(STRING, "appVariant", "release")
        buildConfigField(STRING, "loggingLevel", "warning")
        buildConfigField(STRING, "analyticsMeasurementId", analyticsMeasurementId)
        buildConfigField(STRING, "analyticsApiSecret", analyticsApiSecret)
        buildConfigField(STRING, "firebaseWebConfigJson", decodedWebConfig)
    }
    defaultConfigs("debug") {
        buildConfigField(STRING, "appVariant", "debug")
        buildConfigField(STRING, "loggingLevel", "verbose")
        buildConfigField(BOOLEAN, "allowClearPreferences", "true")
    }
}

tasks.withType<Test> {
    testLogging {

        events = setOf(
            TestLogEvent.PASSED,
            TestLogEvent.FAILED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STANDARD_ERROR,
        )

        exceptionFormat = TestExceptionFormat.FULL
        ignoreFailures = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true

        reports.html.required = false
        reports.junitXml.required = true
    }

}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

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
            implementation(libs.sqldelight.driver.android)
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
            implementation(libs.androidx.navigation.compose)

            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
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
            implementation(libs.sqldelight.driver.desktop)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.driver.native)
        }

        wasmJsMain.dependencies {
            implementation(devNpm("firebase", "10.13.2"))
        }

        jsMain.dependencies {
            implementation(libs.sqldelight.driver.js)
            implementation(npm("sql.js", "1.8.0"))
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
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

sqldelight {
    databases {
        create("Database") {
            packageName.set("org.cyanotic.olpaka")
            generateAsync.set(true)
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
            println("Set base href to $baseHref in index.html")
        } else {
            println("index.html not found!")
        }
    }
}

// Make sure the replacement runs after wasmJsBrowserDistribution is generated
tasks.named("wasmJsBrowserDistribution") {
    finalizedBy("replaceBaseHref")
}
