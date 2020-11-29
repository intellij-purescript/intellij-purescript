package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSAccessor;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSAccessorImpl extends PSPsiElement implements PSAccessor {

    public PSAccessorImpl(final ASTNode node) {
        super(node);
    }

}
