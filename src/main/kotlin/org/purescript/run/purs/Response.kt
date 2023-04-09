package org.purescript.run.purs

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange

data class Response(val result: List<Result>, val resultType: String) {
    data class Result(
        val message: String,
        val errorCode: String?,
        val position: Position,
        val suggestion: Suggestion?
    )

    data class Position(
        val startLine: Int,
        val endLine: Int,
        val startColumn: Int,
        val endColumn: Int
    ) {
        fun textRange(document: Document) = TextRange(getStart(document), getEnd(document))
        fun getStart(document: Document) =
            document.getLineStartOffset(this.startLine - 1) + startColumn - 1

        fun getEnd(document: Document): Int = minOf(
            document.getLineStartOffset(endLine - 1) + endColumn - 1,
            document.getLineEndOffset(endLine - 1)
        )
    }

    data class Suggestion(val replacement: String, val replaceRange: Position)
}