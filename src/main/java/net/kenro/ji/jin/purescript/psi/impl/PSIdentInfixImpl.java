package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSIdentInfix;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSIdentInfixImpl extends PSPsiElement implements PSIdentInfix {

    public PSIdentInfixImpl(final ASTNode node) {
        super(node);
    }

}
