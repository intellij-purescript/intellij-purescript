package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSIdentifier;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSIdentifierImpl extends PSNamedElementImpl implements PSIdentifier {

    public PSIdentifierImpl(ASTNode node){
        super(node);
//        System.out.print("  IN THE PSIDENTIFIER IMPL  ");
    }

    public void accept(@NotNull PSVisitor visitor) {
        visitor.visitPSIdentifier(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof PSVisitor) accept((PSVisitor)visitor);
        else super.accept(visitor);
    }

    public String getName() {
        return PSPsiImplUtil.getName(this);
    }

    public PsiElement getNameIdentifier() {
        return PSPsiImplUtil.getNameIdentifier(this);
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        return null;
    }
}
