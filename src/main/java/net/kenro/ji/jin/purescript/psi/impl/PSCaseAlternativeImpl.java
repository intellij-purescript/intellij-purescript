package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSCaseAlternative;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSCaseAlternativeImpl extends PSPsiElement implements PSCaseAlternative {

    public PSCaseAlternativeImpl(final ASTNode node) {
        super(node);
    }

}
