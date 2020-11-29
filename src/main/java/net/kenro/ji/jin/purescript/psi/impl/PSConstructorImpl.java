package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSConstructor;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSConstructorImpl extends PSPsiElement implements PSConstructor {

    public PSConstructorImpl(final ASTNode node) {
        super(node);
    }

}
