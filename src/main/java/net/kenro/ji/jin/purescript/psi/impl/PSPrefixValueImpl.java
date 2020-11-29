package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSPrefixValue;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSPrefixValueImpl extends PSPsiElement implements PSPrefixValue {

    public PSPrefixValueImpl(final ASTNode node) {
        super(node);
    }

}
