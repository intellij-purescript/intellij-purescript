package org.purescript.psi

object PSPsiImplUtil {
    fun getName(element: PSProperName): String {
        return element.text
    }

}