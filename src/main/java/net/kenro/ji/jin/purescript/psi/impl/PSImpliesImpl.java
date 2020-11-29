package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSImplies;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSImpliesImpl extends PSPsiElement implements PSImplies {

    public PSImpliesImpl(final ASTNode node) {
        super(node);
    }

}
