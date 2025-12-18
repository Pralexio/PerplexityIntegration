package fr.pralexio.perplexityintegration

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager

class SendToPerplexityAction : AnAction("Send to Perplexity", "Send selected code to Perplexity chat", null) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selectedText = editor.selectionModel.selectedText ?: return
        
        if (selectedText.isBlank()) return
        
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val language = psiFile?.language?.id?.lowercase() ?: ""
        
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Perplexity")
        if (toolWindow != null) {
            toolWindow.show {
                val panel = PerplexityPanelService.getInstance(project).panel
                panel?.sendCodeToChat(selectedText, language)
                panel?.focusBrowser()
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val hasSelection = editor?.selectionModel?.hasSelection() == true
        e.presentation.isEnabledAndVisible = hasSelection
    }
}
