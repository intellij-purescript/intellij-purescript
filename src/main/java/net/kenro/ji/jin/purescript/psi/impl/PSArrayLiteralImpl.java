package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSArrayLiteral;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSArrayLiteralImpl extends PSPsiElement implements PSArrayLiteral {

    public PSArrayLiteralImpl(final ASTNode node) {
        super(node);
    }

}
