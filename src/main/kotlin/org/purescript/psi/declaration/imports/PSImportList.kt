package org.purescript.psi.declaration.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.purescript.parser.HIDING
import org.purescript.psi.base.PSPsiElement

/**
 * The import list in an import declaration.
 *
 * Example:
 * `hiding (foo, bar)`
 *
 * in
 *
 * ```import Foo.Bar hiding (foo, bar) as Bar```
 */
class PSImportList(node: ASTNode) : PSPsiElement(node) {
    private val hidingElement: PsiElement?
        get() = findChildByType(HIDING)

    /**
     * Returns `true` if the import list is hiding its
     * imported items, `false` otherwise.
     */
    val isHiding: Boolean get() = hidingElement != null

    /**
     * The items that the import list contains. They may be
     * either hidden or exposed depending on whether [isHiding]
     * is `true` or `false`, respectively.
     */
    val importedItems: Array<PSImportedItem>
        get() =
            findChildrenByClass(PSImportedItem::class.java)
}
