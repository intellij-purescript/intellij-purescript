package org.purescript.ide.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.RefactoringActionHandler
import org.purescript.psi.binder.PSVarBinder
import org.purescript.psi.declaration.fixity.FixityDeclaration
import org.purescript.psi.declaration.imports.PSImportedDataMember
import org.purescript.psi.declaration.imports.PSImportedItem
import org.purescript.psi.declaration.value.ValueDeclarationGroup
import org.purescript.psi.expression.Expression
import org.purescript.psi.module.Module

class PSRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean =
        element is Module ||
                element is ValueDeclarationGroup ||
                element is FixityDeclaration ||
                element is PSVarBinder

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
    override fun getExtractMethodHandler(): RefactoringActionHandler? {
        return ExpressionIdentifierIntroduceHandler()
    }
}

