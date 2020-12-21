package net.kenro.ji.jin.purescript.psi.cst

import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

class PSStringManipulator : AbstractElementManipulator<PSASTWrapperElement>() {

    override fun handleContentChange(
        psi: PSASTWrapperElement,
        range: TextRange,
        newContent: String
    ): PSASTWrapperElement {
        val oldText = psi.text
        val newText = oldText.substring(
            0,
            range.startOffset
        ) + newContent + oldText.substring(range.endOffset)
        return psi.updateText(newText)
    }

    override fun getRangeInElement(element: PSASTWrapperElement): TextRange {
        return pairToTextRange(
            if (element.isBlockString) getRangeForBlockString(
                element
            ) else getRangeForString(element)
        )
    }

    companion object {
        private fun getRangeForBlockString(element: PSASTWrapperElement): Pair<Int, Int> {
            val text = element.stringText
            val start = text.indexOf("\"\"\"") + 3
            val end = text.lastIndexOf("\"\"\"") - start
            return Pair(start, end)
        }

        private fun getRangeForString(element: PSASTWrapperElement): Pair<Int, Int> {
            val text = element.stringText
            val start = text.indexOf("\"") + 1
            val end = text.lastIndexOf("\"") - start
            return Pair(start, end)
        }

        private fun pairToTextRange(pair: Pair<Int, Int>): TextRange {
            val start = Math.max(pair.first, 0)
            val end = Math.max(pair.second, start)
            return TextRange.from(start, end)
        }
    }
}