package fr.pralexio.perplexityintegration

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager

abstract class PerplexitySendActionBase(
    text: String,
    description: String,
    private val instruction: String
) : AnAction(text, description, null) {

    private val notificationGroup by lazy {
        NotificationGroupManager.getInstance().getNotificationGroup("Perplexity.Notifications")
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = editor?.selectionModel?.hasSelection() == true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            showError(null, "No project found")
            return
        }

        val editor = e.getData(CommonDataKeys.EDITOR)
        if (editor == null) {
            showError(project, "No editor found")
            return
        }

        val selectedText = editor.selectionModel.selectedText
        if (selectedText.isNullOrBlank()) {
            showError(project, "No text selected")
            return
        }

        if (!PerplexityPrivacyConfirmation.ensureConfirmed(project)) return

        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val language = psiFile?.language?.id?.lowercase() ?: ""

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Perplexity")
        if (toolWindow == null) {
            showError(project, "Perplexity tool window not found. Open View > Tool Windows > Perplexity first.")
            return
        }

        toolWindow.show {
            try {
                val panel = PerplexityPanelService.getInstance(project).panel
                if (panel == null) {
                    showError(project, "Perplexity panel not initialized. Please wait a moment and try again.")
                    return@show
                }

                panel.sendCodeToChat(selectedText, language, instruction)
                panel.focusBrowser()

                val title = templatePresentation.text ?: "Perplexity"
                showSuccess(project, "$title: ${selectedText.lines().size} lines sent")
            } catch (ex: Exception) {
                showError(project, "Failed to send code: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    private fun showError(project: Project?, message: String) {
        notificationGroup
            .createNotification("Send to Perplexity Failed", message, NotificationType.ERROR)
            .notify(project)
    }

    private fun showSuccess(project: Project?, message: String) {
        notificationGroup
            .createNotification("Perplexity", message, NotificationType.INFORMATION)
            .notify(project)
    }
}

class PerplexityExplainAction : PerplexitySendActionBase(
    text = "Explain Selection",
    description = "Ask Perplexity to explain the selected code",
    instruction = "Please explain in detail what the following code does, how it works, and any non-obvious behavior."
)

class PerplexityFindBugsAction : PerplexitySendActionBase(
    text = "Find Bugs",
    description = "Ask Perplexity to look for bugs and edge cases",
    instruction = "Please carefully review the following code and list any bugs, race conditions, edge cases, or potential issues."
)

class PerplexityOptimizeAction : PerplexitySendActionBase(
    text = "Optimize",
    description = "Ask Perplexity to suggest optimizations",
    instruction = "Please suggest performance and readability optimizations for the following code, explaining the trade-offs."
)

class PerplexityWriteTestsAction : PerplexitySendActionBase(
    text = "Write Tests",
    description = "Ask Perplexity to generate unit tests",
    instruction = "Please write thorough unit tests for the following code. Cover the happy path and the meaningful edge cases."
)

class PerplexityRefactorAction : PerplexitySendActionBase(
    text = "Refactor",
    description = "Ask Perplexity to refactor the selection",
    instruction = "Please refactor the following code to improve readability and maintainability while preserving the exact behavior."
)

class PerplexityAddCommentsAction : PerplexitySendActionBase(
    text = "Add Comments",
    description = "Ask Perplexity to add explanatory comments",
    instruction = "Please add clear, concise comments to the following code, explaining only the non-obvious parts."
)

object PerplexityPrivacyConfirmation {

    private const val TITLE = "Send Code to Perplexity?"
    private val MESSAGE = """
        The selected code will be sent to perplexity.ai for analysis.

        Make sure the selection does not contain secrets, credentials, or private data.

        You can reset this confirmation from Settings → Tools → Perplexity AI.
    """.trimIndent()

    fun ensureConfirmed(project: Project): Boolean {
        val settings = PerplexitySettings.getInstance()
        if (settings.firstSendConfirmed) return true

        val confirmed = MessageDialogBuilder
            .yesNo(TITLE, MESSAGE)
            .icon(Messages.getWarningIcon())
            .yesText("Send")
            .noText("Cancel")
            .ask(project)

        if (confirmed) {
            settings.firstSendConfirmed = true
        }
        return confirmed
    }
}
