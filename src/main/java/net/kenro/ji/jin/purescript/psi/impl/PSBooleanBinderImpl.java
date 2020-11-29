package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSBooleanBinder;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSBooleanBinderImpl extends PSPsiElement implements PSBooleanBinder {

    public PSBooleanBinderImpl(final ASTNode node) {
        super(node);
    }

}
