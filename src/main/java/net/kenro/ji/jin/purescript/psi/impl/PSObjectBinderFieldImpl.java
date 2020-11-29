package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSObjectBinderField;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSObjectBinderFieldImpl extends PSPsiElement implements PSObjectBinderField {

    public PSObjectBinderFieldImpl(final ASTNode node) {
        super(node);
    }

}
