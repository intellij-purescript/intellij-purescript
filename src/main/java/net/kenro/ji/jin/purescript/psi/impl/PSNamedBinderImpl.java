package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSNamedBinder;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSNamedBinderImpl extends PSPsiElement implements PSNamedBinder {

    public PSNamedBinderImpl(final ASTNode node) {
        super(node);
    }

    public void accept(@NotNull final PSVisitor visitor) {
        visitor.visitElement(this);
    }

    public void accept(@NotNull final PsiElementVisitor visitor) {
        if (visitor instanceof PSVisitor) accept((PSVisitor) visitor);
        else super.accept(visitor);
    }





}
