package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSModule;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PSModuleImpl extends PSPsiElement implements PSModule {

    public PSModuleImpl(final ASTNode node) {
        super(node);
    }

    public Map<String, PSValueDeclarationImpl> getTopLevelValueDeclarations() {
        return Arrays
            .stream(this.getChildren())
            .filter(psi -> psi instanceof  PSValueDeclarationImpl)
            .map(psi -> (PSValueDeclarationImpl) psi)
            .collect(Collectors.toMap(
                psValueDeclaration -> psValueDeclaration.getName(),
                psValueDeclaration -> psValueDeclaration
            ));
    }

}
