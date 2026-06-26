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
        create("IC", "2025.1.4.1")
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
            <h3>Version 2.0.1</h3>
            <ul>
                <li><strong>FIXED:</strong> Restores the legacy JCEF startup path for IDEs where the embedded browser works normally.</li>
                <li><strong>FIXED:</strong> Keeps the crash guard for incompatible JCEF runtimes without blocking supported setups.</li>
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
