package org.purescript.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.purescript.file.PSFile
import org.purescript.psi.declaration.PSValueDeclaration
import org.purescript.psi.module.Module

class SpagoRunConfigurationProducer :
    LazyRunConfigurationProducer<SpagoRunConfiguration>() {
    override fun setupConfigurationFromContext(
        configuration: SpagoRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val psi = sourceElement.get()
        val module = when (psi) {
            is PSFile.Psi -> psi.module
            is PSValueDeclaration -> psi.parentOfType()
            else -> null
        } ?: return false
        configuration.name = module.name
        configuration.options.moduleName = module.name
        return module.exportedValueDeclarations.any { it.name == "main" }
    }

    override fun isConfigurationFromContext(
        configuration: SpagoRunConfiguration,
        context: ConfigurationContext
    ): Boolean = when (val psi = context.psiLocation) {
        is PSFile.Psi -> psi.module?.name == configuration.options.moduleName
        is PSValueDeclaration ->
            psi.parentOfType<Module.Psi>()?.name == configuration.options.moduleName

        else -> false
    }

    override fun getConfigurationFactory() = SpagoConfigurationFactory()
}