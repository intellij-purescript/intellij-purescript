package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSGenericIdentifier;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSGenericIdentifierImpl extends PSPsiElement implements PSGenericIdentifier {

    public PSGenericIdentifierImpl(final ASTNode node) {
        super(node);
    }

}
