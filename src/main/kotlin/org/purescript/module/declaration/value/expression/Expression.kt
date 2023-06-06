package org.purescript.module.declaration.value.expression

import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import org.purescript.module.Module
import org.purescript.module.declaration.value.Similar
import org.purescript.psi.PSPsiFactory
import org.purescript.typechecker.TypeCheckable

interface Expression : PsiElement, Similar, TypeCheckable {

    /**
     * All expressions recursively including this one
     */
    val expressions: Sequence<Expression> get() = 
        sequenceOf(this) + childrenOfType<Expression>().flatMap { it.expressions }
    val module: Module?
    fun getName(): String?

    fun withParenthesis(): PSParens? {
        val factory = project.service<PSPsiFactory>()
        return factory.createParenthesis(text)
    }
}