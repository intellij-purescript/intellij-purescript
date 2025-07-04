package org.purescript.module.declaration

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import org.purescript.inference.Inferable
import org.purescript.inference.RecursiveTypeException

class DebugTypeInlayHintProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector {
        return object : SharedBypassCollector {
            override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
                when (element) {
                    is Inferable -> {
                        if(element.children.any { it is Inferable }) return
                        try {
                            val hint = ":: ${element.inferType()}"
                            sink.addPresentation(
                                InlineInlayPosition(element.endOffset, true),
                                null,
                                null,
                                HintFormat.default
                            ) { text(hint) }
                        } catch (e: NotImplementedError) {
                            sink.addPresentation(
                                InlineInlayPosition(element.endOffset, true),
                                null,
                                null,
                                HintFormat.default
                            ) { text("?! ${e.message?.removePrefix("An operation is not implemented: ")}") }
                        } catch (e: RecursiveTypeException) {
                            sink.addPresentation(
                                InlineInlayPosition(element.endOffset, true),
                                null,
                                null,
                                HintFormat.default
                            ) { text("?! ${e.message}") }
                        }
                    }
                }
            }
        }
    }
}