package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
<<<<<<< HEAD
import net.kenro.ji.jin.purescript.psi.PSValueDeclaration;
=======
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;
>>>>>>> Inline empty interface

public class PSValueDeclarationImpl extends PSPsiElement {

    public PSValueDeclarationImpl(final ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        final PSIdentifierImpl identifier =
            this.findChildByClass(PSIdentifierImpl.class);
        return identifier.getName();
    }
}
