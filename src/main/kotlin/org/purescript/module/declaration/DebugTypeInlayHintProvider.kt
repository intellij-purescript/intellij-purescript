package org.purescript.module.declaration

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import org.purescript.typechecker.TypeCheckable

class DebugTypeInlayHintProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
        return object : SharedBypassCollector {
            override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
                when (element) {
                    is TypeCheckable -> {
                        if(element.children.any { it is TypeCheckable }) return
                        val document = editor.document
                        val line = document.getLineNumber(element.endOffset)
                        val position = EndOfLinePosition(line)
                        val type = element.checkType().toString()
                        val hint = "${element.text} :: $type"
                        sink.addPresentation(
                            position, null, null, true
                        ) { text(hint) }
                    }
                }
            }
        }
    }
}