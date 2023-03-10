package org.purescript.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.parentOfType
import org.purescript.module.Module

abstract class PSPsiElement(node: ASTNode, val string: String? = null) :
    ASTWrapperPsiElement(node) {

    override fun toString(): String {
        if (string != null) return string + "(" + node.elementType + ")"
        else return super.toString()
    }

    /**
     * @return the [Module] containing this element
     */
    val module: Module? get() = this.parentOfType(true)
}
