package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSClassName;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSClassNameImpl extends PSPsiElement implements PSClassName {

    public PSClassNameImpl(final ASTNode node) {
        super(node);
    }

}
