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
}
