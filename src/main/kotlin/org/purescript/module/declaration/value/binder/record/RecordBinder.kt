package org.purescript.module.declaration.value.binder.record

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.binder.Binder

/**
 * The node `{a: 1, b}` in the code
 *
 * ```purescript
 * f {a: 1, b} = 1
 * ```
 */
class RecordBinder(node: ASTNode) : Binder(node)