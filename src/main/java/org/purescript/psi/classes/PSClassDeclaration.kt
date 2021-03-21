package org.purescript.psi.classes

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.PSProperName
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSTypeVarImpl
import org.purescript.psi.typevar.PSTypeVarBinding

/**
 * A class declaration, e.g.
 * ```
 * class Foldable f <= FoldableWithIndex i f | f -> i where
 *     foldrWithIndex :: forall a b. (i -> a -> b -> b) -> b -> f a -> b
 *     foldlWithIndex :: forall a b. (i -> b -> a -> b) -> b -> f a -> b
 *     foldMapWithIndex :: forall a m. Monoid m => (i -> a -> m) -> f a -> m
 * ```
 */
class PSClassDeclaration(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner
{
    internal val classConstraintList: PSClassConstraintList?
        get() = findChildByClass(PSClassConstraintList::class.java)

    internal val properName: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    internal val typeVarBindings: Array<PSTypeVarBinding>
        get() = findChildrenByClass(PSTypeVarBinding::class.java)

    internal val functionalDependencyList: PSClassFunctionalDependencyList?
        get() = findChildByClass(PSClassFunctionalDependencyList::class.java)

    internal val classMemberList: PSClassMemberList?
        get() = findChildByClass(PSClassMemberList::class.java)

    override fun getName(): String = properName.name

    override fun setName(name: String): PsiElement? = null

    override fun getNameIdentifier(): PsiElement = properName

    override fun getTextOffset(): Int = properName.textOffset
}
