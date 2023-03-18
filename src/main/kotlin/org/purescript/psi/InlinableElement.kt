package org.purescript.psi

import com.intellij.psi.PsiElement
import org.purescript.module.declaration.value.expression.Expression

interface InlinableElement: PsiElement, UsedElement {

    /**
     * Returns an expression that can be used instead of its reference
     */
    fun inline(arguments: List<Expression>): Expression

    /**
     * Checks that the psi element can be inlined, for value declaration 
     * groups this can be to check that it only have one value declaration
     */
    fun canBeInlined(): Boolean

    /**
     * deletes the psi and any parents that no longer are valid
     */
    fun deleteAfterInline()
}