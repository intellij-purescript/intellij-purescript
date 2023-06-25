package org.purescript.module.declaration

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import org.purescript.inference.InferType
import org.purescript.inference.RecursiveTypeException
import org.purescript.inference.inferType
import org.purescript.module.declaration.value.ValueDeclarationGroup

class SignatureInlayHintProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector {
        return object : SharedBypassCollector {
            override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
                when (element) {
                    is ValueDeclarationGroup -> if (element.signature == null) {
                        fun PresentationTreeBuilder.pprint(type: InferType) {
                            when (type) {
                                is InferType.App -> {
                                    if (type.f == InferType.Record) {
                                        text("{")
                                        collapsibleList(CollapseState.Collapsed,
                                            {
                                                toggleButton {
                                                    var first = true
                                                    for (label in (type.on as InferType.Row).mergedLabels()) {
                                                        if (!first) text(", ")
                                                        first = false
                                                        text("${label.first} :: ")
                                                        pprint(label.second)
                                                    }
                                                }
                                            },
                                            {toggleButton { text("...") }}
                                        )
                                        text("}")
                                    } else {
                                        text("${type.f} ")
                                        pprint(type.on)
                                    }    
                                }
                                is InferType.Row -> {
                                    collapsibleList(CollapseState.Collapsed,
                                        {
                                            text("(")
                                            var first = true
                                            for (label in type.mergedLabels()) {
                                                if (!first) text(", ")
                                                first = false
                                                text("${label.first} :: ")
                                                pprint(label.second)
                                            }
                                            text(")")
                                        },
                                        { toggleButton { text("...") } }
                                    )
                                }

                                else ->
                                    text("$type")
                            }
                        }
                        try {
                            val type = element.inferType()
                            sink.addPresentation(
                                InlineInlayPosition(
                                    element.nameIdentifier.endOffset,
                                    true
                                ),
                                null,
                                null,
                                true,
                            ) {
                                text(":: ")
                                this.pprint(type)
                            }
                        } catch (e: NotImplementedError) {
                            return
                        } catch (e: RecursiveTypeException) {
                            return
                        }
                    }
                }
            }
        }

    }
}