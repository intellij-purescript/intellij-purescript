package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.declaration.value.ValueDeclarationGroup

/**
 * A where claus un a expression, e.g.
 * ```
 * where x = 1
 * ```
 * in
 * ```
 * f = x
 *   where x = 1
 * ```
 */
class PSExpressionWhere(node: ASTNode) : PSPsiElement(node) {
    val expressionAtoms: List<ExpressionAtom>
        get() = valueDeclarationGroups
            .flatMap { it.expressionAtoms } +
            (where?.expressionAtoms ?: emptyList())
    val where get() = findChildByClass(PSExpressionWhere::class.java)
    val valueDeclarationGroups: Array<ValueDeclarationGroup>
        get() =
            findChildrenByClass(ValueDeclarationGroup::class.java)
}