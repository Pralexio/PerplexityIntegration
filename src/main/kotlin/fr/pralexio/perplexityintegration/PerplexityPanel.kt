package fr.pralexio.perplexityintegration

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.TimeUnit
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefCookie
import org.cef.network.CefCookieManager
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*

class PerplexityPanel {

    private var browser: JBCefBrowser? = null
    private val settings = PerplexitySettings.getInstance()
    private val containerPanel = JPanel(BorderLayout())
    private val component: JComponent
    private var tokenExpirationLabel: JLabel? = null
    private var devToolsOpen = false
    private var gpuButton: JButton? = null
    private var zoomLabel: JLabel? = null
    private var currentZoom: Double = 1.0
    
    private val dateFormat by lazy { SimpleDateFormat("MMM dd, yyyy") }
    
    private val notificationGroup by lazy {
        NotificationGroupManager.getInstance().getNotificationGroup("Perplexity.Notifications")
    }
    
    private val darkModeScript: String by lazy {
        """
            (function() {
                // Override matchMedia to always report dark mode preference
                if (!window.__darkModePatched) {
                    window.__darkModePatched = true;
                    const originalMatchMedia = window.matchMedia;
                    window.matchMedia = function(query) {
                        if (query.includes('prefers-color-scheme')) {
                            return {
                                matches: query.includes('dark'),
                                media: query,
                                onchange: null,
                                addListener: function(cb) {},
                                removeListener: function(cb) {},
                                addEventListener: function(type, cb) {},
                                removeEventListener: function(type, cb) {},
                                dispatchEvent: function() { return true; }
                            };
                        }
                        return originalMatchMedia.call(window, query);
                    };
                }
                
                // Force dark color scheme on html element
                document.documentElement.style.colorScheme = 'dark';
                document.documentElement.setAttribute('data-color-mode', 'dark');
                document.documentElement.setAttribute('data-theme', 'dark');
                document.documentElement.classList.remove('light');
                document.documentElement.classList.add('dark');
                
                // Inject persistent dark mode CSS
                let style = document.getElementById('force-dark-mode-plugin');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'force-dark-mode-plugin';
                    style.textContent = `
                        :root {
                            color-scheme: dark !important;
                        }
                        html, body {
                            background-color: #191a1a !important;
                            color-scheme: dark !important;
                        }
                        html.light, body.light, [data-theme="light"], [data-color-mode="light"] {
                            background-color: #191a1a !important;
                            color-scheme: dark !important;
                        }
                    `;
                    document.head.appendChild(style);
                }
                
                // Set localStorage preferences
                try {
                    localStorage.setItem('theme', 'dark');
                    localStorage.setItem('color-theme', 'dark');
                    localStorage.setItem('perplexity-theme', 'dark');
                    localStorage.setItem('colorMode', 'dark');
                } catch (e) {}
                
                // Watch for theme changes and revert them
                if (!window.__darkModeObserver) {
                    window.__darkModeObserver = new MutationObserver(function(mutations) {
                        mutations.forEach(function(mutation) {
                            if (mutation.type === 'attributes') {
                                const html = document.documentElement;
                                if (html.classList.contains('light')) {
                                    html.classList.remove('light');
                                    html.classList.add('dark');
                                }
                                if (html.getAttribute('data-theme') === 'light') {
                                    html.setAttribute('data-theme', 'dark');
                                }
                                if (html.getAttribute('data-color-mode') === 'light') {
                                    html.setAttribute('data-color-mode', 'dark');
                                }
                                html.style.colorScheme = 'dark';
                            }
                        });
                    });
                    window.__darkModeObserver.observe(document.documentElement, {
                        attributes: true,
                        attributeFilter: ['class', 'data-theme', 'data-color-mode', 'style']
                    });
                }
            })();
        """.trimIndent()
    }

    init {
        if (JBCefApp.isSupported()) {
            val toolbar = createToolbar()
            containerPanel.add(toolbar, BorderLayout.NORTH)
            
            component = containerPanel
            
            // Load browser after component is set up, on EDT
            ApplicationManager.getApplication().invokeLater {
                loadBrowser()
            }
        } else {
            component = JPanel().apply {
                layout = BorderLayout()
                add(JLabel("JCEF is not supported on this system"), BorderLayout.CENTER)
            }
        }
    }

    private fun loadBrowser() {
        try {
            if (containerPanel.componentCount > 1) {
                containerPanel.remove(1)
            }

            browser?.dispose()
            browser = null
            
            // Check if JCEF is actually ready
            if (!JBCefApp.isSupported()) {
                showBrowserError("JCEF is not supported on this system.")
                return
            }
            
            // Inject token before loading if available
            if (settings.sessionToken.isNotEmpty()) {
                injectSessionToken(settings.sessionToken)
            }
            
            // Load saved zoom level
            currentZoom = settings.zoomLevel

            // Use simple constructor - more compatible with dev mode
            val newBrowser = JBCefBrowser("https://www.perplexity.ai")

            newBrowser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
                override fun onLoadEnd(
                    cefBrowser: CefBrowser?,
                    frame: CefFrame?,
                    httpStatusCode: Int
                ) {
                    if (frame == null || !frame.isMain) return
                    
                    // Re-inject token on each page load to ensure it persists
                    if (settings.sessionToken.isNotEmpty()) {
                        injectSessionToken(settings.sessionToken)
                    }

                    cefBrowser?.executeJavaScript(darkModeScript, cefBrowser.url, 0)
                    
                    // Apply saved zoom level
                    applyZoom()
                }
            }, newBrowser.cefBrowser)

            browser = newBrowser
            
            // Add component directly
            containerPanel.add(newBrowser.component, BorderLayout.CENTER)
            containerPanel.revalidate()
            containerPanel.repaint()
        } catch (e: Exception) {
            showBrowserError("Failed to load browser: ${e.message}")
        }
    }
    
    private fun showBrowserError(message: String) {
        ApplicationManager.getApplication().invokeLater {
            val errorLabel = JLabel("<html><center>$message<br>Try clicking Reload or restart the IDE.</center></html>")
            errorLabel.horizontalAlignment = SwingConstants.CENTER
            if (containerPanel.componentCount > 1) {
                containerPanel.remove(1)
            }
            containerPanel.add(errorLabel, BorderLayout.CENTER)
            containerPanel.revalidate()
            containerPanel.repaint()
        }
    }

    private fun reloadBrowser() {
        if (browser == null) {
            // Browser not loaded yet, try to load it
            loadBrowser()
            return
        }
        
        browser?.let { browserInstance ->
            // Re-inject token before reload
            if (settings.sessionToken.isNotEmpty()) {
                injectSessionToken(settings.sessionToken)
            }
            
            browserInstance.loadURL("about:blank")

            AppExecutorUtil.getAppScheduledExecutorService().schedule({
                ApplicationManager.getApplication().invokeLater {
                    browserInstance.loadURL("https://www.perplexity.ai")
                }
            }, 300, TimeUnit.MILLISECONDS)
        }
    }

    private fun createToolbar(): JPanel {
        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT))

        val setTokenButton = JButton("Set Token")
        setTokenButton.addActionListener {
            showTokenInputDialog()
        }

        val clearTokenButton = JButton("Clear Token")
        clearTokenButton.addActionListener {
            clearToken()
        }

        val reloadButton = JButton("Reload")
        reloadButton.addActionListener {
            reloadBrowser()
        }

        val helpButton = JButton("Help")
        helpButton.addActionListener {
            showTokenInstructions()
        }
        
        val devToolsButton = JButton("DevTools")
        devToolsButton.addActionListener {
            toggleDevTools()
        }
        
        // GPU Toggle button
        gpuButton = JButton(if (settings.gpuEnabled) "GPU: ON" else "GPU: OFF")
        gpuButton?.addActionListener {
            toggleGpu()
        }
        
        // Zoom controls
        val zoomOutButton = JButton("-")
        zoomOutButton.toolTipText = "Zoom Out"
        zoomOutButton.addActionListener {
            zoomOut()
        }
        
        zoomLabel = JLabel("${(settings.zoomLevel * 100).toInt()}%")
        
        val zoomInButton = JButton("+")
        zoomInButton.toolTipText = "Zoom In"
        zoomInButton.addActionListener {
            zoomIn()
        }
        
        val zoomResetButton = JButton("100%")
        zoomResetButton.toolTipText = "Reset Zoom"
        zoomResetButton.addActionListener {
            resetZoom()
        }

        toolbar.add(setTokenButton)
        toolbar.add(clearTokenButton)
        toolbar.add(reloadButton)
        toolbar.add(helpButton)
        toolbar.add(devToolsButton)
        toolbar.add(gpuButton)
        
        // Zoom controls separator
        toolbar.add(Box.createHorizontalStrut(10))
        toolbar.add(JLabel("|"))
        toolbar.add(Box.createHorizontalStrut(5))
        toolbar.add(zoomOutButton)
        toolbar.add(zoomLabel)
        toolbar.add(zoomInButton)
        toolbar.add(zoomResetButton)

        // Add token expiration label
        tokenExpirationLabel = JLabel()
        updateTokenExpirationLabel()
        toolbar.add(Box.createHorizontalStrut(20))
        toolbar.add(tokenExpirationLabel)

        return toolbar
    }

    private fun updateTokenExpirationLabel() {
        tokenExpirationLabel?.let { label ->
            if (settings.sessionToken.isNotEmpty()) {
                val expirationDate = Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
                label.text = "Token expires: ${dateFormat.format(expirationDate)}"
            } else {
                label.text = "No token set"
            }
        }
    }

    private fun showTokenInputDialog() {
        val token = Messages.showInputDialog(
            "Paste your Perplexity session token (__Secure-next-auth.session-token):",
            "Set Token",
            Messages.getQuestionIcon(),
            settings.sessionToken,
            null
        )

        if (token != null && token.trim().isNotEmpty()) {
            val trimmedToken = token.trim()
            
            // Basic validation - token should be reasonably long
            if (trimmedToken.length < 20) {
                notificationGroup
                    .createNotification(
                        "Invalid Token",
                        "The token appears to be too short. Please copy the full token value.",
                        NotificationType.WARNING
                    )
                    .notify(null)
                return
            }
            
            settings.sessionToken = trimmedToken
            
            // Inject token immediately
            injectSessionToken(trimmedToken)

            notificationGroup
                .createNotification(
                    "Token Saved",
                    "Reloading Perplexity...",
                    NotificationType.INFORMATION
                )
                .notify(null)

            updateTokenExpirationLabel()

            ApplicationManager.getApplication().invokeLater {
                reloadBrowser()
            }
        }
    }

    private fun clearToken() {
        val result = Messages.showYesNoDialog(
            "Are you sure you want to clear the saved token?",
            "Clear Token",
            Messages.getQuestionIcon()
        )

        if (result == Messages.YES) {
            settings.sessionToken = ""
            
            // Clear the session cookie
            clearSessionCookie()

            notificationGroup
                .createNotification(
                    "Token Cleared",
                    "Token has been removed. You will need to login again.",
                    NotificationType.INFORMATION
                )
                .notify(null)

            updateTokenExpirationLabel()

            ApplicationManager.getApplication().invokeLater {
                reloadBrowser()
            }
        }
    }

    private fun showTokenInstructions() {
        val instructions = """
            Login Methods for Perplexity
            
            PRIMARY METHOD (Recommended):
            Login directly in the browser above using Google, Apple, or Email.
            
            ALTERNATIVE METHOD (If you have login issues):
            Use a session token from your browser.
            
            How to get your session token:
            
            FOR GOOGLE CHROME:
            1. Open https://www.perplexity.ai in Chrome
            2. Sign in to your account
            3. Press F12 to open DevTools
            4. Click the "Application" tab
            5. In the left sidebar, expand "Cookies"
            6. Click on "https://www.perplexity.ai"
            7. Find "__Secure-next-auth.session-token" in the list
            8. Double-click the Value column and copy the entire token
            9. Click "Set Token" above and paste it
            
            FOR FIREFOX:
            1. Open https://www.perplexity.ai in Firefox
            2. Sign in to your account
            3. Press F12 to open DevTools
            4. Click the "Storage" tab
            5. In the left sidebar, expand "Cookies"
            6. Click on "https://www.perplexity.ai"
            7. Find "__Secure-next-auth.session-token" in the list
            8. Right-click the Value and select "Copy"
            9. Click "Set Token" above and paste it
            
            Note: Tokens typically expire after 30 days.
        """.trimIndent()

        Messages.showMessageDialog(
            instructions,
            "How to Login",
            Messages.getInformationIcon()
        )
    }

    private fun injectSessionToken(token: String) {
        try {
            val cookieManager = CefCookieManager.getGlobalManager()
            val expirationDate = Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)

            // Main session token
            val cookie = CefCookie(
                "__Secure-next-auth.session-token",
                token,
                ".perplexity.ai",
                "/",
                true,  // secure
                true,  // httpOnly
                null,  // creation
                null,  // lastAccess
                true,  // hasExpires
                expirationDate
            )
            cookieManager.setCookie("https://www.perplexity.ai", cookie)
            
            // Also set for www subdomain
            val wwwCookie = CefCookie(
                "__Secure-next-auth.session-token",
                token,
                "www.perplexity.ai",
                "/",
                true,
                true,
                null,
                null,
                true,
                expirationDate
            )
            cookieManager.setCookie("https://www.perplexity.ai", wwwCookie)
        } catch (e: Exception) {
            // CEF not ready yet - will retry on page load
        }
    }
    
    private fun clearSessionCookie() {
        try {
            val cookieManager = CefCookieManager.getGlobalManager()
            // Delete by setting expired cookie
            val expiredDate = Date(0)
            
            val expiredCookie = CefCookie(
                "__Secure-next-auth.session-token",
                "",
                ".perplexity.ai",
                "/",
                true,
                true,
                null,
                null,
                true,
                expiredDate
            )
            cookieManager.setCookie("https://www.perplexity.ai", expiredCookie)
            
            val expiredWwwCookie = CefCookie(
                "__Secure-next-auth.session-token",
                "",
                "www.perplexity.ai",
                "/",
                true,
                true,
                null,
                null,
                true,
                expiredDate
            )
            cookieManager.setCookie("https://www.perplexity.ai", expiredWwwCookie)
        } catch (e: Exception) {
            // Ignore errors
        }
    }

    private fun toggleDevTools() {
        browser?.let { browserInstance ->
            try {
                browserInstance.openDevtools()
                devToolsOpen = !devToolsOpen
            } catch (e: Exception) {
                // DevTools not available or already open
            }
        }
    }
    
    private fun toggleGpu() {
        settings.gpuEnabled = !settings.gpuEnabled
        gpuButton?.text = if (settings.gpuEnabled) "GPU: ON" else "GPU: OFF"
        
        notificationGroup
            .createNotification(
                "GPU ${if (settings.gpuEnabled) "Enabled" else "Disabled"}",
                "Please restart the IDE for the change to take effect.",
                NotificationType.INFORMATION
            )
            .notify(null)
    }
    
    private fun zoomIn() {
        if (currentZoom < 2.0) {
            currentZoom += 0.1
            settings.zoomLevel = currentZoom
            applyZoom()
            updateZoomLabel()
        }
    }
    
    private fun zoomOut() {
        if (currentZoom > 0.5) {
            currentZoom -= 0.1
            settings.zoomLevel = currentZoom
            applyZoom()
            updateZoomLabel()
        }
    }
    
    private fun resetZoom() {
        currentZoom = 1.0
        settings.zoomLevel = currentZoom
        applyZoom()
        updateZoomLabel()
    }
    
    private fun applyZoom() {
        browser?.let { browserInstance ->
            browserInstance.cefBrowser.zoomLevel = currentZoom - 1.0
        }
    }
    
    private fun updateZoomLabel() {
        zoomLabel?.text = "${(currentZoom * 100).toInt()}%"
    }
    
    fun sendCodeToChat(code: String, language: String = "") {
        browser?.let { browserInstance ->
            val escapedCode = code
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("`", "\\`")
                .replace("\$", "\\\$")
                .replace("\n", "\\n")
                .replace("\r", "")
            
            val langInfo = if (language.isNotEmpty()) " ($language)" else ""
            val fullText = "Here is my code$langInfo:\\n\\n$escapedCode"
            
            val script = """
                (function() {
                    console.log('[Perplexity Plugin] Attempting to send code to chat...');
                    
                    // Try multiple selectors for Perplexity's input
                    const selectors = [
                        'textarea[placeholder*="Ask"]',
                        'textarea[placeholder*="ask"]', 
                        'textarea[placeholder*="anything"]',
                        'textarea[placeholder*="Follow"]',
                        'textarea',
                        '[contenteditable="true"]',
                        'div[role="textbox"]'
                    ];
                    
                    let input = null;
                    for (const selector of selectors) {
                        input = document.querySelector(selector);
                        if (input) {
                            console.log('[Perplexity Plugin] Found input with selector:', selector);
                            break;
                        }
                    }
                    
                    if (input) {
                        const text = "$fullText";
                        
                        if (input.tagName === 'TEXTAREA') {
                            input.focus();
                            input.value = text;
                            input.dispatchEvent(new Event('input', { bubbles: true, cancelable: true }));
                            input.dispatchEvent(new Event('change', { bubbles: true, cancelable: true }));
                            console.log('[Perplexity Plugin] Set textarea value');
                        } else {
                            input.focus();
                            input.textContent = text;
                            input.dispatchEvent(new InputEvent('input', { bubbles: true, cancelable: true, data: text }));
                            console.log('[Perplexity Plugin] Set contenteditable text');
                        }
                    } else {
                        console.error('[Perplexity Plugin] Could not find chat input element');
                    }
                })();
            """.trimIndent()
            
            browserInstance.cefBrowser.executeJavaScript(script, browserInstance.cefBrowser.url, 0)
        }
    }
    
    fun focusBrowser() {
        browser?.component?.requestFocusInWindow()
    }

    fun getContent(): JComponent {
        return component
    }

    fun dispose() {
        browser?.dispose()
    }
}
