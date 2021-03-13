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
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .textAttributes(PSSyntaxHighlighter.IMPORT_REF)
                    .create()
            }
            PSElements.TypeAnnotationName -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .textAttributes(PSSyntaxHighlighter.TYPE_ANNOTATION_NAME)
                    .create()
            }
            PSElements.PositionedDeclarationRef, PSElements.TypeConstructor, PSElements.pClassName -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .textAttributes(PSSyntaxHighlighter.TYPE_NAME)
                    .create()
            }
            PSElements.GenericIdentifier, PSElements.Constructor, PSElements.qualifiedModuleName -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .textAttributes(PSSyntaxHighlighter.TYPE_VARIABLE)
                    .create()
            }
            PSElements.LocalIdentifier -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .textAttributes(PSSyntaxHighlighter.NUMBER)
                    .create()
            }
        }
    }
}