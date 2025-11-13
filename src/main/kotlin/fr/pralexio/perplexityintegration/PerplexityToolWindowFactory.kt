package fr.pralexio.perplexityintegration

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class PerplexityToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        System.setProperty("ide.browser.jcef.gpu.disable", "true")
        System.setProperty("ide.browser.jcef.sandbox.enable", "false")
        System.setProperty("ide.browser.jcef.darkTheme.enabled", "true")

        val perplexityPanel = PerplexityPanel()
        val content = ContentFactory.getInstance().createContent(
            perplexityPanel.getContent(),
            "",
            false
        )
        content.isCloseable = false
        toolWindow.contentManager.addContent(content)

        Disposer.register(content, perplexityPanel::dispose)
    }
}
