package org.purescript.run.spago

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.purescript.file.PSFile

class SpagoRunConfigurationProducer :
    LazyRunConfigurationProducer<SpagoRunConfiguration>() {
    override fun setupConfigurationFromContext(
        configuration: SpagoRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val psi = sourceElement.get()
        val module = when (psi) {
            is PSFile -> psi.module
            else -> psi.parentOfType()
        } ?: return false
        val main = module.exportedValueDeclarationGroups
            .firstOrNull { it.name == "main" } ?: return false
        if (psi != main.nameIdentifier?.firstChild && psi !is PSFile) return false
        configuration.name = module.name
        configuration.options.moduleName = module.name
        return module.exportedValueDeclarationGroups.any { it.name == "main" }
    }

    override fun isConfigurationFromContext(
        configuration: SpagoRunConfiguration,
        context: ConfigurationContext
    ): Boolean =
        context.psiLocation
            ?.parentOfType<PSFile>(true)
            ?.module
            ?.name == configuration.options.moduleName


    override fun getConfigurationFactory() = SpagoConfigurationFactory()
}