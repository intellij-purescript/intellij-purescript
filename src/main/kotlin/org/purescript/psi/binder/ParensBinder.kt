package org.purescript.psi.binder

import com.intellij.lang.ASTNode

/**
 * The node `(Box a)` in the code
 * 
 * ```purescript
 * f (Box a) = a
 * ```
 */
class ParensBinder(node: ASTNode) : Binder(node)