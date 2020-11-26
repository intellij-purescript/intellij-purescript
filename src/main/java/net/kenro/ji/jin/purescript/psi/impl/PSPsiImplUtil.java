package net.kenro.ji.jin.purescript.psi.impl;


import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import net.kenro.ji.jin.purescript.psi.PSIdentifier;
import net.kenro.ji.jin.purescript.psi.PSProperName;

public class PSPsiImplUtil {

    public static String getName(final PSProperName element) {
        return element.getText();
    }

    public static PsiElement getNameIdentifier(final PSProperName element) {
        final ASTNode node = element.getNode();
        if (node != null) {
            return node.getPsi();
        } else {
            return null;
        }
    }

    public static String getName(final PSIdentifier element) {
        return element.getText();
    }

    public static PsiElement getNameIdentifier(final PSIdentifier element) {
        final ASTNode node = element.getNode();
        if (node != null) {
            return node.getPsi();
        } else {
            return null;
        }
    }
}
