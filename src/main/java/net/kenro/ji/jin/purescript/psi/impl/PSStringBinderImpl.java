package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSStringBinder;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSStringBinderImpl extends PSPsiElement implements PSStringBinder {

    public PSStringBinderImpl(final ASTNode node) {
        super(node);
    }

}
