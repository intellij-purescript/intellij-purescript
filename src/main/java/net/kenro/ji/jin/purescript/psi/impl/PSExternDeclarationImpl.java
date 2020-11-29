package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSExternDeclaration;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSExternDeclarationImpl extends PSPsiElement implements PSExternDeclaration {

    public PSExternDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
