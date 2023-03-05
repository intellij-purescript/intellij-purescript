package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement

sealed class PSBinderAtom(node: ASTNode?) : PSPsiElement(node!!) {
    val binders
        get(): List<PSBinderAtom> = childrenOfType<PSBinderAtom>().flatMap {
            it.binders
        } + listOf(this)
}