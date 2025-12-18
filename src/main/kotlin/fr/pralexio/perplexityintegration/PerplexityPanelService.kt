package fr.pralexio.perplexityintegration

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class PerplexityPanelService {
    
    @Volatile
    var panel: PerplexityPanel? = null
    
    companion object {
        fun getInstance(project: Project): PerplexityPanelService = project.service()
    }
}
