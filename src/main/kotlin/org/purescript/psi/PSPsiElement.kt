package org.purescript.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.util.parentOfType
import org.purescript.inference.HasTypeId
import org.purescript.inference.InferType
import org.purescript.module.Module

abstract class PSPsiElement(node: ASTNode, val string: String? = null) :
    ASTWrapperPsiElement(node), HasTypeId {
    private val LOG: Logger = Logger.getInstance(PSPsiElement::class.java)

    override fun toString(): String =
        if (string != null) string + "(" + node.elementType + ")"
        else super.toString()

    /**
     * @return the [Module] containing this element
     */
    val module: Module get() = this.parentOfType(true) ?: error("Failed to parse module")
    override val typeId get() = module.typeIdOf(this)
    override val substitutedType: InferType
        get() =
            typeId.let { module.substitute(it) }

    fun unify(other: InferType) {
        try {
            unify(substitutedType, other)
        } catch (e: IllegalStateException) {
            LOG.error(e)
        }
    }
    fun unify(first: InferType, other: InferType) {
        module.unify(first, other)
    }
}
