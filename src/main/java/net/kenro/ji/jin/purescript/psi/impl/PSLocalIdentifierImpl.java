package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSLocalIdentifier;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSLocalIdentifierImpl extends PSPsiElement implements PSLocalIdentifier {

    public PSLocalIdentifierImpl(final ASTNode node) {
        super(node);
    }

}
