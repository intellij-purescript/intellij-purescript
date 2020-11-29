package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSNumericLiteral;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSNumericLiteralImpl extends PSPsiElement implements PSNumericLiteral {

    public PSNumericLiteralImpl(final ASTNode node) {
        super(node);
    }

}
