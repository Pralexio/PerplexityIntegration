package fr.pralexio.perplexityintegration

import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefApp
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.BorderLayout

class PerplexityPanel {

    private val browser: JBCefBrowser?
    private val component: JComponent

    init {
        if (JBCefApp.isSupported()) {
            browser = JBCefBrowser.createBuilder()
                .setOffScreenRendering(false)
                .setUrl("https://www.perplexity.ai")
                .build()

            browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
                override fun onLoadEnd(
                    cefBrowser: CefBrowser?,
                    frame: CefFrame?,
                    httpStatusCode: Int
                ) {
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
            }, browser.cefBrowser)

            component = browser.component
        } else {
            browser = null
            component = JPanel().apply {
                layout = BorderLayout()
                add(JLabel("JCEF n'est pas supporté sur ce système"), BorderLayout.CENTER)
            }
        }
    }

    fun getContent(): JComponent {
        return component
    }

    fun dispose() {
        browser?.dispose()
    }
}
