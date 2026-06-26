# User Guide

This guide explains how to use Perplexity AI Chat inside a JetBrains IDE.

## Open The Tool Window

Open View -> Tool Windows -> Perplexity.

The plugin loads Perplexity in an embedded JetBrains browser. Your session can be kept across IDE restarts when you sign in or configure a session token.

## Sign In

The recommended login method is Perplexity's email login flow inside the embedded browser.

If the embedded login flow does not work for your account, open Tools -> Perplexity AI and configure a session token manually. The plugin stores the token in the IDE PasswordSafe when available.

Google and Apple OAuth flows may be blocked inside embedded browsers. If OAuth sign-in fails, use email login or the session token method.

## Session Token Method

1. Open https://www.perplexity.ai in Chrome or Firefox.
2. Sign in to your Perplexity account.
3. Open browser developer tools.
4. Find the `__Secure-next-auth.session-token` cookie for `https://www.perplexity.ai`.
5. Copy the full token value.
6. Open Tools -> Perplexity AI in the IDE.
7. Paste the token in the Session Token field.
8. Apply the settings and reload the Perplexity tool window.

Keep this token private. It gives access to your Perplexity session.

## Send Selected Code

1. Select code in the editor.
2. Right-click the selection.
3. Open the Perplexity submenu.
4. Choose one of the available actions.

| Action | Purpose |
| --- | --- |
| Send Selection | Sends the selected code as-is |
| Explain Selection | Requests a detailed explanation |
| Find Bugs | Reviews bugs, edge cases, and race conditions |
| Optimize | Requests performance and readability improvements |
| Write Tests | Requests unit tests |
| Refactor | Requests a behavior-preserving refactor |
| Add Comments | Requests comments for non-obvious code |

The raw send action is also available with `Ctrl+Shift+P`.

Before the first send, the plugin shows a privacy confirmation dialog. You can reset this confirmation from Tools -> Perplexity AI.

## Tool Window Toolbar

| Control | Description |
| --- | --- |
| Settings | Opens Tools -> Perplexity AI |
| Reload | Reloads the Perplexity page |
| DevTools | Opens browser developer tools |
| - / + | Adjusts browser zoom |
| 100% | Resets browser zoom |

## Settings

Open Tools -> Perplexity AI or use the Settings button in the tool window.

Available settings:

- Session token
- Scroll speed multiplier
- Browser zoom level
- Reset privacy warning

## Scroll Speed

JCEF off-screen rendering can scroll slower than a normal browser. The scroll speed multiplier is a plugin-side workaround for that behavior.

The default multiplier is `3.0x`. You can adjust it from Tools -> Perplexity AI.

## Privacy

Selected code is sent to Perplexity only when you trigger one of the send actions.

Review your selection before sending. Do not send secrets, credentials, private keys, access tokens, or private customer data.
