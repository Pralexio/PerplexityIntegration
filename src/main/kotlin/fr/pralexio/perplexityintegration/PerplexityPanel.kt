package fr.pralexio.perplexityintegration

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.TimeUnit
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefCookie
import org.cef.network.CefCookieManager
import org.cef.network.CefRequest
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.util.*
import javax.swing.*

class PerplexityPanel {

    private var browser: JBCefBrowser? = null
    private val settings = PerplexitySettings.getInstance()
    private val containerPanel = JPanel(BorderLayout())
    private val component: JComponent
    private var devToolsOpen = false
    private var zoomLabel: JLabel? = null
    private var currentZoom: Double = 1.0

    private val notificationGroup by lazy {
        NotificationGroupManager.getInstance().getNotificationGroup("Perplexity.Notifications")
    }

    private val darkModeScript: String by lazy {
        """
            (function() {
                const originalMatchMedia = window.matchMedia;
                window.matchMedia = function(query) {
                    if (query.includes('prefers-color-scheme')) {
                        return {
                            matches: query.includes('dark'),
                            media: query,
                            onchange: null,
                            addListener: function() {},
                            removeListener: function() {},
                            addEventListener: function() {},
                            removeEventListener: function() {},
                            dispatchEvent: function() { return true; }
                        };
                    }
                    return originalMatchMedia.call(this, query);
                };
                
                document.documentElement.style.setProperty('color-scheme', 'dark', 'important');
                document.documentElement.setAttribute('data-color-mode', 'dark');
                document.documentElement.setAttribute('data-theme', 'dark');
                
                try {
                    localStorage.setItem('theme', 'dark');
                    localStorage.setItem('color-theme', 'dark');
                } catch (e) {}
                
                window.dispatchEvent(new Event('storage'));
            })();
        """.trimIndent()
    }

    init {
        if (JBCefApp.isSupported()) {
            val toolbar = createToolbar()
            containerPanel.add(toolbar, BorderLayout.NORTH)

            component = containerPanel

            AppExecutorUtil.getAppScheduledExecutorService().schedule({
                ApplicationManager.getApplication().invokeLater {
                    loadBrowser()
                }
            }, 2000, TimeUnit.MILLISECONDS)
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

            if (!JBCefApp.isSupported()) {
                showBrowserError("JCEF is not supported on this system.")
                return
            }

            if (settings.sessionToken.isNotEmpty()) {
                injectSessionToken(settings.sessionToken)
            }

            currentZoom = settings.zoomLevel

            val newBrowser = JBCefBrowser.createBuilder()
                .setOffScreenRendering(true)
                .setUrl("about:blank")
                .build()

            newBrowser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
                override fun onLoadStart(
                    cefBrowser: CefBrowser?,
                    frame: CefFrame?,
                    transitionType: CefRequest.TransitionType?
                ) {
                    if (frame != null && frame.isMain) {
                        cefBrowser?.executeJavaScript(darkModeScript, cefBrowser.url, 0)
                    }
                }

                override fun onLoadEnd(
                    cefBrowser: CefBrowser?,
                    frame: CefFrame?,
                    httpStatusCode: Int
                ) {
                    if (frame == null || !frame.isMain) return

                    if (settings.sessionToken.isNotEmpty()) {
                        injectSessionToken(settings.sessionToken)
                    }

                    cefBrowser?.executeJavaScript(darkModeScript, cefBrowser.url, 0)
                    applyZoom()
                }
            }, newBrowser.cefBrowser)

            browser = newBrowser

            containerPanel.add(newBrowser.component, BorderLayout.CENTER)
            containerPanel.revalidate()
            containerPanel.repaint()

            AppExecutorUtil.getAppScheduledExecutorService().schedule({
                ApplicationManager.getApplication().invokeLater {
                    newBrowser.loadURL("https://www.perplexity.ai")
                }
            }, 500, TimeUnit.MILLISECONDS)

        } catch (e: Exception) {
            e.printStackTrace()
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
            loadBrowser()
            return
        }

        browser?.let { browserInstance ->
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

        val settingsButton = JButton("⚙ Settings")
        settingsButton.toolTipText = "Open Perplexity settings"
        settingsButton.addActionListener {
            ShowSettingsUtil.getInstance().showSettingsDialog(null, PerplexityConfigurable::class.java)
        }

        val reloadButton = JButton("↻ Reload")
        reloadButton.toolTipText = "Reload Perplexity"
        reloadButton.addActionListener {
            reloadBrowser()
        }

        val devToolsButton = JButton("⚒ DevTools")
        devToolsButton.toolTipText = "Open browser developer tools"
        devToolsButton.addActionListener {
            toggleDevTools()
        }

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

        toolbar.add(settingsButton)
        toolbar.add(reloadButton)
        toolbar.add(devToolsButton)

        toolbar.add(Box.createHorizontalStrut(10))
        toolbar.add(JLabel("|"))
        toolbar.add(Box.createHorizontalStrut(5))
        toolbar.add(zoomOutButton)
        toolbar.add(zoomLabel)
        toolbar.add(zoomInButton)
        toolbar.add(zoomResetButton)

        return toolbar
    }

    private fun injectSessionToken(token: String) {
        try {
            val cookieManager = CefCookieManager.getGlobalManager()
            val expirationDate = Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)

            val cookie = CefCookie(
                "__Secure-next-auth.session-token",
                token,
                ".perplexity.ai",
                "/",
                true,
                true,
                null,
                null,
                true,
                expirationDate
            )
            cookieManager.setCookie("https://www.perplexity.ai", cookie)

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
        }
    }

    private fun toggleDevTools() {
        browser?.let { browserInstance ->
            try {
                browserInstance.openDevtools()
                devToolsOpen = !devToolsOpen
            } catch (e: Exception) {
            }
        }
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
                        if (input) break;
                    }
                    
                    if (input) {
                        const text = "$fullText";
                        
                        if (input.tagName === 'TEXTAREA') {
                            input.focus();
                            input.value = text;
                            input.dispatchEvent(new Event('input', { bubbles: true, cancelable: true }));
                            input.dispatchEvent(new Event('change', { bubbles: true, cancelable: true }));
                        } else {
                            input.focus();
                            input.textContent = text;
                            input.dispatchEvent(new InputEvent('input', { bubbles: true, cancelable: true, data: text }));
                        }
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
