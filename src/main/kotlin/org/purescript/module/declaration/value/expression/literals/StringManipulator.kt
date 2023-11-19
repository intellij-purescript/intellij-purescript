package org.purescript.module.declaration.value.expression.literals

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulator
import com.intellij.psi.ElementManipulators
import com.intellij.psi.impl.source.tree.LeafElement
import kotlin.math.max
import kotlin.math.min

class StringManipulator : ElementManipulator<PSStringLiteral> {
    override fun handleContentChange(
        element: PSStringLiteral,
        insideHost: TextRange,
        newContent: String
    ): PSStringLiteral? {
        val oldValue = ElementManipulators.getValueText(element)
        val quoteWith = getRangeInElement(element).startOffset
        // the quote might have drifted, if " have changed to """
        val newNewContent = TextRange.from(
            max(0, insideHost.startOffset - quoteWith),
            min(insideHost.length, oldValue.length)
        ).replace(oldValue, newContent)
        return this.handleContentChange(element, newNewContent)
    }

    override fun handleContentChange(element: PSStringLiteral, newContent: String): PSStringLiteral? {
        val literal = when {
            newContent.contains("\n") -> "\"\"\"$newContent\"\"\""
            else -> "\"$newContent\""
        }
        val valueNode = element.node.firstChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(literal)
        return element
    }

    override fun getRangeInElement(element: PSStringLiteral): TextRange {
        val text = element.text
        if (text.startsWith("\"\"\"") || text.startsWith("'''")) {
            return TextRange.from(3, text.length - 6)
        } else {
            return TextRange.from(1, text.length - 2)
        }
    }
}