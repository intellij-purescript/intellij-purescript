package org.purescript.psi.binder

import com.intellij.lang.ASTNode

/**
 * The node `box@(Box a)` in the code
 * 
 * ```purescript
 * f box@(Box a) = a
 * ```
 */
class NamedBinder(node: ASTNode) : Binder(node)