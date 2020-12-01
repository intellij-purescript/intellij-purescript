package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier;

public class PSConstructorBinderImpl extends PSPsiElement implements ContainsIdentifier {

    public PSConstructorBinderImpl(final ASTNode node) {
        super(node);
    }

}
