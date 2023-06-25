package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.util.parentsOfType
import org.purescript.inference.Inferable

import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.VarBinder
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.Qualified
import org.purescript.module.declaration.value.expression.ReplaceableWithInline
import org.purescript.module.declaration.value.expression.dostmt.PSDoNotationBind
import org.purescript.module.declaration.value.expression.literals.RecordLabel
import org.purescript.name.PSQualifiedIdentifier
import org.purescript.psi.InlinableElement
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSPsiFactory

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
class PSExpressionIdentifier(node: ASTNode) : PSPsiElement(node), ExpressionAtom, Qualified, ReplaceableWithInline {

    val arguments: Sequence<Argument>
        get() = when (val parent = parent) {
            is Call -> parent.arguments
            else -> emptySequence()
        }

    /**
     * @return the [PSQualifiedIdentifier] identifying this constructor
     */
    internal val qualifiedIdentifier: PSQualifiedIdentifier get() = findNotNullChildByClass(PSQualifiedIdentifier::class.java)
    override val qualifierName: String? get() = qualifiedIdentifier.moduleName?.name
    override fun canBeReplacedWithInline(): Boolean = true
    override fun replaceWithInline(toInlineWith: InlinableElement) {
        val arguments = this.arguments.toList()
        val toReplaceWith = toInlineWith.inline(arguments)
        val factory = project.service<PSPsiFactory>()
        when (val parent = this.parent) {
            is Call -> {
                this.parentsOfType<Call>()
                    .drop(arguments.size - 1)
                    .first()
                    .replace(toReplaceWith.let { it.withParenthesis() ?: it })
            }

            is RecordLabel -> factory
                .createRecordLabel("${this.name}: ${toReplaceWith.text}")
                ?.let { parent.replace(it) }
                ?: this.replace(toReplaceWith)

            is Argument -> this.replace(this.replace(toReplaceWith.let { it.withParenthesis() ?: it }))
            else -> this.replace(toReplaceWith.let { it.withParenthesis() ?: it })
        }
    }

    override fun getName(): String = qualifiedIdentifier.name
    override fun getReference(): ExpressionIdentifierReference = ExpressionIdentifierReference(this)
    override fun areSimilarTo(otherUnknown: Similar): Boolean {
        val other = otherUnknown as? PSExpressionIdentifier ?: return false
        val ref = reference.resolve()
        val otherRef = other.reference.resolve()
        return when {
            ref == otherRef -> true
            name != other.name -> false
            ref is ValueDeclarationGroup && otherRef is ValueDeclarationGroup -> ref.valueDeclarations
                .zip(otherRef.valueDeclarations) { a, b -> b.value?.let { a.value?.areSimilarTo(it) } ?: false }
                .all { it }

            ref is VarBinder && otherRef is VarBinder -> {
                val expression = (ref.parent as? PSDoNotationBind)?.expression
                val otherExpression = (otherRef.parent as? PSDoNotationBind)?.expression
                expression?.let { otherExpression?.areSimilarTo(it) } ?: false
            }

            else -> false
        }
    }
    override fun unify() {
        val module = module
        val ref = reference.resolve()
        when (ref) {
            is VarBinder -> ref.inferType()
            is Inferable -> ref.inferType().withNewIds(module.replaceMap())
            else -> null
        }?.let { unify(it) }
    }

}
