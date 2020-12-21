package net.kenro.ji.jin.purescript.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import net.kenro.ji.jin.purescript.file.PSFile;
import net.kenro.ji.jin.purescript.file.PSFileStubType;
import net.kenro.ji.jin.purescript.lexer.PSLexer;
import net.kenro.ji.jin.purescript.psi.PSElements;
import net.kenro.ji.jin.purescript.psi.PSTokens;
import net.kenro.ji.jin.purescript.psi.cst.PSASTWrapperElement;
import net.kenro.ji.jin.purescript.psi.impl.*;
import org.jetbrains.annotations.NotNull;

public class PSParserDefinition implements ParserDefinition, PSTokens {
    @NotNull
    @Override
    public Lexer createLexer(final Project project) {
        return new PSLexer();
    }

    @Override
    public PsiParser createParser(final Project project) {
        return new PureParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return PSFileStubType.INSTANCE;
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return TokenSet.EMPTY;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return TokenSet.EMPTY;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return kStrings;
    }

    @NotNull
    @Override
    public PsiElement createElement(final ASTNode node) {
        final IElementType type = node.getElementType();
        if ((type.equals(PSElements.ProperName)  ||
             type.equals(PSElements.Qualified)   ||
             type.equals(PSElements.pClassName)  ||
             type.equals(PSElements.pModuleName) ||
             type.equals(PSElements.importModuleName))) {
            return new PSProperNameImpl(node);
        } else if ((type.equals(PSElements.Identifier)    ||
                type.equals(PSElements.GenericIdentifier) ||
                type.equals(PSElements.TypeConstructor)   ||
                type.equals(PSElements.Constructor)       ||
                type.equals(PSElements.LocalIdentifier))) {
            return new PSIdentifierImpl(node);
        } else if (type.equals(PSElements.ImportDeclaration)) {
            return new PSImportDeclarationImpl(node);
        } else if (type.equals(PSElements.DataDeclaration)) {
            return new PSDataDeclarationImpl(node);
        } else if (type.equals(PSElements.Binder)) {
            return new PSBinderImpl(node);

        } else if (type.equals(PSElements.Program)) {
            return new PSProgramImpl(node);

        } else if (type.equals(PSElements.Module)) {
            return new PSModuleImpl(node);

        } else if (type.equals(PSElements.Star)) {
            return new PSStarImpl(node);

        } else if (type.equals(PSElements.Bang)) {
            return new PSBangImpl(node);

        } else if (type.equals(PSElements.RowKind)) {
            return new PSRowKindImpl(node);

        } else if (type.equals(PSElements.FunKind)) {
            return new PSFunKindImpl(node);

//        } else if (type.equals(PSElements.Qualified)) {
//            return new PSQualifiedImpl(node);

        } else if (type.equals(PSElements.Type)) {
            return new PSTypeImpl(node);

        } else if (type.equals(PSElements.TypeArgs)) {
            return new PSTypeArgsImpl(node);

        } else if (type.equals(PSElements.TypeAnnotationName)) {
            return new PSTypeAnnotationNameImpl(node);

        } else if (type.equals(PSElements.ForAll)) {
            return new PSForAllImpl(node);

        } else if (type.equals(PSElements.ConstrainedType)) {
            return new PSConstrainedTypeImpl(node);

        } else if (type.equals(PSElements.Row)) {
            return new PSRowImpl(node);

        } else if (type.equals(PSElements.ObjectType)) {
            return new PSObjectTypeImpl(node);

        } else if (type.equals(PSElements.TypeVar)) {
            return new PSTypeVarImpl(node);

//        } else if (type.equals(PSElements.TypeConstructor)) {
//            return new PSTypeConstructorImpl(node);

        } else if (type.equals(PSElements.TypeAtom)) {
            return new PSTypeAtomImpl(node);

        } else if (type.equals(PSElements.GenericIdentifier)) {
            return new PSGenericIdentifierImpl(node);

        } else if (type.equals(PSElements.LocalIdentifier)) {
            return new PSLocalIdentifierImpl(node);

        } else if (type.equals(PSElements.TypeDeclaration)) {
            return new PSTypeDeclarationImpl(node);

        } else if (type.equals(PSElements.TypeSynonymDeclaration)) {
            return new PSTypeSynonymDeclarationImpl(node);

        } else if (type.equals(PSElements.ValueDeclaration)) {
            return new PSValueDeclarationImpl(node);

        } else if (type.equals(PSElements.ExternDataDeclaration)) {
            return new PSExternDataDeclarationImpl(node);

        } else if (type.equals(PSElements.ExternInstanceDeclaration)) {
            return new PSExternInstanceDeclarationImpl(node);

        } else if (type.equals(PSElements.ExternDeclaration)) {
            return new PSExternDeclarationImpl(node);

        } else if (type.equals(PSElements.FixityDeclaration)) {
            return new PSFixityDeclarationImpl(node);

        } else if (type.equals(PSElements.PositionedDeclarationRef)) {
            return new PSPositionedDeclarationRefImpl(node);

        } else if (type.equals(PSElements.TypeClassDeclaration)) {
            return new PSTypeClassDeclarationImpl(node);

        } else if (type.equals(PSElements.TypeInstanceDeclaration)) {
            return new PSTypeInstanceDeclarationImpl(node);

        } else if (type.equals(PSElements.NewtypeDeclaration)) {
            return new PSNewTypeDeclarationImpl(node);

        } else if (type.equals(PSElements.Guard)) {
            return new PSGuardImpl(node);

        } else if (type.equals(PSElements.NullBinder)) {
            return new PSNullBinderImpl(node);

        } else if (type.equals(PSElements.StringBinder)) {
            return new PSStringBinderImpl(node);

        } else if (type.equals(PSElements.BooleanBinder)) {
            return new PSBooleanBinderImpl(node);

        } else if (type.equals(PSElements.NumberBinder)) {
            return new PSNumberBinderImpl(node);

        } else if (type.equals(PSElements.NamedBinder)) {
            return new PSNamedBinderImpl(node);

        } else if (type.equals(PSElements.VarBinder)) {
            return new PSVarBinderImpl(node);

        } else if (type.equals(PSElements.ConstructorBinder)) {
            return new PSConstructorBinderImpl(node);

        } else if (type.equals(PSElements.ObjectBinder)) {
            return new PSObjectBinderImpl(node);

        } else if (type.equals(PSElements.ObjectBinderField)) {
            return new PSObjectBinderFieldImpl(node);

        } else if (type.equals(PSElements.BinderAtom)) {
            return new PSBinderAtomImpl(node);

        } else if (type.equals(PSElements.Binder)) {
            return new PSBinderImpl(node);

        } else if (type.equals(PSElements.ValueRef)) {
            return new PSValueRefImpl(node);

        } else if (type.equals(PSElements.BooleanLiteral)) {
            return new PSBooleanLiteralImpl(node);

        } else if (type.equals(PSElements.NumericLiteral)) {
            return new PSNumericLiteralImpl(node);

        } else if (type.equals(PSElements.StringLiteral)) {
            return new PSStringLiteralImpl(node);

        } else if (type.equals(PSElements.ArrayLiteral)) {
            return new PSArrayLiteralImpl(node);

        } else if (type.equals(PSElements.ObjectLiteral)) {
            return new PSObjectLiteralImpl(node);

        } else if (type.equals(PSElements.Abs)) {
            return new PSAbsImpl(node);

        } else if (type.equals(PSElements.IdentInfix)) {
            return new PSIdentInfixImpl(node);

        } else if (type.equals(PSElements.Var)) {
            return new PSVarImpl(node);
//
//        } else if (type.equals(PSElements.Constructor)) {
//            return new PSConstructorImpl(node);

        } else if (type.equals(PSElements.Case)) {
            return new PSCaseImpl(node);

        } else if (type.equals(PSElements.CaseAlternative)) {
            return new PSCaseAlternativeImpl(node);

        } else if (type.equals(PSElements.IfThenElse)) {
            return new PSIfThenElseImpl(node);

        } else if (type.equals(PSElements.Let)) {
            return new PSLetImpl(node);

        } else if (type.equals(PSElements.Parens)) {
            return new PSParensImpl(node);

        } else if (type.equals(PSElements.UnaryMinus)) {
            return new PSUnaryMinusImpl(node);

        } else if (type.equals(PSElements.PrefixValue)) {
            return new PSPrefixValueImpl(node);

        } else if (type.equals(PSElements.Accessor)) {
            return new PSAccessorImpl(node);

        } else if (type.equals(PSElements.DoNotationLet)) {
            return new PSDoNotationLetImpl(node);

        } else if (type.equals(PSElements.DoNotationBind)) {
            return new PSDoNotationBindImpl(node);

        } else if (type.equals(PSElements.DoNotationValue)) {
            return new PSDoNotationValueImpl(node);

        } else if (type.equals(PSElements.Value)) {
            return new PSValueImpl(node);

        } else if (type.equals(PSElements.Fixity)) {
            return new PSFixityImpl(node);

        } else if (type.equals(PSElements.JSRaw)) {
            return new PSJSRawImpl(node);

//        } else if (type.equals(PSElements.pModuleName)) {
//            return new PSModuleNameImpl(node);
//
//        } else if (type.equals(PSElements.importModuleName)) {
//            return new PSImportModuleNameImpl(node);
//
//        } else if (type.equals(PSElements.qualifiedModuleName)) {
//            return new PSQualifiedModuleNameImpl(node);
//
//        } else if (type.equals(PSElements.pClassName)) {
//            return new PSClassNameImpl(node);

        } else if (type.equals(PSElements.pImplies)) {
            return new PSImpliesImpl(node);

        } else if (type.equals(PSElements.TypeHole)) {
            return new PSTypeHoleImpl(node);

        } else {
            return new PSASTWrapperElement(node);
        }


    }

    @Override
    public PsiFile createFile(final FileViewProvider viewProvider) {
        return new PSFile(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(final ASTNode left, final ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
