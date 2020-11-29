package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSDoNotationBind;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSDoNotationBindImpl extends PSPsiElement implements PSDoNotationBind {

    public PSDoNotationBindImpl(final ASTNode node) {
        super(node);
    }

}
