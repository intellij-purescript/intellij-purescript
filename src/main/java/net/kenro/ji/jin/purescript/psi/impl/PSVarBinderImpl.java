package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSVarBinder;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSVarBinderImpl extends PSPsiElement implements PSVarBinder {

    public PSVarBinderImpl(final ASTNode node) {
        super(node);
    }

}
