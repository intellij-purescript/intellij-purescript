package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.VarBinder
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.Qualified
import org.purescript.module.declaration.value.expression.dostmt.PSDoNotationBind
import org.purescript.name.PSQualifiedIdentifier
import org.purescript.psi.PSPsiElement

/**
 * A identifier in an expression, e.g.
 * ```
 * add
 * ```
 * in
 * ```
 * f = add 1 3
 * ```
 */
class PSExpressionIdentifier(node: ASTNode) : PSPsiElement(node), ExpressionAtom, Qualified {

    val arguments: Sequence<Argument> get() = when(val parent = parent) {
        is Call -> parent.arguments
        else -> emptySequence()
    }

    /**
     * @return the [PSQualifiedIdentifier] identifying this constructor
     */
    internal val qualifiedIdentifier: PSQualifiedIdentifier
        get() = findNotNullChildByClass(PSQualifiedIdentifier::class.java)

    override val qualifierName: String? get() = qualifiedIdentifier.moduleName?.name

    override fun getName(): String = qualifiedIdentifier.name

    override fun getReference(): ExpressionIdentifierReference =
        ExpressionIdentifierReference(this)

    override fun areSimilarTo(other: Similar): Boolean {
        val ref = reference.resolve()
        val otherRef = other.reference?.resolve()
        return when {
            ref == otherRef -> true
            ref is ValueDeclarationGroup && otherRef is ValueDeclarationGroup -> ref.valueDeclarations
                .zip(otherRef.valueDeclarations) { a, b -> a.value.areSimilarTo(b.value) }
                .all { it }
            ref is VarBinder && otherRef is VarBinder -> {
                val expression = (ref.parent as? PSDoNotationBind)?.expression
                val otherExpression = (otherRef.parent as? PSDoNotationBind)?.expression
                expression?.let { otherExpression?.areSimilarTo(it) } ?: false
            }

            else -> false
        }
    }
}
