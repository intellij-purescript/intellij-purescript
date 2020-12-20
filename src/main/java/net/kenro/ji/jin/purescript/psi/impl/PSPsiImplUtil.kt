package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.psi.PsiElement

object PSPsiImplUtil {
    fun getName(element: PSProperNameImpl): String {
        return element.text
    }

    fun getNameIdentifier(element: PSProperNameImpl): PsiElement? {
        val node = element.node
        return if (node != null) {
            node.psi
        } else {
            null
        }
    }
}