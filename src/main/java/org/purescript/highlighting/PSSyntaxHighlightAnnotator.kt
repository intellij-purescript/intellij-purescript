package org.purescript.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.purescript.parser.PSElements

class PSSyntaxHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.node.elementType == PSElements.ValueRef) {
            val text = element.text
            holder.newAnnotation(HighlightSeverity.INFORMATION, text)
                .textAttributes(PSSyntaxHighlighter.IMPORT_REF)
                .create()
        } else if (element.node.elementType == PSElements.TypeAnnotationName) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, element.text)
                .textAttributes(PSSyntaxHighlighter.TYPE_ANNOTATION_NAME)
                .create()
        } else if (element.node.elementType == PSElements.PositionedDeclarationRef
            || element.node.elementType == PSElements.TypeConstructor
            || element.node.elementType == PSElements.pClassName) {
            holder
                .newAnnotation(HighlightSeverity.INFORMATION, element.text)
                .textAttributes(PSSyntaxHighlighter.TYPE_NAME)
                .create()
        } else if (element.node.elementType == PSElements.GenericIdentifier
            || element.node.elementType == PSElements.Constructor
            || element.node.elementType == PSElements.qualifiedModuleName
        ) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, element.text)
                .textAttributes(PSSyntaxHighlighter.TYPE_VARIABLE)
                .create()
        } else if (element.node.elementType == PSElements.LocalIdentifier
        ) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, element.text)
                .textAttributes(PSSyntaxHighlighter.NUMBER)
                .create()
        }
    }
}