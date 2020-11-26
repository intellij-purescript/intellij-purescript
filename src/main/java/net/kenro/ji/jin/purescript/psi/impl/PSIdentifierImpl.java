package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSIdentifier;
import net.kenro.ji.jin.purescript.psi.PSProperName;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSIdentifierImpl extends PSNamedElementImpl implements PSIdentifier {

    public PSIdentifierImpl(final ASTNode node){
        super(node);
//        System.out.print("  IN THE PSIDENTIFIER IMPL  ");
    }

    public void accept(@NotNull final PSVisitor visitor) {
        visitor.visitPSIdentifier(this);
    }

    public void accept(@NotNull final PsiElementVisitor visitor) {
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
    public PsiElement setName(@NotNull final String name) {
        return null;
    }
}
