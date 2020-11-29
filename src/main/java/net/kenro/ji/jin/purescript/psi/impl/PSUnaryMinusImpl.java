package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSUnaryMinus;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSUnaryMinusImpl extends PSPsiElement implements PSUnaryMinus {

    public PSUnaryMinusImpl(final ASTNode node) {
        super(node);
    }

}
