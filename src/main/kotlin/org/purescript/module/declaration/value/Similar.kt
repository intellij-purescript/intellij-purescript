package org.purescript.module.declaration.value

import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType

interface Similar: PsiElement {

    fun areSimilarTo(other: Similar): Boolean {
        if (this.javaClass != other.javaClass) return false
        if (children.size != other.children.size) return false
        val children = childrenOfType<Similar>()
        val otherChildren = other.childrenOfType<Similar>()
        if (children.size != otherChildren.size) return false
        return children.zip(otherChildren) { a, b -> a.areSimilarTo(b) }.all { it }
    }
}