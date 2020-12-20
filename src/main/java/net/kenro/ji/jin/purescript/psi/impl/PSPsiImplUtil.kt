package net.kenro.ji.jin.purescript.psi.impl;


import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

public class PSPsiImplUtil {

    public static String getName(final PSProperNameImpl element) {
        return element.getText();
    }

    public static PsiElement getNameIdentifier(final PSProperNameImpl element) {
        final ASTNode node = element.getNode();
        if (node != null) {
            return node.getPsi();
        } else {
            return null;
        }
    }

}
