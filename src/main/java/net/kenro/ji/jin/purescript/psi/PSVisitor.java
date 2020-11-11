package net.kenro.ji.jin.purescript.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class PSVisitor extends PsiElementVisitor {

    public void visitPsiElement(@NotNull PsiElement o) {
        visitElement(o);
    }

    public void visitNamedElement(@NotNull PSNamedElement o) {
        visitPsiElement(o);
    }

    public void visitPSProperName(@NotNull PSProperName o) {
        visitNamedElement(o);
    }

    public void visitPSImportDeclaration(@NotNull PSImportDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSDataDeclaration(@NotNull PSDataDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSBinder(@NotNull PSBinder o) {
        visitPsiElement(o);
    }


    public void visitPSIdentifier(@NotNull PSIdentifier o) {
        visitNamedElement(o);
    }


    public void visitPSAbs(PSAbs o) {
        visitPsiElement(o);
    }

    public void visitPSProgram(PSProgram o) {
        visitPsiElement(o);
    }

    public void visitPSModule(PSModule o) {
        visitPsiElement(o);
    }

    public void visitPSStar(PSStar o) {
        visitPsiElement(o);
    }

    public void visitPSBang(PSBang o) {
        visitPsiElement(o);
    }


    public void visitPSRowKind(PSRowKind o) {
        visitPsiElement(o);
    }

    public void visitPSFunKind(PSFunKind o) {
        visitPsiElement(o);
    }

    public void visitPSQualified(PSQualified o) {
        visitPsiElement(o);
    }

    public void visitPSType(PSType o) {
        visitPsiElement(o);
    }

    public void visitPSTypeArgs(PSTypeArgs o) {
        visitPsiElement(o);
    }

    public void visitPSTypeAnnotationName(PSTypeAnnotationName o) {
        visitPsiElement(o);
    }

    public void visitPSForAll(PSForAll o) {
        visitPsiElement(o);
    }

    public void visitPSConstrainedType(PSConstrainedType o) {
        visitPsiElement(o);
    }

    public void visitPSRow(PSRow o) {
        visitPsiElement(o);
    }

    public void visitPSObjectType(PSObjectType o) {
        visitPsiElement(o);
    }

    public void visitPSTypeVar(PSTypeVar o) {
        visitPsiElement(o);
    }

    public void visitPSTypeConstructor(PSTypeConstructor o) {
        visitPsiElement(o);
    }

    public void visitPSTypeAtom(PSTypeAtom o) {
        visitPsiElement(o);
    }

    public void visitPSGenericIdentifier(PSGenericIdentifier o) {
        visitPsiElement(o);
    }

    public void visitPSLocalIdentifier(PSLocalIdentifier o) {
        visitPsiElement(o);
    }

    public void visitPSTypeDeclaration(PSTypeDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSTypeSynonymDeclaration(PSTypeSynonymDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSValueDeclaration(PSValueDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSExternDataDeclaration(PSExternDataDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSExternInstanceDeclaration(PSExternInstanceDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSExternDeclaration(PSExternDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSFixityDeclaration(PSFixityDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSPositionedDeclarationRef(PSPositionedDeclarationRef o) {
        visitPsiElement(o);
    }

    public void visitPSTypeClassDeclaration(PSTypeClassDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSTypeInstanceDeclaration(PSTypeInstanceDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSNewTypeDeclaration(PSNewTypeDeclaration o) {
        visitPsiElement(o);
    }

    public void visitPSGuard(PSGuard o) {
        visitPsiElement(o);
    }

    public void visitPSNullBinder(PSNullBinder o) {
        visitPsiElement(o);
    }

    public void visitPSStringBinder(PSStringBinder o) {
        visitPsiElement(o);
    }

    public void visitPSBooleanBinder(PSBooleanBinder o) {
        visitPsiElement(o);
    }

    public void visitPSNumberBinder(PSNumberBinder o) {
        visitPsiElement(o);
    }

    public void visitPSNamedBinder(PSNamedBinder o) {
        visitPsiElement(o);
    }

    public void visitPSVarBinder(PSVarBinder o) {
        visitPsiElement(o);
    }

    public void visitPSConstructorBinder(PSConstructorBinder o) {
        visitPsiElement(o);
    }

    public void visitPSObjectBinder(PSObjectBinder o) {
        visitPsiElement(o);
    }

    public void visitPSObjectBinderField(PSObjectBinderField o) {
        visitPsiElement(o);
    }

    public void visitPSBinderAtom(PSBinderAtom o) {
        visitPsiElement(o);
    }

    public void visitPSValueRef(PSValueRef o) {
        visitPsiElement(o);
    }

    public void visitPSBooleanLiteral(PSBooleanLiteral o) {
        visitPsiElement(o);
    }

    public void visitPSNumericLiteral(PSNumericLiteral o) {
        visitPsiElement(o);
    }

    public void visitPSStringLiteral(PSStringLiteral o) {
        visitPsiElement(o);
    }

    public void visitPSArrayLiteral(PSArrayLiteral o) {
        visitPsiElement(o);
    }

    public void visitPSObjectLiteral(PSObjectLiteral o) {
        visitPsiElement(o);
    }

    public void visitPSIdentInfix(PSIdentInfix o) {
        visitPsiElement(o);
    }

    public void visitPSVar(PSVar o) {
        visitPsiElement(o);
    }

    public void visitPSConstructor(PSConstructor o) {
        visitPsiElement(o);
    }

    public void visitPSCase(PSCase o) {
        visitPsiElement(o);
    }

    public void visitPSCaseAlternative(PSCaseAlternative o) {
        visitPsiElement(o);
    }

    public void visitPSIfThenElse(PSIfThenElse o) {
        visitPsiElement(o);
    }

    public void visitPSLet(PSLet o) {
        visitPsiElement(o);
    }

    public void visitPSParens(PSParens o) {
        visitPsiElement(o);
    }

    public void visitPSUnaryMinus(PSUnaryMinus o) {
        visitPsiElement(o);
    }

    public void visitPSPrefixValue(PSPrefixValue o) {
        visitPsiElement(o);
    }

    public void visitPSAccessor(PSAccessor o) {
        visitPsiElement(o);
    }

    public void visitPSDoNotationLet(PSDoNotationLet o) {
        visitPsiElement(o);
    }

    public void visitPSDoNotationBind(PSDoNotationBind o) {
        visitPsiElement(o);
    }

    public void visitPSDoNotationValue(PSDoNotationValue o) {
        visitPsiElement(o);
    }

    public void visitPSValue(PSValue o) {
        visitPsiElement(o);
    }

    public void visitPSFixity(PSFixity o) {
        visitPsiElement(o);
    }

    public void visitPSJSRaw(PSJSRaw o) {
        visitPsiElement(o);
    }

    public void visitPSModuleName(PSModuleName o) {
        visitPsiElement(o);
    }

    public void visitPSImportModuleName(PSImportModuleName o) {
        visitPsiElement(o);
    }

    public void visitPSQualifiedModuleName
            (PSQualifiedModuleName o) {
        visitPsiElement(o);
    }

    public void visitPSClassName(PSClassName o) {
        visitPsiElement(o);
    }

    public void visitPSImplies(PSImplies o) {
        visitPsiElement(o);
    }

    public void visitPSTypeHole(PSTypeHole o) {
        visitPsiElement(o);
    }

}
