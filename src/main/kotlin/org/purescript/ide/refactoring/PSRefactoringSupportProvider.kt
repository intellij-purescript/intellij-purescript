package org.purescript.ide.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.RefactoringActionHandler
import org.purescript.module.Module
import org.purescript.module.declaration.fixity.FixityDeclaration
import org.purescript.module.declaration.imports.PSImportedDataMember
import org.purescript.module.declaration.imports.PSImportedItem
import org.purescript.module.declaration.type.PSTypeVarName
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.VarBinder
import org.purescript.module.declaration.value.expression.Expression

class PSRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean =
        element is Module ||
                element is ValueDeclarationGroup ||
                element is FixityDeclaration ||
                element is VarBinder ||
                element is PSTypeVarName

    override fun isSafeDeleteAvailable(element: PsiElement): Boolean =
        element is ValueDeclarationGroup ||
                element is PSImportedItem ||
                element is PSImportedDataMember 

    override fun isInplaceIntroduceAvailable(element: PsiElement, context: PsiElement?) =
        element is Expression

    /**
     * The closest to a variable is let .. in
     *
     * `foo x = x + 2 + x + 2`
     *
     * can become
     *
     * `foo x = let y = x + 2 in y + y`
     *
     * or
     *
     * ```
     * foo x =
     *   let
     *     y = x + 2
     *   in
     *     y + y
     * ```
     *
     *
     */
    override fun getExtractMethodHandler(): RefactoringActionHandler {
        return ValueGroupIntroducer()
    }
}

