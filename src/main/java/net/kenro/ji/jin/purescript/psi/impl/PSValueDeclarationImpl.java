package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSValueDeclaration;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSValueDeclarationImpl extends PSPsiElement implements PSValueDeclaration {

    public PSValueDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
