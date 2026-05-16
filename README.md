# Perplexity AI for IntelliJ IDEA

[![Get from Marketplace](https://img.shields.io/jetbrains/plugin/v/28929?label=Perplexity&color=blue)](https://plugins.jetbrains.com/plugin/28929)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/28929?label=downloads)](https://plugins.jetbrains.com/plugin/28929)
![Rating](https://img.shields.io/badge/rating-4.3★-brightgreen)

Access Perplexity AI directly in your IDE without switching windows. Get instant AI-powered answers, code explanations, and research assistance while you develop.

![Perplexity Integration Demo](https://raw.githubusercontent.com/Pralexio/PerplexityIntegration/refs/heads/main/screenshot.png)

## ✨ Features

- 🚀 **Embedded Browser** - Full Perplexity experience in a tool window
- 💻 **Send Code to Chat** - Select code and send it directly to Perplexity (`Ctrl+Shift+P`)
- 🎯 **Prompt Actions** - Built-in actions for *Explain*, *Find Bugs*, *Optimize*, *Write Tests*, *Refactor*, *Add Comments*
- ⚡ **Scroll Speed Boost** - Adjustable multiplier (1.0x – 8.0x) to compensate for JCEF off-screen rendering
- 🔒 **Secure Token Storage** - Session token stored in the IDE's PasswordSafe (OS-encrypted)
- ⚠️ **Privacy Warning** - One-time confirmation before code leaves the IDE
- 🔄 **Retry on Failure** - Clear error overlay with a retry button when Perplexity is unreachable
- 🌙 **Native Dark Mode** - Automatic dark theme synchronized with your IDE
- 🔐 **Session Persistence** - Stay logged in across IDE restarts
- 🎯 **Smart Notifications** - Get feedback when sending code (with line count)
- 🔍 **Zoom Controls** - Adjust browser zoom level (saved per session)
- 🛠️ **DevTools** - Built-in browser developer tools for debugging
- ⚙️ **Dedicated Settings** - Clean UI with organized settings panel

## 📦 Installation

### Via JetBrains Marketplace

1. Open IntelliJ IDEA
2. Go to **Settings/Preferences** → **Plugins**
3. Click on **Marketplace** tab
4. Search for **"Perplexity AI"**
5. Click **Install** and restart IDE

### Manual Installation

1. Download the latest release from [Releases](https://github.com/Pralexio/PerplexityIntegration/releases)
2. Open IntelliJ IDEA → **Settings/Preferences** → **Plugins**
3. Click ⚙️ → **Install Plugin from Disk**
4. Select the downloaded `.zip` file
5. Restart IDE

## 🚀 Quick Start

1. **Open the plugin**
   - Click on the **Perplexity** tab in the right sidebar
   - Or use **View → Tool Windows → Perplexity**

2. **Login (Choose one method)**

   **Method 1: Direct Login (Recommended)**
   - Login directly in the embedded browser using your email
   - Use Google, Apple, or Email to sign in
   - Even if you created your account with Google on Perplexity, they send a code to login with your email

   **Method 2: Session Token (Alternative)**
   - If you have login issues, open **Settings** (toolbar button or Tools → Perplexity AI)
   - Click **"How to Get Token"** for detailed instructions
   - Paste your token and click **Apply**
   - Token lasts ~30 days

3. **Send Code to Perplexity**
   - Select code in your editor
   - Right-click → **Perplexity ▶** to pick an action:
     - **Send Selection** (or press `Ctrl+Shift+P`) — sends the raw code
     - **Explain Selection** — detailed explanation of what the code does
     - **Find Bugs** — bug, edge-case and race-condition review
     - **Optimize** — performance and readability suggestions
     - **Write Tests** — unit test generation
     - **Refactor** — readability/maintainability refactor (behavior preserved)
     - **Add Comments** — adds inline comments for the non-obvious parts
   - The code appears in the Perplexity chat prefixed with the matching prompt
   - A notification confirms the send (with line count)
   - The first time, a one-time privacy confirmation dialog is shown

## 🛠️ Toolbar Features

| Button | Description |
|--------|-------------|
| **⚙ Settings** | Open settings dialog (token management, GPU options) |
| **↻ Reload** | Refresh the Perplexity page |
| **⚒ DevTools** | Open browser developer tools for debugging |
| **- / +** | Zoom controls for the browser view |
| **100%** | Reset zoom to default |

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl+Shift+P` | Send selected code to Perplexity (raw) |

> **Note:** The editor must have focus for the shortcut to work. The other prompt actions (Explain, Find Bugs, etc.) are available from the **Perplexity ▶** sub-menu on right-click — you can bind your own shortcuts via **Settings → Keymap**.

## ⚙️ Requirements

- IntelliJ IDEA 2025.1+ (or any compatible JetBrains IDE on build 251+)
- JCEF (Java Chromium Embedded Framework) support
- Internet connection

## 🐛 Known Limitations

- **Scroll Speed (mitigated in v1.9):** JCEF off-screen rendering scrolls slower than a native browser. v1.9 adds a JavaScript-level scroll multiplier (Settings → Tools → Perplexity AI, default 3.0x, range 1.0x – 8.0x) that compensates for most of the slowdown. Increase the multiplier if it still feels too slow.
- **Google / Apple Sign-In:** Google and Apple block OAuth flows inside embedded browsers (CEF/JCEF) as a security policy — this cannot be worked around at the plugin level. Use the **email login** (Perplexity sends a one-time code, even for Google-linked accounts) or the **session token** method instead.

## ⚙️ Settings

Access via **Tools → Perplexity AI** or the **⚙ Settings** button in the toolbar:

- **Session Token:** Manually set your Perplexity authentication token (stored in the IDE's PasswordSafe — OS-encrypted, not in plain XML)
- **Scroll Speed Multiplier:** Adjust mouse wheel scroll speed inside the panel (1.0x – 8.0x, default 3.0x). Applied live, no reload required.
- **GPU Acceleration:** Enable/disable hardware acceleration (may help with performance on some systems)
- **Zoom Level:** Persistent zoom setting (also adjustable via toolbar)
- **Reset Privacy Warning:** Show the send confirmation dialog again on the next "Send to Perplexity" action

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 🐛 Reporting Issues

Found a bug or have a feature request? [Open an issue](https://github.com/Pralexio/PerplexityIntegration/issues/new)

Please include:
- IntelliJ IDEA version
- Plugin version
- Operating System
- Steps to reproduce
- Expected vs. actual behavior
- Error notifications (if any)

## 🏗️ Development

### Prerequisites
- JDK 21 or later
- Gradle 8.0+

### Build & Run
```bash
# Run plugin in development IDE
./gradlew runIde

# Build plugin
./gradlew buildPlugin

# Run tests
./gradlew test
```

## 📝 Changelog

### v1.9 (Latest)
- ✨ **New:** "Perplexity ▶" sub-menu in the editor right-click with six prompt actions: *Explain*, *Find Bugs*, *Optimize*, *Write Tests*, *Refactor*, *Add Comments*. `Ctrl+Shift+P` still triggers the raw send.
- ⚡ **New:** Scroll speed multiplier (1.0x – 8.0x, default 3.0x) — workaround for the slow JCEF off-screen rendering scroll. Live update from Settings without reload.
- 🔒 **New:** Session token migrated to PasswordSafe (Windows DPAPI / macOS Keychain / Linux Secret Service). Existing tokens migrate automatically on first launch.
- ⚠️ **New:** Privacy confirmation dialog before the first "Send to Perplexity", with a *Reset privacy warning* button in Settings.
- 🔄 **New:** Failure overlay with a **Retry** button when Perplexity can't be loaded (network down, captive portal, etc.) instead of a blank page.
- 🛠️ **Improved:** Platform compatibility for IntelliJ 2024.1+ (`getActionUpdateThread`, `DumbAware`, proper `Disposable` with handler cleanup).
- 🎨 **Improved:** Tool window icon switched to SVG (HiDPI / theme-aware).
- 🔍 **Improved:** Logger replaces silent `catch` blocks — `idea.log` now contains useful diagnostics.
- 🏗️ **Build:** Version, `sinceBuild`, and change notes centralized in `gradle.properties` and injected via `patchPluginXml`.
- 🐛 **Fixed:** Marketplace change notes previously stuck on "Initial version".

### v1.8
- ✨ **New:** Dedicated Settings dialog for cleaner UI
- ✨ **New:** Smart notifications with error diagnostics for "Send to Perplexity"
- ✨ **New:** Success notifications showing line count when sending code
- ✨ **New:** Zoom controls with persistent settings
- 🌙 **Improved:** Native dark mode using matchMedia override (more reliable)
- 🎨 **Improved:** Simplified toolbar - moved settings to dedicated panel
- 🔍 **Improved:** Better diagnostics for action failures
- 🐛 **Fixed:** JCEF initialization issues on Windows
- 🐛 **Fixed:** Dark mode persistence across page loads

### v1.6
- ✨ **New:** Send selected code to Perplexity with right-click or `Ctrl+Shift+P`
- 🛠️ **New:** DevTools button for browser debugging
- 🌙 **Improved:** Dark mode now persists and won't revert to light theme
- 🔐 **Improved:** Token login reliability with validation
- ⚡ **Improved:** Performance optimizations

### v1.5
- ✨ Token-based authentication system
- 📊 Token expiration tracking
- 📖 Enhanced help with Chrome/Firefox guides
- 🎨 Improved UI with toolbar
- 🔄 Better page reload mechanism

## 🙏 Acknowledgments

- [Perplexity AI](https://www.perplexity.ai) for the amazing AI platform
- JetBrains for the IntelliJ Platform SDK
- All contributors who help improve this plugin

## 📧 Support

- **Issues:** [GitHub Issues](https://github.com/Pralexio/PerplexityIntegration/issues)
- **Discussions:** [GitHub Discussions](https://github.com/Pralexio/PerplexityIntegration/discussions)

---

Made with ❤️ by [Pralexio](https://github.com/Pralexio)  
Powered by [Tickrate France](https://tickrate.fr)

⭐ Star this repo if you find it useful!
