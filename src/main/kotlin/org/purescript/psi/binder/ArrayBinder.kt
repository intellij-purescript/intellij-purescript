package org.purescript.psi.binder

import com.intellij.lang.ASTNode
/**
 * The node `[1, 2]` in the code
 *
 * ```purescript
 * f [1, 2] = a
 * ```
 */
class ArrayBinder(node: ASTNode) : Binder(node)