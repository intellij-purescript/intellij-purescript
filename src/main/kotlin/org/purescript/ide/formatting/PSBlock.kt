package org.purescript.ide.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.purescript.parser.WS


class PSBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val spacingBuilder: SpacingBuilder
) :
    AbstractBlock(node, wrap, alignment) {
    override fun buildChildren(): MutableList<Block> {
        val blocks = arrayListOf<Block>()
        var child = myNode.firstChildNode
        while (child != null) {
            if (child.elementType === WS || child.textLength == 0) continue
            val block: Block = PSBlock(
                child,
                Wrap.createWrap(WrapType.NONE, false),
                Alignment.createAlignment(),
                spacingBuilder
            )
            blocks.add(block)
            child = child.treeNext
        }
        return blocks
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2);
    }

}