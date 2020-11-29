package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSTypeVar;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSTypeVarImpl extends PSPsiElement implements PSTypeVar {

    public PSTypeVarImpl(final ASTNode node) {
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
