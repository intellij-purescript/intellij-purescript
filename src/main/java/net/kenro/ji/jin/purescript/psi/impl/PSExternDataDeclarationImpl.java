package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSExternDataDeclaration;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSExternDataDeclarationImpl extends PSPsiElement implements PSExternDataDeclaration {

    public PSExternDataDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
