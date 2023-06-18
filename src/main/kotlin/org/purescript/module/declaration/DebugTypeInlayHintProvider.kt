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
                        val type = element.checkType().toString()
                        val hint = ":: $type"
                        sink.addPresentation(
                            InlineInlayPosition(element.endOffset, true),
                            null, null, true
                        ) { text(hint) }
                    }
                }
            }
        }
    }
}