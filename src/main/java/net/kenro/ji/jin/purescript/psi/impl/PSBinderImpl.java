package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import net.kenro.ji.jin.purescript.psi.PSBinder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PSBinderImpl extends PSPsiElement implements PSBinder {

    public PSBinderImpl(final ASTNode node) {
        super(node);
    }

    @Override
    @NotNull
    public List<PSIdentifierImpl> getIdentifierList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, PSIdentifierImpl.class);
    }

    @Override
    @NotNull
    public List<PSBinder> getBinderList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, PSBinder.class);
    }

}
