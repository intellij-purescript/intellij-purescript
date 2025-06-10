package org.purescript.module.declaration.value.binder

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.fixity.ConstructorFixityDeclaration
import org.purescript.module.declaration.imports.ImportQuickFix
import org.purescript.name.PSModuleName
import org.purescript.name.PSOperatorName
import org.purescript.psi.PSPsiFactory

class BinderOperatorReference(symbol: BinderOperator, val moduleName: PSModuleName?, val operator: PSOperatorName) :
    LocalQuickFixProvider, PsiReferenceBase<BinderOperator>(symbol, operator.textRangeInParent, false) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): ConstructorFixityDeclaration? {
        val name = element.name
        return candidates(name).firstOrNull { it.name == name }
    }

    override fun isReferenceTo(element: PsiElement) = when (element) {
        is ConstructorFixityDeclaration -> element.name == this.element.name
        else -> false
    } && super.isReferenceTo(element)

    val candidates
        get() = sequence {
            val module = element.module
            yieldAll(module.constructorFixityDeclarations.asSequence())
            yieldAll(module.cache.imports.flatMap { it.importedConstructorFixityDeclarations })
        }

    fun candidates(name: String) = sequence {
        val module = element.module
        yieldAll(module.constructorFixityDeclarations.asSequence())
        yieldAll(module.cache.imports.flatMap { it.importedConstructorFixityDeclarations(name) })
    }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val qualifyingName = moduleName?.name
        val scope = GlobalSearchScope.allScope(element.project)
        val imports = StubIndex.getElements(
            ImportableIndex.key,
            element.name,
            element.project,
            scope,
            Importable::class.java
        )
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
