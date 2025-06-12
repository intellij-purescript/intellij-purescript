package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.file.PSFile
import org.purescript.inference.InferType

import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class PSArrayLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    val values get() = findChildrenByClass(Expression::class.java)
    override fun unify() {
        val arg = (module.containingFile as PSFile).typeSpace.newId()
        for (value in values) unify(arg, value.inferType())
        unify(InferType.Array.app(arg))
    }
}