package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSProgram;

public class PSProgramImpl extends PSPsiElement implements PSProgram {

    public PSProgramImpl(final ASTNode node) {
        super(node);
    }

}
