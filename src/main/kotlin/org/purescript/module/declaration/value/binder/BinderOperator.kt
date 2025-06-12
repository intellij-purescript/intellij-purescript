package org.purescript.module.declaration.value.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.util.siblings
import org.purescript.file.PSFile
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.Qualified
import org.purescript.name.PSQualifiedOperatorName

/**
 * An Operator in an expression, e.g.
 * ```
 * P./\
 * ```
 * in
 * ```
 * import Prelude as P
 * f (a P./\ b) = a
 * ```
 */
class BinderOperator(node: ASTNode) : Binder(node), Qualified {
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


    override fun getName(): String = qualifiedOperator.name
    override fun unify() = 
        unify(reference.resolve()?.inferType()?.withNewIds((module.containingFile as PSFile).typeSpace.replaceMap()) ?: (module.containingFile as PSFile).typeSpace.newId())

    val associativity get() = reference.resolve()?.associativity
    val precedence get() = reference.resolve()?.precedence

    override fun getReference() =
        BinderOperatorReference(
            this,
            qualifiedOperator.moduleName,
            qualifiedOperator.operator
        )
}