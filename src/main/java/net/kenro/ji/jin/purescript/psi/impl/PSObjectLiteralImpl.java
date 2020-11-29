package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSObjectLiteral;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSObjectLiteralImpl extends PSPsiElement implements PSObjectLiteral {

    public PSObjectLiteralImpl(final ASTNode node) {
        super(node);
    }

}
