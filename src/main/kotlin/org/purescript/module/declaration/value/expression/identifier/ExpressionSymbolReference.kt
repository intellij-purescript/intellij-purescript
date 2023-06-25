package org.purescript.module.declaration.value.expression.identifier

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.fixity.FixityDeclaration
import org.purescript.module.declaration.imports.ImportQuickFix
import org.purescript.name.PSModuleName
import org.purescript.name.PSOperatorName
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSPsiFactory

class ExpressionSymbolReference(symbol: PSPsiElement, val moduleName: PSModuleName?, val operator: PSOperatorName) :
    LocalQuickFixProvider, PsiReferenceBase<PSPsiElement>(symbol, operator.textRangeInParent, false) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): FixityDeclaration? {
        val name = element.name ?: return null
        return candidates(name).firstOrNull { it.name == name }
    }

    override fun isReferenceTo(element: PsiElement) = when (element) {
        is FixityDeclaration ->
            if (element.name == this.element.name) super.isReferenceTo(element)
            else false
        else -> false
    }

    val candidates
        get() = sequence {
            val module = element.module
            yieldAll(module.fixityDeclarations.asSequence())
            yieldAll(module.cache.imports.flatMap { it.importedFixityDeclarations })
        }

    fun candidates(name: String) = sequence {
        val module = element.module
        yieldAll(module.fixityDeclarations.asSequence())
        yieldAll(module.cache.imports.flatMap { it.importedFixityDeclarations(name) })
    }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val qualifyingName = moduleName?.name
        val scope = GlobalSearchScope.allScope(element.project)
        val imports = ImportableIndex
            .get(element.name!!, element.project, scope)
            .filter { it.isValid }
            .map { it.asImport()?.withAlias(qualifyingName) }
            .filterNotNull().toTypedArray()
        return if (imports.isNotEmpty()) {
            arrayOf(ImportQuickFix(*imports))
        } else {
            arrayOf()
        }
    }

    override fun handleElementRename(name: String): PsiElement? {
        val newName = PSPsiFactory(element.project).createOperatorName(name)
            ?: return null
        operator.replace(newName)
        return element
    }
}
