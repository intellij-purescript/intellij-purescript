package org.purescript.psi.declaration.classes

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement

/**
 * The members part of a class declaration, e.g.
 * ```
 * where
 *     foldrWithIndex :: forall a b. (i -> a -> b -> b) -> b -> f a -> b
 *     foldlWithIndex :: forall a b. (i -> b -> a -> b) -> b -> f a -> b
 *     foldMapWithIndex :: forall a m. Monoid m => (i -> a -> m) -> f a -> m
 * ```
 * in
 * ```
 * class Foldable f <= FoldableWithIndex i f | f -> i where
 *     foldrWithIndex :: forall a b. (i -> a -> b -> b) -> b -> f a -> b
 *     foldlWithIndex :: forall a b. (i -> b -> a -> b) -> b -> f a -> b
 *     foldMapWithIndex :: forall a m. Monoid m => (i -> a -> m) -> f a -> m
 * ```
 */
class PSClassMemberList(node: ASTNode) : PSPsiElement(node) {

    /**
     * @return the [PSClassMember] elements contained in this list
     */
    val classMembers: Array<PSClassMember>
        get() = findChildrenByClass(PSClassMember::class.java)
}
