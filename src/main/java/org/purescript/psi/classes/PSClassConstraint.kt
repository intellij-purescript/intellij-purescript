package org.purescript.psi.classes

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

/**
 * An individual constraint in a class declaration, e.g.
 * ```
 * Semigroup m
 * ```
 * in
 * ```
 * class Semigroup m <= Monoid m
 * ```
 */
class PSClassConstraint(node: ASTNode) : PSPsiElement(node) {
}
