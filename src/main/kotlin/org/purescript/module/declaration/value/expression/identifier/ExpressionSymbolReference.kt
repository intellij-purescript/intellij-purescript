package org.purescript.module.declaration.value.expression.identifier

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.fixity.ConstructorFixityDeclaration
import org.purescript.module.declaration.fixity.FixityDeclaration
import org.purescript.module.declaration.fixity.ValueFixityDeclaration
import org.purescript.module.declaration.imports.Import
import org.purescript.module.declaration.imports.ImportQuickFix
import org.purescript.name.PSModuleName
import org.purescript.name.PSOperatorName
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSPsiFactory

class ExpressionSymbolReference(symbol: PSPsiElement, val qualifier: PSModuleName?, val operator: PSOperatorName) :
    LocalQuickFixProvider, PsiReferenceBase<PSPsiElement>(symbol, operator.textRangeInParent, false) {

    override fun getVariants() = candidates.toList().toTypedArray()

    override fun resolve(): FixityDeclaration? {
        val name = element.name ?: return null
        return candidates(name).firstOrNull { it.name == name }
    }

    override fun isReferenceTo(element: PsiElement) = when (element) {
        is ValueFixityDeclaration -> element.name == this.element.name
        is ConstructorFixityDeclaration -> element.name == this.element.name
        else -> false
    } && super.isReferenceTo(element)

    val localCandidates: Sequence<FixityDeclaration>
        get() = sequence {
            val module = element.module
            yieldAll(module.valueFixityDeclarations.asSequence())
            yieldAll(module.constructorFixityDeclarations.asSequence())
        }
    val candidates
        get() = sequence {
            yieldAll(localCandidates)
            val module = element.module
            val imports: List<Import> = module.cache.importsByAlias[qualifier?.name] ?: return@sequence
            yieldAll(imports.flatMap { it.importedValueFixityDeclarations })
            yieldAll(imports.flatMap { it.importedConstructorFixityDeclarations })
        }

    fun candidates(name: String): Sequence<FixityDeclaration> = sequence {
        yieldAll(localCandidates)
        val module = element.module
        val imports: List<Import> = module.cache.importsByAlias[qualifier?.name] ?: return@sequence
        yieldAll(imports.flatMap { it.importedFixityDeclarations(name) })
        yieldAll(imports.flatMap { it.importedConstructorFixityDeclarations(name) })
    }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val qualifyingName = qualifier?.name
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
        val newName = PSPsiFactory(element.project).createOperatorName(name) ?: return null
        operator.replace(newName)
        return element
    }
}
