plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType").get(),
            providers.gradleProperty("platformVersion").get()
        )
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        jetbrainsRuntime()
    }
}

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion").get()
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild").get()
        }

        changeNotes = """
            <h3>Version 2.1.0</h3>
            <ul>
                <li><strong>ADDED:</strong> Extends plugin compatibility to JetBrains IDE build 243 and newer.</li>
                <li><strong>TESTED:</strong> Verified the embedded Perplexity panel on GoLand 2024.3.</li>
            </ul>
        """.trimIndent()
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    named<JavaExec>("runIde") {
        jvmArgs(
            "-Dide.browser.jcef.osr.enabled=true",
            "-Dide.browser.jcef.gpu.disable=true"
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
