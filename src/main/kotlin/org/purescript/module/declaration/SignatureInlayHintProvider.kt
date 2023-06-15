package org.purescript.module.declaration

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import org.purescript.module.declaration.value.ValueDeclarationGroup

class SignatureInlayHintProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
        return object : SharedBypassCollector {
            override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
                when (element) {
                    is ValueDeclarationGroup -> if (element.signature == null) {
                        val type = element
                            .checkType()
                            ?.substituteModuleNames(mapOf(
                                "Prim" to "",
                                "${element.module?.name}" to ""
                            ))
                        if (type != null) {
                            val tooltip = ":: $type"
                            sink.addPresentation(
                                InlineInlayPosition(
                                    element.nameIdentifier.endOffset ,
                                    true
                                ),
                                null,
                                null,
                                true,
                            ) {
                                text(tooltip)
                            }
                        }
                    }
                }
            }
        }

    }
}