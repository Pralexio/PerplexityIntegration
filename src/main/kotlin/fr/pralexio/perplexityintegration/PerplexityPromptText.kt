package fr.pralexio.perplexityintegration

internal object PerplexityPromptText {

    fun build(code: String, language: String = "", instruction: String = ""): String {
        val langInfo = if (language.isNotEmpty()) " ($language)" else ""
        val preamble = if (instruction.isNotEmpty()) {
            "$instruction\n\nHere is the code$langInfo:"
        } else {
            "Here is my code$langInfo:"
        }
        return "$preamble\n\n$code"
    }
}
