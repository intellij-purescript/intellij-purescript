package net.kenro.ji.jin.purescript.psi.cst

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafElement
import net.kenro.ji.jin.purescript.psi.PSElements

class PSASTWrapperElement(astNode: ASTNode?) : ASTWrapperPsiElement(
    astNode!!
) {
    val isString: Boolean
        get() {
            val type = node.elementType
            return type == PSElements.JSRaw || type == PSElements.StringLiteral
        }
    val isBlockString: Boolean
        get() = stringText.startsWith("\"\"\"")

    /**
     * Returns the text of a string element, including its quotes.
     */
    val stringText: String
        get() = if (isString) node.firstChildNode.text else ""

    fun updateText(s: String): PSASTWrapperElement {
        val valueNode = node.firstChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(s)
        return this
    }
}