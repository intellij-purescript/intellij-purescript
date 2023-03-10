package org.purescript.module.declaration.value.binder

import com.intellij.lang.ASTNode

/**
 * The node `_` in code like
 * 
 * ```purescript
 * f _ = 1
 * ```
 */
class WildcardBinder(node: ASTNode) : Binder(node)