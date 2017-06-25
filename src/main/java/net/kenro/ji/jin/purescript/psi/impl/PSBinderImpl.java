package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import net.kenro.ji.jin.purescript.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PSBinderImpl extends PSPsiElement implements PSBinder {

    public PSBinderImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull PSVisitor visitor) {
        visitor.visitPSBinder(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof PSVisitor) accept((PSVisitor) visitor);
        else super.accept(visitor);
    }



    @Override
    @NotNull
    public List<PSIdentifier> getIdentifierList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, PSIdentifier.class);
    }

    @Override
    @NotNull
    public List<PSBinder> getBinderList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, PSBinder.class);
    }

}
