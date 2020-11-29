package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSValueRef;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSValueRefImpl extends PSPsiElement implements PSValueRef {

    public PSValueRefImpl(final ASTNode node) {
        super(node);
    }

}
