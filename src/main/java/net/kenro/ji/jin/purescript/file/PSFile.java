package net.kenro.ji.jin.purescript.file;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.util.PsiTreeUtil;
import net.kenro.ji.jin.purescript.PSLanguage;
import net.kenro.ji.jin.purescript.psi.PSImportDeclaration;
import net.kenro.ji.jin.purescript.psi.PSProperName;
import net.kenro.ji.jin.purescript.psi.impl.PSProperNameImpl;
import net.kenro.ji.jin.purescript.util.TypeFilter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class PSFile extends PsiFileBase {
    public PSFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, PSLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return PSFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Purescript File";
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }

    public List<PSImportDeclaration> getImportClauses() {
        LinkedList<PSImportDeclaration> psImportDeclarations = new LinkedList<>(PsiTreeUtil.findChildrenOfType(this, PSImportDeclaration.class));
        System.out.println("GET IMPORT CLAUSES");
        System.out.println(psImportDeclarations);
        return psImportDeclarations;
    }

//    @NotNull
//    public Stream<PSProperName> getInternalTypes() {
//        return this.getTypes(TypeFilter.always(true));
//    }

//    @NotNull
//    private Stream<PSProperName> getTypes(TypeFilter typeFilter) {
//        return Stream.concat(
//                this.getDataTypes(typeFilter),
////                this.getUnionTypesAndMembers(typeFilter)
//        );
//    }

//    @NotNull
//    private Stream<PSProperName> getDataTypes(TypeFilter typeFilter) {
//        return Arrays.stream(this.getChildren())
//                .filter(e -> e instanceof DataDeclaration)
//                .map(e -> ((ElmTypeAliasDeclaration) e).getUpperCaseId())
//                .filter(e -> typeFilter.testType(e.getText()));
//    }
}
