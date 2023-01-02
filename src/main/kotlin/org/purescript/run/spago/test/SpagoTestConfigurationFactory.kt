package org.purescript.run.spago.test

import com.intellij.execution.configurations.*
import com.intellij.openapi.project.Project

class SpagoTestConfigurationFactory: ConfigurationFactory(SpagoTestConfigurationType) {
    override fun getId() = "SpagoTestConfigurationFactory"
    override fun createTemplateConfiguration(project: Project) =
        SpagoTestRunConfiguration(project, this)

    override fun getOptionsClass() = SpagoTestRunConfigurationOptions::class.java
}

