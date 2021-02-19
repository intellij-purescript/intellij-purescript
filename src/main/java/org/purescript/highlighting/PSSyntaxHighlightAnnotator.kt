package org.purescript.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import org.purescript.parser.PSElements

class PSSyntaxHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (PlatformPatterns.psiElement(PSElements.ValueRef).accepts(element)) {
            val text = element.text
            holder.newAnnotation(HighlightSeverity.INFORMATION, text)
                .textAttributes(PSSyntaxHighlighter.IMPORT_REF)
                .create()
        } else if (PlatformPatterns.psiElement(PSElements.TypeAnnotationName)
                .accepts(element)
        ) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, element.text)
                .textAttributes(PSSyntaxHighlighter.TYPE_ANNOTATION_NAME)
                .create()
        } else if (PlatformPatterns.psiElement(PSElements.PositionedDeclarationRef)
                .accepts(element)
            || PlatformPatterns.psiElement(PSElements.TypeConstructor)
                .accepts(element)
            || PlatformPatterns.psiElement(PSElements.pClassName)
                .accepts(element)
        ) {
//                || psiElement(PSElements.pModuleName).accepts(element))) {
            holder
                .newAnnotation(HighlightSeverity.INFORMATION, element.text)
                .textAttributes(PSSyntaxHighlighter.TYPE_NAME)
                .create()
        } else if (PlatformPatterns.psiElement(PSElements.GenericIdentifier)
                .accepts(element)
            || PlatformPatterns.psiElement(PSElements.Constructor)
                .accepts(element)
            || PlatformPatterns.psiElement(PSElements.qualifiedModuleName)
                .accepts(element)
        ) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, element.text)
                .textAttributes(PSSyntaxHighlighter.TYPE_VARIABLE)
                .create()
        } else if (PlatformPatterns.psiElement(PSElements.LocalIdentifier)
                .accepts(element)
        ) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, element.text)
                .textAttributes(PSSyntaxHighlighter.NUMBER)
                .create()
        }
    }
}