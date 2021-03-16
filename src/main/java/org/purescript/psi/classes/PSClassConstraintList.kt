package org.purescript.psi.classes

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

/**
 * The class constraint portion of a class declaration, e.g.
 * ```
 * (Decide f, Divisible f) <=
 * ```
 * in
 * ```
 * class (Decide f, Divisible f) <= Decidable f
 * ```
 */
class PSClassConstraintList(node: ASTNode) : PSPsiElement(node) {
}
