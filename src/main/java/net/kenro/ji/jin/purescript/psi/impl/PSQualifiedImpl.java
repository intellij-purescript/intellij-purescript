package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSQualified;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSQualifiedImpl extends PSPsiElement implements PSQualified {

    public PSQualifiedImpl(final ASTNode node) {
        super(node);
    }

}
