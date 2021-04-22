package org.purescript.ide.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import org.purescript.psi.PSModule
import org.purescript.psi.declaration.PSValueDeclaration

class PSRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        return element is PSModule || element is PSValueDeclaration
    }
}
