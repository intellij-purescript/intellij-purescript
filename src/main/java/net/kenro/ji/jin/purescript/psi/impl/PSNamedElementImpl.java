package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class PSNamedElementImpl extends PSPsiElement implements PSNamedElement {
    public PSNamedElementImpl(@NotNull ASTNode node){
        super(node);
    }
}
