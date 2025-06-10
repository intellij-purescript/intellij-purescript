package org.purescript.module.declaration.classes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.ImportableTypeIndex
import org.purescript.module.declaration.imports.ImportQuickFix

class ClassConstraintReference(classConstraint: PSClassConstraint) :
    LocalQuickFixProvider,
    PsiReferenceBase<PSClassConstraint>(classConstraint, classConstraint.identifier.textRangeInParent, false) {

    override fun getVariants(): Array<ClassDecl> =
        candidates.toTypedArray()

    override fun resolve(): ClassDecl? =
        candidates.firstOrNull { it.name == myElement.name }

    private val candidates: List<ClassDecl>
        get() = myElement.module.run { classes.toList() + cache.imports.flatMap { it.importedClassDeclarations } }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val scope = GlobalSearchScope.allScope(element.project)
        val imports = StubIndex.getElements(
            ImportableTypeIndex.key,
            element.name,
            element.project,
            scope,
            Importable::class.java
        ).filterIsInstance<ClassDecl>().map { it.asImport() }
        return if (imports.isNotEmpty()) {
            arrayOf(ImportQuickFix(*imports.toTypedArray()))
        } else {
            arrayOf()
        }
    }

}
