package fr.pralexio.perplexityintegration

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
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

    init {
        if (JBCefApp.isSupported()) {
            val toolbar = createToolbar()
            containerPanel.add(toolbar, BorderLayout.NORTH)

            loadBrowser()

            component = containerPanel
        } else {
            component = JPanel().apply {
                layout = BorderLayout()
                add(JLabel("JCEF is not supported on this system"), BorderLayout.CENTER)
            }
        }
    }

    private fun loadBrowser() {
        if (containerPanel.componentCount > 1) {
            containerPanel.remove(1)
        }

        browser?.dispose()

        browser = JBCefBrowser.createBuilder()
            .setOffScreenRendering(false)
            .setUrl("https://www.perplexity.ai")
            .build()

        browser!!.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(
                cefBrowser: CefBrowser?,
                frame: CefFrame?,
                httpStatusCode: Int
            ) {
                if (settings.sessionToken.isNotEmpty()) {
                    injectSessionToken(settings.sessionToken)
                }

                val darkModeScript = """
                    (function() {
                        const originalMatchMedia = window.matchMedia;
                        window.matchMedia = function(query) {
                            if (query.includes('prefers-color-scheme')) {
                                return {
                                    matches: query.includes('dark'),
                                    media: query,
                                    addListener: function() {},
                                    removeListener: function() {},
                                    addEventListener: function() {},
                                    removeEventListener: function() {},
                                    dispatchEvent: function() { return true; }
                                };
                            }
                            return originalMatchMedia.call(window, query);
                        };
                        
                        const style = document.createElement('style');
                        style.id = 'force-dark-mode';
                        style.textContent = `
                            :root {
                                color-scheme: dark !important;
                            }
                            body {
                                background-color: #0b0f19 !important;
                                color: #e0e0e0 !important;
                            }
                            * {
                                color-scheme: dark !important;
                            }
                        `;
                        
                        const existingStyle = document.getElementById('force-dark-mode');
                        if (existingStyle) {
                            existingStyle.remove();
                        }
                        document.head.appendChild(style);
                        
                        if (document.documentElement) {
                            document.documentElement.setAttribute('data-theme', 'dark');
                            document.documentElement.style.colorScheme = 'dark';
                        }
                        
                        try {
                            localStorage.setItem('theme', 'dark');
                            localStorage.setItem('perplexity-theme', 'dark');
                        } catch (e) {
                            console.log('Could not set localStorage:', e);
                        }
                    })();
                """.trimIndent()

                cefBrowser?.executeJavaScript(darkModeScript, cefBrowser.url, 0)
            }
        }, browser!!.cefBrowser)

        containerPanel.add(browser!!.component, BorderLayout.CENTER)
        containerPanel.revalidate()
        containerPanel.repaint()
    }

    private fun reloadBrowser() {
        browser?.let { browserInstance ->
            browserInstance.loadURL("about:blank")

            Thread {
                Thread.sleep(300)
                ApplicationManager.getApplication().invokeLater {
                    browserInstance.loadURL("https://www.perplexity.ai")
                }
            }.start()
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

        toolbar.add(setTokenButton)
        toolbar.add(clearTokenButton)
        toolbar.add(reloadButton)
        toolbar.add(helpButton)

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
                val dateFormat = SimpleDateFormat("MMM dd, yyyy")
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
            settings.sessionToken = token.trim()

            NotificationGroupManager.getInstance()
                .getNotificationGroup("Perplexity.Notifications")
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

            NotificationGroupManager.getInstance()
                .getNotificationGroup("Perplexity.Notifications")
                .createNotification(
                    "Token Cleared",
                    "Token has been removed",
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
                Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
            )

            cookieManager.setCookie("https://www.perplexity.ai", cookie)
        } catch (e: Exception) {
            // CEF not ready yet
        }
    }

    fun getContent(): JComponent {
        return component
    }

    fun dispose() {
        browser?.dispose()
    }
}
