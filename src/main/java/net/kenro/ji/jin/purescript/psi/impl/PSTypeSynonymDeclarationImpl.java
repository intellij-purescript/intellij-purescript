package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSTypeSynonymDeclaration;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSTypeSynonymDeclarationImpl extends PSPsiElement implements PSTypeSynonymDeclaration {

    public PSTypeSynonymDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
