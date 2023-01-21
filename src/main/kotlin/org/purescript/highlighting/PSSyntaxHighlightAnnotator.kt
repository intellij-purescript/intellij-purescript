package org.purescript.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.psi.PsiElement
import org.purescript.highlighting.PSSyntaxHighlighter.TYPE_NAME
import org.purescript.highlighting.PSSyntaxHighlighter.TYPE_VARIABLE
import org.purescript.parser.*

class PSSyntaxHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.node.elementType) {
            TypeCtor, ClassName -> {
                holder.newSilentAnnotation(INFORMATION)
                    .textAttributes(TYPE_NAME)
                    .create()
            }
            ExpressionCtor -> {
                holder.newSilentAnnotation(INFORMATION)
                    .textAttributes(TYPE_VARIABLE)
                    .create()
            }
        }
    }
}
