package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import org.purescript.file.PSFile

import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.Qualified
import org.purescript.module.declaration.value.expression.ReplaceableWithInline
import org.purescript.name.PSQualifiedOperatorName
import org.purescript.psi.InlinableElement
import org.purescript.psi.PSPsiElement

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
class PSExpressionOperator(node: ASTNode) : PSPsiElement(node), ExpressionAtom, Qualified, ReplaceableWithInline {

    /**
     * @return the [PSQualifiedOperatorName] identifying this constructor
     */
    internal val qualifiedOperator: PSQualifiedOperatorName
        get() = findNotNullChildByClass(PSQualifiedOperatorName::class.java)
    override val qualifierName: String? get() = qualifiedOperator.moduleName?.name
    val arguments
        get() = listOf(
            siblings(false, false).filterIsInstance<Expression>().first(),
            siblings(true, false).filterIsInstance<Expression>().first(),
        )

    override fun replaceWithInline(toInlineWith: InlinableElement) {
        parentOfType<Expression>()?.replace(toInlineWith.inline(arguments))
    }

    override fun canBeReplacedWithInline(): Boolean {
        return parent.children.size == 3
    }

    override fun getName(): String = qualifiedOperator.name
    override fun unify() = 
        unify(reference.resolve()?.inferType()?.withNewIds((module.containingFile as PSFile).typeSpace.replaceMap()) ?: (module.containingFile as PSFile).typeSpace.newId())

    val associativity get() = reference.resolve()?.associativity
    val precedence get() = reference.resolve()?.precedence

    override fun getReference() =
        ExpressionSymbolReference(
            this,
            qualifiedOperator.moduleName,
            qualifiedOperator.operator
        )
}