<h1 align="center">Perplexity AI Chat for JetBrains IDEs</h1>

<p align="center">
  <a href="https://plugins.jetbrains.com/plugin/28929">
    <img src="https://img.shields.io/jetbrains/plugin/v/28929?label=Marketplace&color=blue" alt="JetBrains Marketplace version">
  </a>
  <a href="https://plugins.jetbrains.com/plugin/28929">
    <img src="https://img.shields.io/jetbrains/plugin/d/28929?label=downloads" alt="JetBrains Marketplace downloads">
  </a>
</p>

<p align="center">
  Use Perplexity directly inside your JetBrains IDE. Browse, keep your session available across restarts, and send selected code from the editor.
</p>

<p align="center">
  <img src="docs/assets/hero.png" alt="Perplexity AI Chat for JetBrains IDEs" width="100%">
</p>

## Features

- Embedded Perplexity browser in a dedicated IDE tool window
- Send selected code to Perplexity with `Ctrl+Shift+P`
- Editor context menu actions for Explain, Find Bugs, Optimize, Write Tests, Refactor, and Add Comments
- Persistent session support using the IDE PasswordSafe when available
- One-time privacy confirmation before selected code is sent
- Adjustable scroll speed and browser zoom for the embedded JCEF browser
- Clear diagnostics when the IDE embedded browser runtime is unavailable or incompatible
- Built-in settings page under Tools -> Perplexity AI

## Installation

### JetBrains Marketplace

1. Open your JetBrains IDE.
2. Go to Settings/Preferences -> Plugins.
3. Open the Marketplace tab.
4. Search for "Perplexity AI".
5. Install the plugin and restart the IDE.

### Manual Installation

1. Download the latest release from [GitHub Releases](https://github.com/Pralexio/PerplexityIntegration/releases).
2. Open Settings/Preferences -> Plugins.
3. Click the gear icon and choose Install Plugin from Disk.
4. Select the downloaded `.zip` file.
5. Restart the IDE.

## Quick Start

1. Open View -> Tool Windows -> Perplexity.
2. Sign in with Perplexity's email login flow, or configure a session token in Tools -> Perplexity AI.
3. Select code in the editor.
4. Right-click and open the Perplexity submenu.
5. Choose Send Selection or one of the prepared prompt actions.

## Documentation

- [User Guide](docs/USER_GUIDE.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Development Notes](docs/DEVELOPMENT.md)

## Requirements

- JetBrains IDE build 251 or later
- JDK 21 runtime as provided by the IDE
- Compatible JCEF support from the IDE runtime
- Internet connection

## Support

- [GitHub Issues](https://github.com/Pralexio/PerplexityIntegration/issues)
- [GitHub Discussions](https://github.com/Pralexio/PerplexityIntegration/discussions)
- [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/28929)

When reporting an embedded browser issue, include the IDE name and version, plugin version, operating system, runtime information, steps to reproduce, and `idea.log`.

---

Made by [Pralexio](https://github.com/Pralexio)
