package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.psi.PSPsiElement

/**
 * Represents one or many do statements
 *
 * They are grouped like so (DoStatements, DoStatement) or just DoStatement
 */
class DoStatements(node: ASTNode) : PSPsiElement(node), DoStatement, ValueNamespace {
    private val children get() = childrenOfType<DoStatement>()
    override val binders get() = children.flatMap { it.binders }
    override fun unify() = unify(children.map { it.inferType() }.last())
    override val statements get() = children.flatMap { it.statements }
    override val scopes: Array<PsiElement> get() = (parent as? DoStatements)?.scopes ?: arrayOf(this)
    override val valueNames get() = doStatements?.declarations ?: emptySequence()
    private val doStatements get() = childrenOfType<DoStatements>().firstOrNull()
    val declarations: Sequence<PsiNamedElement>
        get() = children.reversed().asSequence().flatMap {
            when (it) {
                is DoStatements -> it.declarations
                is PSDoNotationLet -> it.valueNames
                is PSDoNotationBind -> it.binders.filterIsInstance<PsiNamedElement>().asSequence()
                else -> emptySequence()
            }
        }


}