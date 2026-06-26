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
            <h3>Version 2.0</h3>
            <ul>
                <li><strong>FIXED:</strong> Prevents tool window crashes when the IDE runtime exposes an unavailable or incompatible embedded JetBrains browser (JCEF).</li>
                <li><strong>IMPROVED:</strong> Shows a clear unsupported-runtime message instead of leaving the Perplexity panel blank.</li>
                <li><strong>IMPROVED:</strong> Send actions now fail cleanly when the embedded browser is unavailable.</li>
                <li><strong>IMPROVED:</strong> Error guidance now asks users to verify IDE, OS, and runtime JCEF support and include runtime details with issue reports.</li>
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
