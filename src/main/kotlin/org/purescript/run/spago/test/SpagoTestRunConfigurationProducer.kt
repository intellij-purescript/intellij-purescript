package org.purescript.run.spago.test

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.purescript.file.PSFile
import org.purescript.psi.module.Module

class SpagoTestRunConfigurationProducer :
    LazyRunConfigurationProducer<SpagoTestRunConfiguration>() {
    override fun setupConfigurationFromContext(
        configuration: SpagoTestRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val psi = sourceElement.get()
        val module = when (psi) {
            is PSFile.Psi -> psi.module
            else -> psi.parentOfType()
        } ?: return false
        val main = module.exportedValueDeclarations
            .firstOrNull { it.name == "main" } ?: return false
        if (psi != main.nameIdentifier.firstChild && psi !is PSFile.Psi) return false
        configuration.name = "Test ${module.name}"
        configuration.options.moduleName = module.name
        return module.exportedValueDeclarations.any { it.name == "main" }
    }

    override fun isConfigurationFromContext(
        configuration: SpagoTestRunConfiguration,
        context: ConfigurationContext
    ): Boolean =
        context.psiLocation
            ?.parentOfType<PSFile.Psi>(true)
            ?.module
            ?.name == configuration.options.moduleName


    override fun getConfigurationFactory() = SpagoTestConfigurationFactory()
}