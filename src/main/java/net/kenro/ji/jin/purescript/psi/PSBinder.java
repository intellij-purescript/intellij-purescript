package net.kenro.ji.jin.purescript.psi;


import com.intellij.psi.PsiElement;
import net.kenro.ji.jin.purescript.psi.impl.PSIdentifierImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PSBinder extends PsiElement {

    @NotNull
    List<PSIdentifierImpl> getIdentifierList();

    @NotNull
    List<PSBinder> getBinderList();

}
