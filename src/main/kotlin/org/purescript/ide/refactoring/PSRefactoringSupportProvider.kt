package org.purescript.ide.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import org.purescript.psi.module.Module
import org.purescript.psi.binder.PSVarBinder
import org.purescript.psi.declaration.fixity.FixityDeclaration
import org.purescript.psi.declaration.value.ValueDecl

class PSRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean =
        element is Module.Psi ||
            element is ValueDecl ||
            element is FixityDeclaration.Psi ||
            element is PSVarBinder

    override fun isSafeDeleteAvailable(element: PsiElement): Boolean {
        return element is ValueDecl
    }
}
