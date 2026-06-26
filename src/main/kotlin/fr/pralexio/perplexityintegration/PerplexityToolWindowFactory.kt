package fr.pralexio.perplexityintegration

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import java.awt.BorderLayout

class PerplexityToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        PerplexityJcefSupport.checkSupported()?.let { message ->
            addFallbackContent(toolWindow, message)
            return
        }

        ApplicationManager.getApplication().invokeLater {
            try {
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
            } catch (e: LinkageError) {
                addFallbackContent(toolWindow, PerplexityJcefSupport.messageForBrowserFailure(e))
            } catch (e: Exception) {
                addFallbackContent(toolWindow, PerplexityJcefSupport.unavailableMessage(e.message))
            }
        }
    }

    private fun addFallbackContent(toolWindow: ToolWindow, message: String) {
        val fallbackPanel = JPanel(BorderLayout())
        fallbackPanel.border = JBUI.Borders.empty(16)

        val safeMessage = message
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
        val label = JLabel(
            "<html><body style='text-align:center; width:420px;'>" +
                    "<b>${PerplexityJcefSupport.UNAVAILABLE_TITLE}</b><br><br>$safeMessage</body></html>",
            SwingConstants.CENTER
        )
        fallbackPanel.add(label, BorderLayout.CENTER)

        val content = ContentFactory.getInstance().createContent(fallbackPanel, "", false)
        content.isCloseable = false
        toolWindow.contentManager.addContent(content)
    }
}
