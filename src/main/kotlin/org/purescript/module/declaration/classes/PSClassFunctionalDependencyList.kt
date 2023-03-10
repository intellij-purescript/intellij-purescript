package org.purescript.module.declaration.classes

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

/**
 * The functional dependency portion of a class declaration, e.g.
 * ```
 * | m -> a, m -> b
 * ```
 * in
 * ```
 * class Index m a b <= At m a b | m -> a, m -> b
 * ```
 */
class PSClassFunctionalDependencyList(node: ASTNode) : PSPsiElement(node) {

    /**
     * @return the [PSClassFunctionalDependency] elements contained in this list
     */
    val functionalDependencies: Array<PSClassFunctionalDependency>
        get() = findChildrenByClass(PSClassFunctionalDependency::class.java)
}
