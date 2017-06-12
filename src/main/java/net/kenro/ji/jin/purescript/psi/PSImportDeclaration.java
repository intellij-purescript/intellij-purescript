package net.kenro.ji.jin.purescript.psi;


import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.*;

import java.util.List;

public interface PSImportDeclaration extends PsiElement {

    @Nullable
    List<String> getExposingClause();

    String getModuleName();

}
