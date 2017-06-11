//package net.kenro.ji.jin.purescript.psi.scope;
//
//import com.intellij.psi.PsiElement;
//import net.kenro.ji.jin.purescript.file.PSFile;
//import net.kenro.ji.jin.purescript.psi.PSIdentifier;
//import net.kenro.ji.jin.purescript.psi.PSProperName;
//
//import java.util.Optional;
//import java.util.stream.Stream;
//
//public class PSScope {
//    public static Stream<Optional<PSIdentifier>> scopeFor(PSIdentifier elem) {
//        return provideValuesFor(elem.getParent());
//    }
//
//    public static Stream<Optional<PSIdentifier>> scopeFor(PSFile file) {
//        return provideValuesFor(file);
//    }
//
//    public static Stream<Optional<PSIdentifier>> recordFieldsFor(PSFile file) {
//        PSRecordFieldsProvider p = new PSRecordFieldsProvider(file);
//        return Stream.generate(p::nextField);
//    }
//
//    public static Stream<Optional<PSProperName>> typesFor(PSFile file) {
//        PSTypesProvider p = new PSTypesProvider(file);
//        return Stream.generate(p::nextType);
//    }
//
//    private static Stream<Optional<PSIdentifier>> provideValuesFor(PsiElement element) {
//        PSValuesProvider p = new PSValuesProvider(element);
//        return Stream.generate(p::nextId);
//    }
//}
//
