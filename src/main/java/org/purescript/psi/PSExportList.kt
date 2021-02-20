package org.purescript.psi

import com.intellij.lang.ASTNode

class PSExportList(node: ASTNode) : PSPsiElement(node) {
    val exportedItems: Array<PSExportedItem> =
        findChildrenByClass(PSExportedItem::class.java)
}
