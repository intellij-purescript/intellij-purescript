package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.module.declaration.value.expression.identifier.PSAccessor
import org.purescript.psi.PSPsiElement

class RecordAccess(node: ASTNode) : PSPsiElement(node), Expression {
    val record get() = findNotNullChildByClass(Expression::class.java)
    val accessor get() = findNotNullChildByClass(PSAccessor::class.java)
    override fun infer(scope: Scope)= 
        scope.inferAccess(record.infer(scope), accessor.name)
}