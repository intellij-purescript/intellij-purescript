package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSStringLiteral;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSStringLiteralImpl extends PSPsiElement implements PSStringLiteral {

    public PSStringLiteralImpl(final ASTNode node) {
        super(node);
    }

}
