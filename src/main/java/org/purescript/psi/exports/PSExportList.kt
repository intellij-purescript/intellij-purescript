package org.purescript.psi.exports

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

/**
 * The export list in the module signature.
 *
 * Example:
 * `(foo, bar)`
 *
 * in
 *
 * ```module Foo.Bar (foo, bar) where```
 */
class PSExportList(node: ASTNode) : PSPsiElement(node) {
    val exportedItems: Array<PSExportedItem> =
        findChildrenByClass(PSExportedItem::class.java)
}
