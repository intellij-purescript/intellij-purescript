package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSFunKind;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSFunKindImpl extends PSPsiElement implements PSFunKind {

    public PSFunKindImpl(final ASTNode node) {
        super(node);
    }

}
