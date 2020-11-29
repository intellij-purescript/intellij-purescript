package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSBinderAtom;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSBinderAtomImpl extends PSPsiElement implements PSBinderAtom {

    public PSBinderAtomImpl(final ASTNode node) {
        super(node);
    }

}
