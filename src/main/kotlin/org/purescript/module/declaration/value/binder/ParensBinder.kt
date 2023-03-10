package org.purescript.module.declaration.value.binder

import com.intellij.lang.ASTNode

/**
 * The node `(Box a)` in the code
 * 
 * ```purescript
 * f (Box a) = a
 * ```
 */
class ParensBinder(node: ASTNode) : Binder(node)