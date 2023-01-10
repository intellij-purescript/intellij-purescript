package org.purescript.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.psi.PsiElement
import org.purescript.highlighting.PSSyntaxHighlighter.Companion.NUMBER
import org.purescript.highlighting.PSSyntaxHighlighter.Companion.TYPE_NAME
import org.purescript.highlighting.PSSyntaxHighlighter.Companion.TYPE_VARIABLE
import org.purescript.parser.ExpressionConstructor
import org.purescript.parser.GenericIdentifier
import org.purescript.parser.LocalIdentifier
import org.purescript.parser.TypeConstructor
import org.purescript.parser.ClassName

class PSSyntaxHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.node.elementType) {
            TypeConstructor, ClassName -> {
                holder.newSilentAnnotation(INFORMATION)
                    .textAttributes(TYPE_NAME)
                    .create()
            }
            GenericIdentifier, ExpressionConstructor -> {
                holder.newSilentAnnotation(INFORMATION)
                    .textAttributes(TYPE_VARIABLE)
                    .create()
            }
            LocalIdentifier -> {
                holder.newSilentAnnotation(INFORMATION)
                    .textAttributes(NUMBER)
                    .create()
            }
        }
    }
}
