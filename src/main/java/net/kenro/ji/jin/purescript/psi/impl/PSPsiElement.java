package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public abstract class PSPsiElement extends ASTWrapperPsiElement {
    public PSPsiElement(@NotNull ASTNode node) {
        super(node);
    }
}
