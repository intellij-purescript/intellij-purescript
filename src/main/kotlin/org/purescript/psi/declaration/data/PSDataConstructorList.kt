package org.purescript.psi.declaration.data

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement

/**
 * The data constructors in a data declaration, e.g.
 *
 * ```
 * = Left a | Right b
 * ```
 * in
 * ```
 * data Either a b = Left a | Right b
 * ```
 */
class PSDataConstructorList(node: ASTNode) : PSPsiElement(node) {
    /**
     * @return the [PSDataConstructor] elements in this list
     */
    internal val dataConstructors: Array<PSDataConstructor>
        get() = findChildrenByClass(PSDataConstructor::class.java)
}
