package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSDoNotationValue;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSDoNotationValueImpl extends PSPsiElement implements PSDoNotationValue {

    public PSDoNotationValueImpl(final ASTNode node) {
        super(node);
    }

}
