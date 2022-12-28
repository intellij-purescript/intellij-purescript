package org.purescript.ide.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import org.purescript.psi.module.Module.*
import org.purescript.psi.binder.PSVarBinder
import org.purescript.psi.declaration.PSFixityDeclaration
import org.purescript.psi.declaration.PSValueDeclaration

class PSRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean =
        element is Psi ||
            element is PSValueDeclaration ||
            element is PSFixityDeclaration ||
            element is PSVarBinder
}
