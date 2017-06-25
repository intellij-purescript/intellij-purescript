package net.kenro.ji.jin.purescript.psi;


import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PSBinder extends PsiElement {

    @NotNull
    List<PSIdentifier> getIdentifierList();

    @NotNull
    List<PSBinder> getBinderList();

}
