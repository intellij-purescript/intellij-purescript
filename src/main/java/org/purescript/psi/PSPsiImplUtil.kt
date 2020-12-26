package org.purescript.psi

object PSPsiImplUtil {
    fun getName(element: PSProperNameImpl): String {
        return element.text
    }

}