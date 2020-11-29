package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSNumberBinder;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSNumberBinderImpl extends PSPsiElement implements PSNumberBinder {

    public PSNumberBinderImpl(final ASTNode node) {
        super(node);
    }

}
