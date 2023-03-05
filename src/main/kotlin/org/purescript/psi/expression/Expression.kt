package org.purescript.psi.expression

import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.declaration.classes.PSClassMemberList
import org.purescript.psi.module.Module

interface Expression : PsiElement {
    fun getAtoms(): Sequence<ExpressionAtom>
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
    
    val globals: Set<PSExpressionIdentifier>
        get() {
            val allIdentifiers = getAtoms().filterIsInstance<PSExpressionIdentifier>().toSet()
            val dependencisSet = dependencies.toSet()
            return allIdentifiers.subtract(dependencisSet)
        }
    
    fun areSimilarTo(other: Expression): Boolean {
        if (this.javaClass != other.javaClass) return false
        if (children.size != other.children.size) return false
        return childrenOfType<Expression>()
            .zip(other.childrenOfType<Expression>()) { a, b -> a.areSimilarTo(b) }
                .all { it }
    }
}