package net.kenro.ji.jin.purescript.psi.scope;

import net.kenro.ji.jin.purescript.file.PSFile;
import net.kenro.ji.jin.purescript.psi.PSImportDeclaration;
import net.kenro.ji.jin.purescript.psi.impl.PSIdentifierImpl;

import java.util.Optional;
import java.util.Stack;

import static net.kenro.ji.jin.purescript.psi.scope.PSTypesProvider.TypesProvidingPhase.*;


public class PSRecordFieldsProvider {
    private final PSFile file;

    private final Stack<PSIdentifierImpl> fields = new Stack<>();
    private final Stack<PSImportDeclaration> importClauses = new Stack<>();
    private final Stack<String> implicitImports;
    private PSTypesProvider.TypesProvidingPhase phase = CURRENT_FILE;

    PSRecordFieldsProvider(final PSFile file) {
        this.file = file;
        this.implicitImports =  PSCoreLibrary.getImplicitImportsCopy();
    }

    Optional<PSIdentifierImpl> nextField() {
        if (!this.fields.isEmpty()) {
            return Optional.of(this.fields.pop());
        }

        switch (this.phase) {
            case CURRENT_FILE:
                gatherFieldsFromCurrentFile();
                return this.nextField();
//            case IMPORTED_FILES:
//                gatherTypesFromImport();
//                return this.nextField();
//            case IMPLICIT_IMPORTS:
//                gatherTypesFromImplicitImport();
//                return this.nextField();
            case FINISHED:
                return Optional.empty();
            default:
                throw new RuntimeException("Unhandled phase " + this.phase);
        }
    }

    private void gatherFieldsFromCurrentFile() {
//        this.file.getRecordFields()
//                .forEach(this.fields::push);
        this.file.getImportClauses()
                .forEach(this.importClauses::push);
        this.updatePhase();
    }

    private void updatePhase() {
        this.phase = this.implicitImports.isEmpty()
                ? FINISHED
                : this.importClauses.isEmpty()
                ? IMPLICIT_IMPORTS
                : IMPORTED_FILES;
    }

//    private void gatherTypesFromImport() {
//        ElmImportClause importClause = this.importClauses.pop();
//        this.gatherTypesFromFile(importClause.getModuleName().getText());
//        updatePhase();
//    }
//
//    private void gatherTypesFromFile(String moduleName) {
//        ElmModuleIndex.getFilesByModuleName(moduleName, this.file.getProject())
//                .forEach(f -> f.getRecordFields().forEach(this.fields::push));
//    }
//
//    private void gatherTypesFromImplicitImport() {
//        String module = this.implicitImports.pop();
//        this.gatherTypesFromFile(module);
//        updatePhase();
//    }
}
