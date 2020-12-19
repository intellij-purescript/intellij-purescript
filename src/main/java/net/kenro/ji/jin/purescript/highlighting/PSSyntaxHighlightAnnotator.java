package net.kenro.ji.jin.purescript.highlighting;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import net.kenro.ji.jin.purescript.psi.PSElements;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;


public class PSSyntaxHighlightAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull final AnnotationHolder holder) {
        if (psiElement(PSElements.ValueRef).accepts(element)) {
            final String text = element.getText();
            holder.newAnnotation(HighlightSeverity.INFORMATION, text)
                .textAttributes(PSSyntaxHighlighter.IMPORT_REF)
                .create();
        } else if (psiElement(PSElements.TypeAnnotationName).accepts(element)) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, element.getText())
                .textAttributes(PSSyntaxHighlighter.TYPE_ANNOTATION_NAME)
                .create();
        } else if ((psiElement(PSElements.PositionedDeclarationRef).accepts(element)
                || psiElement(PSElements.TypeConstructor).accepts(element)
                || psiElement(PSElements.pClassName).accepts(element))) {
//                || psiElement(PSElements.pModuleName).accepts(element))) {
            holder
                .newAnnotation(HighlightSeverity.INFORMATION, element.getText())
                .textAttributes(PSSyntaxHighlighter.TYPE_NAME)
                .create();
        } else if ((psiElement(PSElements.GenericIdentifier).accepts(element)
                || psiElement(PSElements.Constructor).accepts(element)
                || psiElement(PSElements.qualifiedModuleName).accepts(element))) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, element.getText())
                .textAttributes(PSSyntaxHighlighter.TYPE_VARIABLE)
                .create();
        } else if (psiElement(PSElements.LocalIdentifier).accepts(element)) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, element.getText())
                .textAttributes(PSSyntaxHighlighter.NUMBER)
                .create();
        }
    }


}
