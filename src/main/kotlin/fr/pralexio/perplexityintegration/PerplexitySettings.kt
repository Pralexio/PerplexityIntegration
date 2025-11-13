package fr.pralexio.perplexityintegration

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "PerplexitySettings",
    storages = [Storage("perplexity-integration.xml")]
)
class PerplexitySettings : PersistentStateComponent<PerplexitySettings> {
    var sessionToken: String = ""

    override fun getState(): PerplexitySettings = this

    override fun loadState(state: PerplexitySettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): PerplexitySettings = service()
    }
}
