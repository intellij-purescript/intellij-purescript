package org.purescript.psi.declaration.classes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.psi.declaration.ImportableTypeIndex
import org.purescript.psi.declaration.imports.ImportQuickFix

class ClassConstraintReference(classConstraint: PSClassConstraint) :
    LocalQuickFixProvider,
    PsiReferenceBase<PSClassConstraint>(classConstraint, classConstraint.identifier.textRangeInParent, false) {

    override fun getVariants(): Array<ClassDecl> =
        candidates.toTypedArray()

    override fun resolve(): ClassDecl? =
        candidates.firstOrNull { it.name == myElement.name }

    private val candidates: List<ClassDecl>
        get() = myElement.module?.run {
            cache.classes.toList() + cache.imports.flatMap { it.importedClassDeclarations }
        } ?: emptyList()

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val scope = GlobalSearchScope.allScope(element.project)
        val imports = ImportableTypeIndex.get(element.name, element.project, scope)
            .filterIsInstance<ClassDecl>()
            .mapNotNull { it.asImport() }
        return if (imports.isNotEmpty()) {
            arrayOf(ImportQuickFix(*imports.toTypedArray()))
        } else {
            arrayOf()
        }
    }

}
