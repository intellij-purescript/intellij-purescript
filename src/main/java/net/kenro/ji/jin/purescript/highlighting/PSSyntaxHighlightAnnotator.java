package net.kenro.ji.jin.purescript.highlighting;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import net.kenro.ji.jin.purescript.psi.PSElements;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;


public class PSSyntaxHighlightAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (psiElement(PSElements.ValueRef).accepts(element)) {
            String text = element.getText();
            Annotation ann = holder.createInfoAnnotation(element, text);
            ann.setTextAttributes(PSSyntaxHighlighter.IMPORT_REF);
        } else if (psiElement(PSElements.TypeAnnotationName).accepts(element)) {
            Annotation ann = holder.createInfoAnnotation(element, element.getText());
            ann.setTextAttributes(PSSyntaxHighlighter.TYPE_ANNOTATION_NAME);
        } else if ((psiElement(PSElements.PositionedDeclarationRef).accepts(element)
                || psiElement(PSElements.TypeConstructor).accepts(element)
                || psiElement(PSElements.pClassName).accepts(element))) {
//                || psiElement(PSElements.pModuleName).accepts(element))) {
            Annotation ann = holder.createInfoAnnotation(element, element.getText());
            ann.setTextAttributes(PSSyntaxHighlighter.TYPE_NAME);
        } else if ((psiElement(PSElements.GenericIdentifier).accepts(element)
                || psiElement(PSElements.Constructor).accepts(element)
                || psiElement(PSElements.qualifiedModuleName).accepts(element))) {
            Annotation ann = holder.createInfoAnnotation(element, element.getText());
            ann.setTextAttributes(PSSyntaxHighlighter.TYPE_VARIABLE);
        } else if (psiElement(PSElements.LocalIdentifier).accepts(element)) {
            Annotation ann = holder.createInfoAnnotation(element, element.getText());
            ann.setTextAttributes(PSSyntaxHighlighter.NUMBER);
        }
    }


}
