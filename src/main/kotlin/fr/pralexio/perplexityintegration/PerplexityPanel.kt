package fr.pralexio.perplexityintegration

import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefApp
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.BorderLayout

class PerplexityPanel {

    private val browser: JBCefBrowser?
    private val component: JComponent

    init {
        if (JBCefApp.isSupported()) {
            browser = JBCefBrowser("https://www.perplexity.ai")
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
