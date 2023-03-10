package org.purescript.module.declaration.value.expression

import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import org.purescript.module.Module
import org.purescript.module.declaration.classes.PSClassMemberList
import org.purescript.module.declaration.value.expression.identifier.PSExpressionIdentifier
import org.purescript.psi.PSPsiFactory

interface Expression : PsiElement {
    /**
     * All expressions atoms recursively including this one if it is one
     */
    fun getAtoms(): Sequence<ExpressionAtom> =
        expressions.filterIsInstance<ExpressionAtom>()

    /**
     * All expressions recursively including this one
     */
    val expressions: Sequence<Expression> get() = sequenceOf(this) + childrenOfType<Expression>()
        .flatMap { it.expressions }
    val module: Module?
    fun getName(): String?
    val dependencies: Sequence<PSExpressionIdentifier>
        get() = getAtoms().filterIsInstance<PSExpressionIdentifier>().filter {
            val reference = it.reference.resolve() ?: return@filter true
            if (textRange.contains(reference.textRange)) false
            else when (reference.parent) {
                is Module, is PSClassMemberList -> false
                else -> true
            }
        }

    fun areSimilarTo(other: Expression): Boolean {
        if (this.javaClass != other.javaClass) return false
        if (children.size != other.children.size) return false
        return childrenOfType<Expression>()
            .zip(other.childrenOfType<Expression>()) { a, b -> a.areSimilarTo(b) }
                .all { it }
    }

    fun withParenthesis(): PSParens? {
        val factory = project.service<PSPsiFactory>()
        return factory.createParenthesis(text)
    }
}