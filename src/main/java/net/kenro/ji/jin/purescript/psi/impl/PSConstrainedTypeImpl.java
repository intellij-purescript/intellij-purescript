package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSConstrainedType;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSConstrainedTypeImpl extends PSPsiElement implements PSConstrainedType {

    public PSConstrainedTypeImpl(final ASTNode node) {
        super(node);
    }

}
