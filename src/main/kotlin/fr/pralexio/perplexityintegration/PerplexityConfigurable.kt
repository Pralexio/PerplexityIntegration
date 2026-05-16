package fr.pralexio.perplexityintegration

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import javax.swing.event.ChangeListener

class PerplexityConfigurable : Configurable {

    private val settings = PerplexitySettings.getInstance()
    private val credentials = PerplexityCredentialStore.getInstance()
    private var tokenField: JBPasswordField? = null
    private var gpuCheckbox: JBCheckBox? = null
    private var expirationLabel: JBLabel? = null
    private var scrollSpeedSlider: JSlider? = null
    private var scrollSpeedLabel: JBLabel? = null
    private var modified = false

    private fun sliderToFactor(v: Int): Double = v / 10.0
    private fun factorToSlider(f: Double): Int = (f * 10).toInt().coerceIn(10, 80)

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

    private val notificationGroup by lazy {
        NotificationGroupManager.getInstance().getNotificationGroup("Perplexity.Notifications")
    }

    override fun getDisplayName(): String = "Perplexity Integration"

    override fun createComponent(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        mainPanel.border = JBUI.Borders.empty(10)

        tokenField = JBPasswordField()
        tokenField?.text = credentials.getToken()
        tokenField?.preferredSize = Dimension(500, 30)
        tokenField?.document?.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) { modified = true; updateExpirationLabel() }
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) { modified = true; updateExpirationLabel() }
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) { modified = true; updateExpirationLabel() }
        })

        val showTokenButton = JButton("Show")
        showTokenButton.addActionListener {
            tokenField?.echoChar = if (tokenField?.echoChar == 0.toChar()) '•' else 0.toChar()
            showTokenButton.text = if (tokenField?.echoChar == 0.toChar()) "Hide" else "Show"
        }

        val clearTokenButton = JButton("Clear Token")
        clearTokenButton.addActionListener {
            val result = Messages.showYesNoDialog(
                "Are you sure you want to clear the saved token?",
                "Clear Token",
                Messages.getQuestionIcon()
            )
            if (result == Messages.YES) {
                tokenField?.text = ""
                modified = true
                updateExpirationLabel()
            }
        }

        val helpButton = JButton("How to Get Token")
        helpButton.addActionListener {
            TokenInstructionsDialog().show()
        }

        val tokenButtonPanel = JPanel()
        tokenButtonPanel.layout = BoxLayout(tokenButtonPanel, BoxLayout.X_AXIS)
        tokenButtonPanel.add(showTokenButton)
        tokenButtonPanel.add(Box.createHorizontalStrut(5))
        tokenButtonPanel.add(clearTokenButton)
        tokenButtonPanel.add(Box.createHorizontalStrut(5))
        tokenButtonPanel.add(helpButton)

        expirationLabel = JBLabel()
        updateExpirationLabel()

        gpuCheckbox = JBCheckBox("Enable GPU Acceleration (requires IDE restart)")
        gpuCheckbox?.isSelected = settings.gpuEnabled
        gpuCheckbox?.addActionListener { modified = true }

        val resetPrivacyButton = JButton("Reset privacy warning")
        resetPrivacyButton.toolTipText = "Show the \"send to Perplexity\" confirmation dialog again on next use"
        resetPrivacyButton.addActionListener {
            settings.firstSendConfirmed = false
            Messages.showInfoMessage(
                "The send confirmation will be shown again on your next Send to Perplexity action.",
                "Privacy Warning Reset"
            )
        }

        scrollSpeedLabel = JBLabel(formatScrollLabel(settings.scrollSpeedMultiplier))
        scrollSpeedSlider = JSlider(10, 80, factorToSlider(settings.scrollSpeedMultiplier)).apply {
            majorTickSpacing = 10
            minorTickSpacing = 5
            paintTicks = true
            toolTipText = "Workaround for slow JCEF scrolling. 1.0x = native CEF speed, 8.0x = very fast."
            addChangeListener(ChangeListener {
                val v = sliderToFactor(value)
                scrollSpeedLabel?.text = formatScrollLabel(v)
                modified = true
            })
        }
        val scrollHelp = JBLabel("Browser scroll speed (workaround for slow JCEF off-screen rendering)")
        scrollHelp.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND

        val instructionsText = JTextArea()
        instructionsText.text = """
    Login Methods:
    
    PRIMARY METHOD (Recommended):
    Login directly in the Perplexity browser panel using your email.
    (Even if you created your account with Google on Perplexity, they send a code to login with your email)
    
    ALTERNATIVE METHOD:
    If you have login issues, manually set your session token below.
    Click "How to Get Token" for detailed instructions.
    
    Note: Session tokens typically expire after 30 days and you need to reload after pasting your token.
""".trimIndent()
        instructionsText.isEditable = false
        instructionsText.background = mainPanel.background
        instructionsText.border = JBUI.Borders.empty(10, 0)
        instructionsText.lineWrap = true
        instructionsText.wrapStyleWord = true


        val formPanel = FormBuilder.createFormBuilder()
            .addComponent(instructionsText)
            .addVerticalGap(10)
            .addLabeledComponent(JBLabel("Session Token:"), tokenField!!, 1, false)
            .addComponent(tokenButtonPanel)
            .addComponent(expirationLabel!!)
            .addVerticalGap(20)
            .addSeparator()
            .addVerticalGap(10)
            .addComponent(scrollHelp)
            .addComponent(scrollSpeedLabel!!)
            .addComponent(scrollSpeedSlider!!)
            .addVerticalGap(10)
            .addComponent(gpuCheckbox!!)
            .addVerticalGap(10)
            .addComponent(resetPrivacyButton)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        mainPanel.add(formPanel, BorderLayout.CENTER)

        return mainPanel
    }

    private fun updateExpirationLabel() {
        expirationLabel?.let { label ->
            val token = String(tokenField?.password ?: charArrayOf())
            if (token.isNotEmpty()) {
                val expirationDate = Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
                label.text = "Token expires approximately: ${dateFormat.format(expirationDate)}"
                label.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
            } else {
                label.text = "No token set"
                label.foreground = JBUI.CurrentTheme.Label.disabledForeground()
            }
        }
    }

    override fun isModified(): Boolean {
        val currentToken = String(tokenField?.password ?: charArrayOf())
        val sliderFactor = scrollSpeedSlider?.let { sliderToFactor(it.value) } ?: settings.scrollSpeedMultiplier
        return modified ||
                currentToken != credentials.getToken() ||
                gpuCheckbox?.isSelected != settings.gpuEnabled ||
                sliderFactor != settings.scrollSpeedMultiplier
    }

    override fun apply() {
        val newToken = String(tokenField?.password ?: charArrayOf()).trim()
        val tokenChanged = newToken != credentials.getToken()
        val gpuChanged = gpuCheckbox?.isSelected != settings.gpuEnabled
        val newScrollFactor = scrollSpeedSlider?.let { sliderToFactor(it.value) } ?: settings.scrollSpeedMultiplier
        val scrollChanged = newScrollFactor != settings.scrollSpeedMultiplier

        if (newToken.isNotEmpty() && newToken.length < 20) {
            notificationGroup
                .createNotification(
                    "Invalid Token",
                    "The token appears to be too short. Please copy the full token value.",
                    NotificationType.WARNING
                )
                .notify(null)
            return
        }

        credentials.setToken(newToken)
        settings.gpuEnabled = gpuCheckbox?.isSelected ?: false
        settings.scrollSpeedMultiplier = newScrollFactor

        if (scrollChanged) {
            // Push live to any open Perplexity panel without requiring reload.
            ProjectManager.getInstance().openProjects.forEach { project ->
                PerplexityPanelService.getInstance(project).panel?.applyScrollSpeed()
            }
        }

        modified = false

        if (tokenChanged && newToken.isNotEmpty()) {
            notificationGroup
                .createNotification(
                    "Token Saved",
                    "Please reload the Perplexity panel for changes to take effect.",
                    NotificationType.INFORMATION
                )
                .notify(null)
        } else if (tokenChanged && newToken.isEmpty()) {
            notificationGroup
                .createNotification(
                    "Token Cleared",
                    "Session token has been removed.",
                    NotificationType.INFORMATION
                )
                .notify(null)
        }

        if (gpuChanged) {
            notificationGroup
                .createNotification(
                    "GPU Settings Changed",
                    "Please restart the IDE for GPU settings to take effect.",
                    NotificationType.INFORMATION
                )
                .notify(null)
        }
    }

    override fun reset() {
        tokenField?.text = credentials.getToken()
        gpuCheckbox?.isSelected = settings.gpuEnabled
        scrollSpeedSlider?.value = factorToSlider(settings.scrollSpeedMultiplier)
        scrollSpeedLabel?.text = formatScrollLabel(settings.scrollSpeedMultiplier)
        modified = false
        updateExpirationLabel()
    }

    private fun formatScrollLabel(factor: Double): String =
        "Scroll speed multiplier: ${"%.1f".format(Locale.ENGLISH, factor)}x"
}

class TokenInstructionsDialog : DialogWrapper(null) {

    init {
        title = "How to Get Your Session Token"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        mainPanel.preferredSize = Dimension(700, 500)

        val tabbedPane = JBTabbedPane()

        // Chrome Tab
        tabbedPane.addTab("Google Chrome", createBrowserInstructions(
            browserName = "Google Chrome",
            steps = listOf(
                "Open https://www.perplexity.ai in Chrome",
                "Sign in to your account",
                "Press F12 to open DevTools",
                "Click the \"Application\" tab",
                "In the left sidebar, expand \"Cookies\"",
                "Click on \"https://www.perplexity.ai\"",
                "Find \"__Secure-next-auth.session-token\" in the list",
                "Double-click the Value column and copy the entire token",
                "Paste it in the Session Token field in settings"
            ),
            icon = "🌐"
        ))

        // Firefox Tab
        tabbedPane.addTab("Firefox", createBrowserInstructions(
            browserName = "Firefox",
            steps = listOf(
                "Open https://www.perplexity.ai in Firefox",
                "Sign in to your account",
                "Press F12 to open DevTools",
                "Click the \"Storage\" tab",
                "In the left sidebar, expand \"Cookies\"",
                "Click on \"https://www.perplexity.ai\"",
                "Find \"__Secure-next-auth.session-token\" in the list",
                "Right-click the Value and select \"Copy\"",
                "Paste it in the Session Token field in settings"
            ),
            icon = "🦊"
        ))

        mainPanel.add(tabbedPane, BorderLayout.CENTER)

        // Bottom warning panel
        val warningPanel = createWarningPanel()
        mainPanel.add(warningPanel, BorderLayout.SOUTH)

        return mainPanel
    }

    private fun createBrowserInstructions(browserName: String, steps: List<String>, icon: String): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(15)

        // Header
        val headerPanel = JPanel()
        headerPanel.layout = BoxLayout(headerPanel, BoxLayout.Y_AXIS)

        val titleLabel = JLabel("$icon  $browserName Instructions")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 16f)
        titleLabel.alignmentX = JComponent.LEFT_ALIGNMENT

        val subtitleLabel = JLabel("Follow these steps to extract your session token:")
        subtitleLabel.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        subtitleLabel.border = JBUI.Borders.emptyTop(5)
        subtitleLabel.alignmentX = JComponent.LEFT_ALIGNMENT

        headerPanel.add(titleLabel)
        headerPanel.add(subtitleLabel)
        headerPanel.add(Box.createVerticalStrut(15))

        // Steps
        val stepsPanel = JPanel()
        stepsPanel.layout = BoxLayout(stepsPanel, BoxLayout.Y_AXIS)

        steps.forEachIndexed { index, step ->
            val stepPanel = createStepPanel(index + 1, step)
            stepsPanel.add(stepPanel)
            if (index < steps.size - 1) {
                stepsPanel.add(Box.createVerticalStrut(10))
            }
        }

        val scrollPane = JScrollPane(stepsPanel)
        scrollPane.border = null
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER

        panel.add(headerPanel, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    private fun createStepPanel(stepNumber: Int, stepText: String): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(5, 0)

        val numberLabel = JLabel("$stepNumber")
        numberLabel.horizontalAlignment = SwingConstants.CENTER
        numberLabel.preferredSize = Dimension(30, 30)
        numberLabel.font = numberLabel.font.deriveFont(Font.BOLD)
        numberLabel.foreground = UIUtil.getLabelForeground()
        numberLabel.border = JBUI.Borders.empty(5)

        val textArea = JTextArea(stepText)
        textArea.isEditable = false
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        textArea.background = panel.background
        textArea.font = UIUtil.getLabelFont()
        textArea.border = JBUI.Borders.emptyLeft(10)

        panel.add(numberLabel, BorderLayout.WEST)
        panel.add(textArea, BorderLayout.CENTER)

        return panel
    }

    private fun createWarningPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = JBUI.Borders.empty(15, 15, 10, 15)
        panel.background = JBUI.CurrentTheme.Banner.WARNING_BACKGROUND

        val titleLabel = JLabel("⚠️  Important Security Notes")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD)
        titleLabel.alignmentX = JComponent.LEFT_ALIGNMENT

        val notes = listOf(
            "• The token is a long string (usually 100+ characters)",
            "• Tokens expire after approximately 30 days",
            "• Keep your token private - it gives access to your account",
            "• Never share your token with others"
        )

        val notesPanel = JPanel()
        notesPanel.layout = BoxLayout(notesPanel, BoxLayout.Y_AXIS)
        notesPanel.alignmentX = JComponent.LEFT_ALIGNMENT
        notesPanel.background = panel.background
        notesPanel.border = JBUI.Borders.emptyTop(8)

        notes.forEach { note ->
            val noteLabel = JLabel(note)
            noteLabel.alignmentX = JComponent.LEFT_ALIGNMENT
            noteLabel.border = JBUI.Borders.emptyTop(3)
            notesPanel.add(noteLabel)
        }

        panel.add(titleLabel)
        panel.add(notesPanel)

        return panel
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction)
    }
}
