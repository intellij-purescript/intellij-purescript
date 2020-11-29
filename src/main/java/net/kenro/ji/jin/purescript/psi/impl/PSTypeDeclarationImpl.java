package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSTypeDeclaration;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSTypeDeclarationImpl extends PSPsiElement implements PSTypeDeclaration {

    public PSTypeDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
