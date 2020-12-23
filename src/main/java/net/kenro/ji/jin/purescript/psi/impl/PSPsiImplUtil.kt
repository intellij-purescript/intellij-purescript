package net.kenro.ji.jin.purescript.psi.impl

object PSPsiImplUtil {
    fun getName(element: PSProperNameImpl): String {
        return element.text
    }

}