package fr.pralexio.perplexityintegration

import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.jcef.JBCefApp

internal object PerplexityJcefSupport {

    private val log = Logger.getInstance(PerplexityJcefSupport::class.java)

    const val UNAVAILABLE_TITLE = "Perplexity AI Chat unavailable"

    fun checkSupported() {
        try {
            if (!JBCefApp.isSupported()) {
                log.warn("JBCefApp.isSupported() returned false; attempting browser creation anyway.")
            }
        } catch (e: LinkageError) {
            log.warn("JCEF compatibility check failed", e)
        } catch (e: Exception) {
            log.warn("JCEF support check failed", e)
        }
    }

    fun messageForBrowserFailure(e: LinkageError): String {
        log.warn("JCEF browser creation failed because of an incompatible runtime", e)
        return unavailableMessage(detail(e))
    }

    fun unavailableMessage(detail: String? = null): String {
        val baseMessage = "Perplexity AI Chat is not supported in this IDE/runtime because the embedded JetBrains browser (JCEF) is unavailable or incompatible. " +
                "Please check online whether your IDE, OS, and runtime officially support JCEF. " +
                "You can also check existing GitHub issues or open a new issue with your IDE version, OS details, runtime information, and idea.log."
        return detail
            ?.takeIf { it.isNotBlank() }
            ?.let { "$baseMessage Technical details: $it" }
            ?: baseMessage
    }

    private fun detail(e: Throwable): String =
        e.message?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
}
