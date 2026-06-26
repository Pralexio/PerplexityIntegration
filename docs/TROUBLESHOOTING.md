# Troubleshooting

This plugin depends on the JetBrains embedded browser runtime, also known as JCEF.

## Perplexity AI Chat Unavailable

If the IDE runtime does not expose a compatible embedded browser, the tool window shows:

```text
Perplexity AI Chat unavailable
```

This means the embedded JetBrains browser is unavailable or incompatible in the current IDE/runtime. The plugin cannot repair an IDE-level JCEF runtime issue, but version 2.0 and later should avoid crashing and show a clear diagnostic message.

Before opening an issue, check online whether your IDE, operating system, and runtime officially support JCEF.

## Blank Panel Or Nothing To Display

If the panel stays blank:

1. Update the plugin to the latest version.
2. Restart the IDE.
3. Check that the IDE uses a JetBrains Runtime with JCEF support.
4. Check whether other IDE embedded browser features work, such as Markdown preview.
5. Open `idea.log` and look for JCEF, JBCef, or Perplexity errors.

## NoSuchMethodError Or NoClassDefFoundError

Errors such as these usually point to an IDE/runtime compatibility problem:

```text
java.lang.NoSuchMethodError
java.lang.NoClassDefFoundError: com/intellij/ui/jcef/JBCefApp
```

They can happen when an IDE build, EAP release, custom runtime, or bundled runtime exposes an incompatible JCEF API to plugins.

Plugin version 2.0 adds a fallback for these startup failures. If the IDE still crashes with version 2.0 or later, please open an issue with the full runtime details and `idea.log`.

## Login Does Not Work

Google and Apple may block OAuth flows inside embedded browsers.

Try one of these alternatives:

1. Use Perplexity's email login flow.
2. Configure the `__Secure-next-auth.session-token` cookie manually in Tools -> Perplexity AI.

After changing the session token, reload the Perplexity tool window.

## Slow Scrolling

JCEF off-screen rendering can scroll slower than a native browser.

Open Tools -> Perplexity AI and adjust the scroll speed multiplier. The default value is `3.0x`, and the supported range is `1.0x` to `8.0x`.

## Send Action Fails

If an editor action cannot send code:

1. Make sure a project is open.
2. Select code in the editor.
3. Open the Perplexity tool window once.
4. Confirm the privacy dialog if it appears.
5. Check whether the tool window shows a JCEF compatibility message.

## What To Include In Bug Reports

Please include:

- IDE name and version
- Plugin version
- Operating system
- IDE runtime information from Help -> About
- Whether the IDE uses bundled JBR or a custom runtime
- Whether other embedded browser features work
- Steps to reproduce
- Expected behavior
- Actual behavior
- Error notification text, if any
- Full `idea.log` when the embedded browser fails
