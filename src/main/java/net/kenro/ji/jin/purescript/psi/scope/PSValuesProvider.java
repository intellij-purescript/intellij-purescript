//package net.kenro.ji.jin.purescript.psi.scope;
//
//import com.intellij.psi.PsiElement;
//import net.kenro.ji.jin.purescript.file.PSFile;
//import net.kenro.ji.jin.purescript.psi.PSIdentifier;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.Stack;
//import java.util.function.Predicate;
//
//class PSValuesProvider
//{
//    private PsiElement elem;
//    private Stack<ElmPattern> patterns = new Stack<>();
//    private Stack<PSIdentifier> ids = new Stack<>();
//
//    PSValuesProvider(PsiElement elem) {
//        this.elem = elem;
//    }
//
//    Optional<PSIdentifier> nextId() {
//        if (!this.ids.isEmpty()) {
//            return Optional.of(ids.pop());
//        }
//
//        if (!this.patterns.isEmpty()) {
//            ElmPsiImplUtil.getDeclarationsFromPattern(this.patterns.pop())
//                    .forEach(ids::add);
//            return nextId();
//        }
//
//        if (this.elem == null || this.elem instanceof ElmPattern) {
//            return Optional.empty();
//        }
//
//        this.elem = this.gatherIdsFromCurrentElement();
//
//        return nextId();
//    }
//
//    private PsiElement gatherIdsFromCurrentElement() {
//        Arrays.stream(this.elem.getChildren())
//                .filter(c -> c instanceof ElmPattern)
//                .map(p -> (ElmPattern)p)
//                .forEach(this.patterns::add);
//
//        if (this.elem instanceof ElmValueDeclarationBase) {
//            ElmValueDeclarationBase declaration = (ElmValueDeclarationBase) this.elem;
//            Optional.ofNullable(declaration.getFunctionDeclarationLeft())
//                    .ifPresent(this::gatherDeclarations);
//            Optional.ofNullable(declaration.getOperatorDeclarationLeft())
//                    .ifPresent(this::gatherDeclarations);
//        } else if (this.elem instanceof ElmLetIn) {
//            this.gatherValueDeclarations();
//        } else if (this.elem instanceof PSFile) {
//            this.gatherEffects();
//            this.gatherValueDeclarations();
//            this.gatherDeclarationsFromOtherFiles();
//            return null;
//        }
//
//        return this.elem.getParent();
//    }
//
//    private void gatherDeclarations(ElmFunctionDeclarationLeft elem) {
//        this.ids.push(elem.getLowerCaseId());
//        this.patterns.addAll(elem.getPatternList());
//    }
//
//    private void gatherValueDeclarations() {
//        this.gatherValueDeclarations((ElmWithValueDeclarations) this.elem);
//    }
//
//    private void gatherEffects() {
//        ((ElmFile) this.elem).getModuleDeclaration()
//                .flatMap(e -> Optional.ofNullable(e.getRecord()))
//                .ifPresent(e -> gatherEffects(e.getFieldList()));
//    }
//
//    private void gatherEffects(List<ElmField> effects) {
//        effects.stream()
//                .map(ElmField::getLowerCaseId)
//                .forEach(this.ids::push);
//    }
//
//    private void gatherValueDeclarations(ElmWithValueDeclarations element) {
//        Arrays.stream(element.getChildren())
//                .filter(c -> c instanceof ElmValueDeclarationBase)
//                .forEach(d -> {
//                    PsiElement child = d.getFirstChild();
//                    if (child instanceof ElmPattern) {
//                        this.patterns.add((ElmPattern) child);
//                    } else if (child instanceof ElmFunctionDeclarationLeft) {
//                        this.ids.push(((ElmFunctionDeclarationLeft) child).getLowerCaseId());
//                    }
//                });
//        Arrays.stream(element.getChildren())
//                .filter(e -> e instanceof ElmTypeAnnotation)
//                .map(e -> (ElmTypeAnnotation) e)
//                .forEach(typeAnnotation -> {
//                    if (typeAnnotation.isPortAnnotation()) {
//                        Optional.ofNullable(typeAnnotation.getLowerCaseId())
//                                .ifPresent(id -> this.ids.push(id));
//                    }
//                });
//    }
//
//    private static boolean startsWithPort(PsiElement element) {
//        return isElementOfType(element, ElmTypes.PORT);
//    }
//
//    private void gatherDeclarationsFromOtherFiles() {
//        ((ElmFile) this.elem).getImportClauses().stream()
//                .filter(e -> e.getExposingClause() != null)
//                .forEach(this::gatherDeclarationsFromOtherFile);
//        gatherDeclarationsFromOtherFile(ElmCoreLibrary.BASICS_MODULE, x -> true);
//    }
//
//    private void gatherDeclarationsFromOtherFile(@NotNull ElmImportClause elem) {
//        Predicate<PSIdentifier> filter = Optional.ofNullable(elem.getExposingClause())
//                .map(ElmExposingBase::getLowerCaseFilter)
//                .orElse(x -> false);
//        Optional.ofNullable(elem.getModuleName())
//                .ifPresent(name -> gatherDeclarationsFromOtherFile(name.getText(), filter));
//    }
//
//    private void gatherDeclarationsFromOtherFile(@NotNull String moduleName, Predicate<PSIdentifier> filter) {
//        ElmModuleIndex.getFilesByModuleName(moduleName, this.elem.getProject())
//                .forEach(f -> gatherDeclarationsFromOtherFile(f, filter));
//    }
//
//    private void gatherDeclarationsFromOtherFile(PSFile file, Predicate<PSIdentifier> filter) {
//        file.getExposedValues()
//                .filter(filter)
//                .forEach(this.ids::add);
//    }
//
//    private void gatherDeclarations(ElmWithPatternList elem) {
//        this.patterns.addAll(elem.getPatternList());
//    }
//
//
//}
//
