package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSRowKind;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSRowKindImpl extends PSPsiElement implements PSRowKind {

    public PSRowKindImpl(final ASTNode node) {
        super(node);
    }

}
