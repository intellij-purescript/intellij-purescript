package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSJSRaw;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSJSRawImpl extends PSPsiElement implements PSJSRaw {

    public PSJSRawImpl(final ASTNode node) {
        super(node);
    }

}
