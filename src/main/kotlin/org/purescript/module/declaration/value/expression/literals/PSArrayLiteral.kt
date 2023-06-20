package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.Prim
import org.purescript.typechecker.TypeCheckable
import org.purescript.typechecker.TypeCheckerType

class PSArrayLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    val values get() = findChildrenByClass(Expression::class.java)
    override fun checkReferenceType() = TypeCheckerType.TypeApp(
        Prim.array,
        values.firstNotNullOfOrNull { (it as? TypeCheckable)?.checkType() }
            ?: TypeCheckerType.Unknown
    )

    override fun infer(scope: Scope): Type = Type.Array
}