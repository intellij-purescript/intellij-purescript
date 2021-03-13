package org.purescript.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.purescript.parser.PSElements

class PSSyntaxHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.node.elementType) {
            PSElements.ValueRef -> {
                val text = element.text
                holder.newAnnotation(HighlightSeverity.INFORMATION, text)
                    .textAttributes(PSSyntaxHighlighter.IMPORT_REF)
                    .create()
            }
            PSElements.TypeAnnotationName -> {
                holder.newAnnotation(HighlightSeverity.INFORMATION, element.text)
                    .textAttributes(PSSyntaxHighlighter.TYPE_ANNOTATION_NAME)
                    .create()
            }
            PSElements.PositionedDeclarationRef, PSElements.TypeConstructor, PSElements.pClassName -> {
                holder
                    .newAnnotation(HighlightSeverity.INFORMATION, element.text)
                    .textAttributes(PSSyntaxHighlighter.TYPE_NAME)
                    .create()
            }
            PSElements.GenericIdentifier, PSElements.Constructor, PSElements.qualifiedModuleName -> {
                holder.newAnnotation(HighlightSeverity.INFORMATION, element.text)
                    .textAttributes(PSSyntaxHighlighter.TYPE_VARIABLE)
                    .create()
            }
            PSElements.LocalIdentifier -> {
                holder.newAnnotation(HighlightSeverity.INFORMATION, element.text)
                    .textAttributes(PSSyntaxHighlighter.NUMBER)
                    .create()
            }
        }
    }
}