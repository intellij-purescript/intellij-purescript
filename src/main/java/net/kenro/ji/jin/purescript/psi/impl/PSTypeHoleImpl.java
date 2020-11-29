package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSTypeHole;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSTypeHoleImpl extends PSPsiElement implements PSTypeHole {

    public PSTypeHoleImpl(final ASTNode node) {
        super(node);
    }

}
