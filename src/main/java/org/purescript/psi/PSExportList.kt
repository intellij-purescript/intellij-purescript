package org.purescript.psi

import com.intellij.lang.ASTNode

class PSExportList(node: ASTNode) : PSPsiElement(node) {
    val exportedItems = findChildrenByClass(PSExportedItem::class.java)
}
