package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSTypeConstructor;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSTypeConstructorImpl extends PSPsiElement implements PSTypeConstructor {

    public PSTypeConstructorImpl(final ASTNode node) {
        super(node);
    }

}
