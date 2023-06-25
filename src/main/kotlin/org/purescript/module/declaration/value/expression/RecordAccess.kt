package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType

import org.purescript.module.declaration.value.expression.identifier.PSAccessor
import org.purescript.psi.PSPsiElement

class RecordAccess(node: ASTNode) : PSPsiElement(node), Expression {
    val record get() = findNotNullChildByClass(Expression::class.java)
    val accessor get() = findNotNullChildByClass(PSAccessor::class.java)
    override fun unify() {
        val recordType = record.inferType()
        val thisType = substitutedType
        unify(recordType, InferType.record(listOf(accessor.name to thisType)))
    }
}