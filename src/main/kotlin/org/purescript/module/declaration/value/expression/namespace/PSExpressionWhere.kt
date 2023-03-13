package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

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
    val valueDeclarationGroups get() = childrenOfType<ValueDeclarationGroup>()
    private val binderChildren = childrenOfType<LetBinder>()
    private val namedBinders = binderChildren.asSequence().flatMap { it.namedBinders }
    val valueNames: Sequence<PsiNamedElement> get() = namedBinders + valueDeclarationGroups.asSequence()
}