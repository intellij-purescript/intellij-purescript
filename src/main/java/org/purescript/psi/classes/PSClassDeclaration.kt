package org.purescript.psi.classes

import com.intellij.lang.ASTNode
import org.purescript.psi.PSProperName
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSTypeVarImpl

/**
 * A class declaration, e.g.
 * ```
 * class Foldable f <= FoldableWithIndex i f | f -> i where
 *     foldrWithIndex :: forall a b. (i -> a -> b -> b) -> b -> f a -> b
 *     foldlWithIndex :: forall a b. (i -> b -> a -> b) -> b -> f a -> b
 *     foldMapWithIndex :: forall a m. Monoid m => (i -> a -> m) -> f a -> m
 * ```
 */
class PSClassDeclaration(node: ASTNode) : PSPsiElement(node) {
    internal val classConstraintList: PSClassConstraintList?
        get() = findChildByClass(PSClassConstraintList::class.java)

    internal val properName: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    internal val typeVariables: Array<PSTypeVarImpl>
        get() = findChildrenByClass(PSTypeVarImpl::class.java)

    internal val functionalDependencyList: Array<PSClassFunctionalDependencyList>
        get() = findChildrenByClass(PSClassFunctionalDependencyList::class.java)


}
