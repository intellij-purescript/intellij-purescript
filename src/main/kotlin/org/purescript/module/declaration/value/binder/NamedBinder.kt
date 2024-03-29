package org.purescript.module.declaration.value.binder

import com.intellij.lang.ASTNode

/**
 * The node `box@(Box a)` in the code
 * 
 * ```purescript
 * f box@(Box a) = a
 * ```
 */
class NamedBinder(node: ASTNode) : Binder(node)