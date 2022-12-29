package org.purescript.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.purescript.file.PSFile
import org.purescript.psi.module.Module
import org.purescript.psi.declaration.PSValueDeclaration

class SpagoRunConfigurationProducer :
    LazyRunConfigurationProducer<SpagoRunConfiguration>() {
    override fun setupConfigurationFromContext(
        configuration: SpagoRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        configuration.name = "Main"
        return when (val psi = sourceElement.get()) {
            is PSFile.Psi -> psi.module?.name == "Main"
            is PSValueDeclaration -> psi.name == "main" &&
                psi.parentOfType<Module.Psi>()?.name == "Main"
            else -> psi.parentOfType<PSValueDeclaration>()?.name == "main" &&
                psi.parentOfType<Module.Psi>()?.name == "Main"
        }
    }

    override fun isConfigurationFromContext(
        configuration: SpagoRunConfiguration,
        context: ConfigurationContext
    ): Boolean = true

    override fun getConfigurationFactory() = SpagoConfigurationFactory()
}