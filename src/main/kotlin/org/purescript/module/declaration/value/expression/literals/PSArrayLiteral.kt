package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.PSValue
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.*

class PSArrayLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    val values get() = findChildrenByClass(PSValue::class.java)

    override fun checkType(): TypeCheckerType = TypeApp(
        TypeConstructor("Prim.Array"),
        values.firstNotNullOfOrNull { (it as? TypeCheckable)?.checkType() }
            ?: Unknown(-1)
    )
}