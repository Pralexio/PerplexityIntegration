package fr.pralexio.perplexityintegration

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp

class PerplexityToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val perplexityPanel = PerplexityPanel()
        val content = ContentFactory.getInstance().createContent(
            perplexityPanel.getContent(),
            "",
            false
        )
        toolWindow.contentManager.addContent(content)
    }

    override fun isApplicable(project: Project): Boolean {
        return JBCefApp.isSupported()
    }
}
