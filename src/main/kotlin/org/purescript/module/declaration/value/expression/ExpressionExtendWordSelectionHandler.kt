package org.purescript.module.declaration.value.expression

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class ExpressionExtendWordSelectionHandler : ExtendWordSelectionHandler {
    override fun canSelect(e: PsiElement) = e is Expression

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor)
            : MutableList<TextRange> =
        (e as? Expression)
            ?.let { fromElement(e) }
            ?: mutableListOf()

    fun fromElement(e: Expression): MutableList<TextRange> =
        OperatorExpression.Tree.fromElement(e)
            ?.ranges()
            ?.toMutableList()
            ?: mutableListOf()


}