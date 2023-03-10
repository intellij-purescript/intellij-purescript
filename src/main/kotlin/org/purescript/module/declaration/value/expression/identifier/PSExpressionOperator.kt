package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.Qualified
import org.purescript.name.PSQualifiedOperatorName

/**
 * An Operator in an expression, e.g.
 * ```
 * P.+
 * ```
 * in
 * ```
 * import Prelude as P
 * f = 1 P.+ 3
 * ```
 */
class PSExpressionOperator(node: ASTNode) : PSPsiElement(node), ExpressionAtom, Qualified {

    /**
     * @return the [PSQualifiedOperatorName] identifying this constructor
     */
    internal val qualifiedOperator: PSQualifiedOperatorName
        get() = findNotNullChildByClass(PSQualifiedOperatorName::class.java)
    override val qualifierName: String? = qualifiedOperator.moduleName?.name
    override fun getName(): String = qualifiedOperator.name


    val associativity get() = reference.resolve()?.associativity
    val precedence get() = reference.resolve()?.precedence

    override fun getReference() =
        ExpressionSymbolReference(
            this,
            qualifiedOperator.moduleName,
            qualifiedOperator.operator
        )
}