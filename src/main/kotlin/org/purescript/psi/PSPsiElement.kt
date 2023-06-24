package org.purescript.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.parentOfType
import org.purescript.inference.HasTypeId
import org.purescript.inference.InferType
import org.purescript.module.Module

abstract class PSPsiElement(node: ASTNode, val string: String? = null) :
    ASTWrapperPsiElement(node), HasTypeId {

    override fun toString(): String {
        if (string != null) return string + "(" + node.elementType + ")"
        else return super.toString()
    }

    /**
     * @return the [Module] containing this element
     */
    val module: Module? get() = this.parentOfType(true)
    override val typeId get() = module?.typeIdOf(this)
    override val substitutedType: InferType? get() = typeId?.let{ module?.substitute(it) }
    fun unify(other: InferType) {
        module?.unify(typeId ?: return, other )
    }
}
