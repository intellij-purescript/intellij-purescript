package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSType;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSTypeImpl extends PSPsiElement implements PSType {

    public PSTypeImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull PSVisitor visitor) {
        visitor.visitPSType(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof PSVisitor) accept((PSVisitor) visitor);
        else super.accept(visitor);
    }





}
