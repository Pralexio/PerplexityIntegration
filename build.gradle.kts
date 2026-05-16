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
            <h3>Version 1.9</h3>
            <ul>
                <li><strong>NEW:</strong> Dedicated Settings dialog for cleaner UI</li>
                <li><strong>NEW:</strong> Better error notifications for "Send to Perplexity" action</li>
                <li><strong>NEW:</strong> Success notifications showing line count when sending code</li>
                <li><strong>IMPROVED:</strong> Native dark mode using matchMedia override</li>
                <li><strong>IMPROVED:</strong> Simplified toolbar with Settings button</li>
                <li><strong>IMPROVED:</strong> Better diagnostics for action failures</li>
                <li><strong>IMPROVED:</strong> Session token now stored via the IDE secure credential store (PasswordSafe). Existing tokens migrate automatically on first launch.</li>
                <li><strong>FIXED:</strong> JCEF initialization issues on Windows</li>
                <li><strong>FIXED:</strong> Dark mode persistence across page loads</li>
                <li><strong>FIXED:</strong> Action threading model now declares background-thread updates as required by recent platform versions.</li>
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
