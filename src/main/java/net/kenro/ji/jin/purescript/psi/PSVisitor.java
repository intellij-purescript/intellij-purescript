package net.kenro.ji.jin.purescript.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.impl.PSAbsImpl;
import org.jetbrains.annotations.NotNull;

public class PSVisitor extends PsiElementVisitor {

    public void visitPsiElement(@NotNull final PsiElement o) {
        visitElement(o);
    }

    public void visitNamedElement(@NotNull final PSNamedElement o) {
        visitPsiElement(o);
    }

    public void visitPSProperName(@NotNull final PSProperName o) {
        visitNamedElement(o);
    }

    public void visitPSImportDeclaration(@NotNull final PSImportDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSDataDeclaration(@NotNull final PSDataDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSBinder(@NotNull final PSBinder o) {
        visitPsiElement(o);
    }


    public void visitPSIdentifier(@NotNull final PSIdentifier o) {
        visitNamedElement(o);
    }


    public void visitPSAbs(final PSAbs o) {
        visitPsiElement(o);
    }

    public void visitPSProgram(final PSProgram o) {
        visitPsiElement(o);
    }

    public void visitPSModule(final PSModule o) {
        visitPsiElement(o);
    }

    public void visitPSStar(final PSStar o) {
        visitPsiElement(o);
    }

    public void visitPSBang(final PSBang o) {
        visitPsiElement(o);
    }


    public void visitPSRowKind(final PSRowKind o) {
        visitPsiElement(o);
    }

    public void visitPSFunKind(final PSFunKind o) {
        visitPsiElement(o);
    }

    public void visitPSQualified(final PSQualified o) {
        visitPsiElement(o);
    }

    public void visitPSType(final PSType o) {
        visitPsiElement(o);
    }

    public void visitPSTypeArgs(final PSTypeArgs o) {
        visitPsiElement(o);
    }

    public void visitPSTypeAnnotationName(final PSTypeAnnotationName o) {
        visitPsiElement(o);
    }

    public void visitPSForAll(final PSForAll o) {
        visitPsiElement(o);
    }

    public void visitPSConstrainedType(final PSConstrainedType o) {
        visitPsiElement(o);
    }

    public void visitPSRow(final PSRow o) {
        visitPsiElement(o);
    }

    public void visitPSObjectType(final PSObjectType o) {
        visitPsiElement(o);
    }

    public void visitPSTypeVar(final PSTypeVar o) {
        visitPsiElement(o);
    }

    public void visitPSTypeConstructor(final PSTypeConstructor o) {
        visitPsiElement(o);
    }

    public void visitPSTypeAtom(final PSTypeAtom o) {
        visitPsiElement(o);
    }

    public void visitPSGenericIdentifier(final PSGenericIdentifier o) {
        visitPsiElement(o);
    }

    public void visitPSLocalIdentifier(final PSLocalIdentifier o) {
        visitPsiElement(o);
    }

    public void visitPSTypeDeclaration(final PSTypeDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSTypeSynonymDeclaration(final PSTypeSynonymDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSValueDeclaration(final PSValueDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSExternDataDeclaration(final PSExternDataDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSExternInstanceDeclaration(final PSExternInstanceDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSExternDeclaration(final PSExternDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSFixityDeclaration(final PSFixityDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSPositionedDeclarationRef(final PSPositionedDeclarationRef o) {
        visitPsiElement(o);
    }

    public void visitPSTypeClassDeclaration(final PSTypeClassDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSTypeInstanceDeclaration(final PSTypeInstanceDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSNewTypeDeclaration(final PSNewTypeDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSGuard(final PSGuard o) {
        visitPsiElement(o);
    }

    public void visitPSNullBinder(final PSNullBinder o) {
        visitPsiElement(o);
    }

    public void visitPSStringBinder(final PSStringBinder o) {
        visitPsiElement(o);
    }

    public void visitPSBooleanBinder(final PSBooleanBinder o) {
        visitPsiElement(o);
    }

    public void visitPSNumberBinder(final PSNumberBinder o) {
        visitPsiElement(o);
    }

    public void visitPSNamedBinder(final PSNamedBinder o) {
        visitPsiElement(o);
    }

    public void visitPSVarBinder(final PSVarBinder o) {
        visitPsiElement(o);
    }

    public void visitPSConstructorBinder(final PSConstructorBinder o) {
        visitPsiElement(o);
    }

    public void visitPSObjectBinder(final PSObjectBinder o) {
        visitPsiElement(o);
    }

    public void visitPSObjectBinderField(final PSObjectBinderField o) {
        visitPsiElement(o);
    }

    public void visitPSBinderAtom(final PSBinderAtom o) {
        visitPsiElement(o);
    }

    public void visitPSValueRef(final PSValueRef o) {
        visitPsiElement(o);
    }

    public void visitPSBooleanLiteral(final PSBooleanLiteral o) {
        visitPsiElement(o);
    }

    public void visitPSNumericLiteral(final PSNumericLiteral o) {
        visitPsiElement(o);
    }

    public void visitPSStringLiteral(final PSStringLiteral o) {
        visitPsiElement(o);
    }

    public void visitPSArrayLiteral(final PSArrayLiteral o) {
        visitPsiElement(o);
    }

    public void visitPSObjectLiteral(final PSObjectLiteral o) {
        visitPsiElement(o);
    }

    public void visitPSIdentInfix(final PSIdentInfix o) {
        visitPsiElement(o);
    }

    public void visitPSVar(final PSVar o) {
        visitPsiElement(o);
    }

    public void visitPSConstructor(final PSConstructor o) {
        visitPsiElement(o);
    }

    public void visitPSCase(final PSCase o) {
        visitPsiElement(o);
    }

    public void visitPSCaseAlternative(final PSCaseAlternative o) {
        visitPsiElement(o);
    }

    public void visitPSIfThenElse(final PSIfThenElse o) {
        visitPsiElement(o);
    }

    public void visitPSLet(final PSLet o) {
        visitPsiElement(o);
    }

    public void visitPSParens(final PSParens o) {
        visitPsiElement(o);
    }

    public void visitPSUnaryMinus(final PSUnaryMinus o) {
        visitPsiElement(o);
    }

    public void visitPSPrefixValue(final PSPrefixValue o) {
        visitPsiElement(o);
    }

    public void visitPSAccessor(final PSAccessor o) {
        visitPsiElement(o);
    }

    public void visitPSDoNotationLet(final PSDoNotationLet o) {
        visitPsiElement(o);
    }

    public void visitPSDoNotationBind(final PSDoNotationBind o) {
        visitPsiElement(o);
    }

    public void visitPSDoNotationValue(final PSDoNotationValue o) {
        visitPsiElement(o);
    }

    public void visitPSValue(final PSValue o) {
        visitPsiElement(o);
    }

    public void visitPSFixity(final PSFixity o) {
        visitPsiElement(o);
    }

    public void visitPSJSRaw(final PSJSRaw o) {
        visitPsiElement(o);
    }

    public void visitPSModuleName(final PSModuleName o) {
        visitPsiElement(o);
    }

    public void visitPSImportModuleName(final PSImportModuleName o) {
        visitPsiElement(o);
    }

    public void visitPSQualifiedModuleName
            (final PSQualifiedModuleName o) {
        visitPsiElement(o);
    }

    public void visitPSClassName(final PSClassName o) {
        visitPsiElement(o);
    }

    public void visitPSImplies(final PSImplies o) {
        visitPsiElement(o);
    }

    public void visitPSTypeHole(final PSTypeHole o) {
        visitPsiElement(o);
    }

}
