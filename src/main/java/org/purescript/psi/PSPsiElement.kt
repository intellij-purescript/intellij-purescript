package org.purescript.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import org.purescript.file.ImportedModuleIndex
import org.purescript.file.PSFile

abstract class PSPsiElement(node: ASTNode) : ASTWrapperPsiElement(node) {

    /**
     * @return the [PSModule] containing this element
     */
    val module: PSModule? get() = (containingFile as? PSFile)?.module

    override fun getUseScope(): SearchScope = module
        ?.name
        ?.let {
            val filesImportingModule =
                ImportedModuleIndex.filesImportingModule(project, it)
            GlobalSearchScope.filesScope(project, filesImportingModule)
                .union(LocalSearchScope(containingFile))
        }
        ?: super.getUseScope()
}
