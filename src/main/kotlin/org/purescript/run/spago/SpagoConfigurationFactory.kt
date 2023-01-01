package org.purescript.run.spago

import com.intellij.execution.configurations.*
import com.intellij.openapi.project.Project

class SpagoConfigurationFactory: ConfigurationFactory(SpagoConfigurationType) {
    override fun getId() = "SpagoConfigurationFactory"
    override fun createTemplateConfiguration(project: Project) =
        SpagoRunConfiguration(project, this)

    override fun getOptionsClass() = SpagoRunConfigurationOptions::class.java
}

