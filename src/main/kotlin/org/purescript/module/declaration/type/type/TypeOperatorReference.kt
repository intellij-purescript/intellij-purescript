package org.purescript.module.declaration.type.type

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.fixity.TypeFixityDeclaration
import org.purescript.module.declaration.imports.ImportQuickFix
import org.purescript.name.PSModuleName
import org.purescript.name.PSOperatorName
import org.purescript.psi.PSPsiFactory

class TypeOperatorReference(symbol: TypeOperator, val moduleName: PSModuleName?, val operator: PSOperatorName) :
    LocalQuickFixProvider, PsiReferenceBase<TypeOperator>(symbol, operator.textRangeInParent, false) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): TypeFixityDeclaration? {
        val name = element.name
        return candidates(name).firstOrNull { it.name == name }
    }

    override fun isReferenceTo(element: PsiElement) = when (element) {
        is TypeFixityDeclaration -> element.name == this.element.name
        else -> false
    } && super.isReferenceTo(element)

    val candidates
        get() = sequence {
            val module = element.module
            yieldAll(module.typeFixityDeclarations.asSequence())
            yieldAll(module.cache.imports.flatMap { it.importedTypeFixityDeclarations })
        }

    fun candidates(name: String) = sequence {
        val module = element.module
        yieldAll(module.typeFixityDeclarations.asSequence())
        yieldAll(module.cache.imports.flatMap { it.importedTypeFixityDeclarations(name) })
    }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val qualifyingName = moduleName?.name
        val scope = GlobalSearchScope.allScope(element.project)
        val imports = ImportableIndex
            .get(element.name, element.project, scope)
            .filter { it.isValid }
            .mapNotNull { it.asImport()?.withAlias(qualifyingName) }
            .toTypedArray()
        return if (imports.isNotEmpty()) {
            arrayOf(ImportQuickFix(*imports))
        } else {
            arrayOf()
        }
    }

    override fun handleElementRename(name: String): PsiElement? {
        val newName = PSPsiFactory(element.project).createOperatorName(name) ?: return null
        operator.replace(newName)
        return element
    }
}
