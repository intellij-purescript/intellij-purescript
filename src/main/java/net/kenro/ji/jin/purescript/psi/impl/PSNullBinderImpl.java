package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSNullBinder;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSNullBinderImpl extends PSPsiElement implements PSNullBinder {

    public PSNullBinderImpl(final ASTNode node) {
        super(node);
    }

}
