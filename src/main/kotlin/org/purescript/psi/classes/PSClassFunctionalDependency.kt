package org.purescript.psi.classes

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

/**
 * A single functional dependency in a class declaration, e.g.
 * ```
 * a -> rep
 * ```
 * in
 * ```
 * class Generic a rep | a -> rep
 * ```
 */
class PSClassFunctionalDependency(node: ASTNode) : PSPsiElement(node) {
    /*
     * TODO [simonolander]
     *  This element currently only contains a single [PSTypeImpl],
     *  but I don't think that's correct. I think the parser is way
     *  too lax in what it considers functional dependencies.
     *  I suspect that it should not allow anything but type variables.
     */
}
