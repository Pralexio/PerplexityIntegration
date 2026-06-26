# Development Notes

This project is a Kotlin IntelliJ Platform plugin that embeds Perplexity in a JCEF browser tool window and sends selected editor code into the Perplexity chat page.

## Prerequisites

- JDK 21
- Gradle wrapper from this repository
- JetBrains IDE compatible with IntelliJ Platform build 251 or later

## Commands

```bash
./gradlew runIde
./gradlew buildPlugin
./gradlew test
./gradlew verifyPlugin
```

Generated plugin archives are written to:

```text
build/distributions/
```

## Versioning

Plugin coordinates are defined in `gradle.properties`:

- `pluginGroup`
- `pluginVersion`
- `pluginSinceBuild`

Do not hardcode the plugin version or since-build in `src/main/resources/META-INF/plugin.xml`. The Gradle IntelliJ Platform plugin injects those values during `patchPluginXml`.

Marketplace change notes are defined in the `changeNotes` block in `build.gradle.kts`.

## Project Structure

```text
src/main/kotlin/fr/pralexio/perplexityintegration/
src/main/resources/META-INF/plugin.xml
src/main/resources/icons/
docs/
```

Important files:

| File | Purpose |
| --- | --- |
| `PerplexityToolWindowFactory.kt` | Creates the Perplexity tool window and installs the fallback UI when JCEF is unavailable |
| `PerplexityPanel.kt` | Owns the JCEF browser, toolbar, session cookie injection, dark mode script, scroll boost, zoom, and send-to-chat JavaScript |
| `PerplexityJcefSupport.kt` | Centralizes JCEF support checks and unsupported-runtime messages |
| `PerplexityPromptActions.kt` | Defines editor actions for sending selected code |
| `PerplexityPromptText.kt` | Builds the text sent to Perplexity for each action |
| `PerplexityPanelService.kt` | Stores the current project panel instance for editor actions |
| `PerplexityCredentialStore.kt` | Stores the Perplexity session token in PasswordSafe with XML fallback |
| `PerplexitySettings.kt` | Persists plugin settings in `perplexity-integration.xml` |
| `PerplexityConfigurable.kt` | Implements Tools -> Perplexity AI settings UI |
| `plugin.xml` | Registers services, settings, tool window, notifications, and editor actions |

## Runtime Flow

1. `PerplexityToolWindowFactory` creates the tool window.
2. `PerplexityJcefSupport.checkSupported()` verifies that JCEF is available.
3. `PerplexityPanel` creates a `JBCefBrowser` in off-screen rendering mode.
4. The panel loads `about:blank`, then `https://www.perplexity.ai`.
5. Load handlers inject dark mode, session cookies, scroll boost, and zoom.
6. Editor actions retrieve the current panel from `PerplexityPanelService`.
7. `PerplexityPromptText` builds the prompt text.
8. `PerplexityPanel.sendCodeToChat()` injects JavaScript that fills the Perplexity input.

## JCEF Compatibility

Always check JCEF support before creating a browser.

The plugin intentionally catches `LinkageError` around JCEF startup because some IDE builds can expose missing or incompatible JCEF classes/methods at runtime.

When JCEF is unavailable or incompatible:

- the tool window should not crash;
- the panel should show a clear unsupported-runtime message;
- editor send actions should fail cleanly with a notification.

Do not add browser fallback behavior that sends user code outside the embedded browser unless the behavior is explicit and documented.

## Authentication

The plugin uses the Perplexity cookie:

```text
__Secure-next-auth.session-token
```

The token is stored in the IDE PasswordSafe when available. If PasswordSafe is memory-only or cannot round-trip the value, the plugin keeps the legacy XML value as fallback.

The token is injected for both `.perplexity.ai` and `www.perplexity.ai` domains.

## Adding A Prompt Action

1. Add a subclass of `PerplexitySendActionBase` in `PerplexityPromptActions.kt`.
2. Give it a user-facing action text, description, and instruction.
3. Register the action in the `Perplexity.SendGroup` group in `plugin.xml`.
4. Keep the action focused on selected editor text.

## Release Checklist

1. Update `pluginVersion` in `gradle.properties`.
2. Update `changeNotes` in `build.gradle.kts`.
3. Update README or docs when user-facing behavior changes.
4. Build the plugin archive with `./gradlew buildPlugin`.
5. Upload the ZIP from `build/distributions/` to JetBrains Marketplace.
6. Create a GitHub release with concise release notes.
