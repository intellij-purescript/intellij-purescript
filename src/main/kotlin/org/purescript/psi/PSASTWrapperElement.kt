package org.purescript.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafElement
import org.purescript.parser.StringLiteral

class PSASTWrapperElement(astNode: ASTNode?) : ASTWrapperPsiElement(
    astNode!!
) {
    val isBlockString: Boolean
        get() = stringText.startsWith("\"\"\"")

    /**
     * Returns the text of a string element, including its quotes.
     */
    val stringText: String
        get() = if (node.elementType == StringLiteral) node.firstChildNode.text else ""

    fun updateText(s: String): PSASTWrapperElement {
        val valueNode = node.firstChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(s)
        return this
    }
}