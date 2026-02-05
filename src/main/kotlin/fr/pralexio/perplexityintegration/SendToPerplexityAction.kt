package fr.pralexio.perplexityintegration

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager

class SendToPerplexityAction : AnAction("Send to Perplexity", "Send selected code to Perplexity chat", null) {

    private val notificationGroup by lazy {
        NotificationGroupManager.getInstance().getNotificationGroup("Perplexity.Notifications")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            showError("No project found")
            return
        }

        val editor = e.getData(CommonDataKeys.EDITOR)
        if (editor == null) {
            showError("No editor found")
            return
        }

        val selectedText = editor.selectionModel.selectedText
        if (selectedText.isNullOrBlank()) {
            showError("No text selected")
            return
        }

        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val language = psiFile?.language?.id?.lowercase() ?: ""

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Perplexity")
        if (toolWindow == null) {
            showError("Perplexity tool window not found. Please open it from View > Tool Windows > Perplexity")
            return
        }

        toolWindow.show {
            try {
                val panel = PerplexityPanelService.getInstance(project).panel
                if (panel == null) {
                    showError("Perplexity panel not initialized. Please wait and try again.")
                    return@show
                }

                panel.sendCodeToChat(selectedText, language)
                panel.focusBrowser()

                showSuccess("Code sent to Perplexity (${selectedText.lines().size} lines)")
            } catch (ex: Exception) {
                showError("Failed to send code: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val hasSelection = editor?.selectionModel?.hasSelection() == true
        e.presentation.isEnabledAndVisible = hasSelection
    }

    private fun showError(message: String) {
        notificationGroup
            .createNotification("Send to Perplexity Failed", message, NotificationType.ERROR)
            .notify(null)
    }

    private fun showSuccess(message: String) {
        notificationGroup
            .createNotification("Perplexity", message, NotificationType.INFORMATION)
            .notify(null)
    }
}
