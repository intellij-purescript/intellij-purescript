package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.psi.PsiElement

object PSPsiImplUtil {
    fun getName(element: PSProperNameImpl): String {
        return element.text
    }

}