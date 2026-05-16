package fr.pralexio.perplexityintegration

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.BorderLayout

class PerplexityToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        if (!JBCefApp.isSupported()) {
            val errorPanel = JPanel(BorderLayout())
            errorPanel.add(JLabel("JCEF is not supported on this system"), BorderLayout.CENTER)

            val content = ContentFactory.getInstance().createContent(errorPanel, "", false)
            content.isCloseable = false
            toolWindow.contentManager.addContent(content)
            return
        }

        ApplicationManager.getApplication().invokeLater {
            try {
                JBCefApp.getInstance()

                val perplexityPanel = PerplexityPanel()
                val content = ContentFactory.getInstance().createContent(
                    perplexityPanel.getContent(),
                    "",
                    false
                )
                content.isCloseable = false
                toolWindow.contentManager.addContent(content)

                PerplexityPanelService.getInstance(project).panel = perplexityPanel

                Disposer.register(content) {
                    PerplexityPanelService.getInstance(project).panel = null
                    perplexityPanel.dispose()
                }
            } catch (e: Exception) {
                val errorPanel = JPanel(BorderLayout())
                errorPanel.add(JLabel("Failed to initialize JCEF: ${e.message}"), BorderLayout.CENTER)

                val content = ContentFactory.getInstance().createContent(errorPanel, "", false)
                content.isCloseable = false
                toolWindow.contentManager.addContent(content)
            }
        }
    }
}
