package net.kenro.ji.jin.purescript.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import net.kenro.ji.jin.purescript.file.PSFile
import net.kenro.ji.jin.purescript.file.PSFileStubType
import net.kenro.ji.jin.purescript.lexer.PSLexer
import net.kenro.ji.jin.purescript.psi.PSElements
import net.kenro.ji.jin.purescript.psi.PSTokens
import net.kenro.ji.jin.purescript.psi.cst.PSASTWrapperElement
import net.kenro.ji.jin.purescript.psi.impl.*

class PSParserDefinition : ParserDefinition, PSTokens {
    override fun createLexer(project: Project): Lexer {
        return PSLexer()
    }

    override fun createParser(project: Project): PsiParser {
        return PureParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return PSFileStubType.INSTANCE
    }

    override fun getWhitespaceTokens(): TokenSet {
        return TokenSet.EMPTY
    }

    override fun getCommentTokens(): TokenSet {
        return TokenSet.EMPTY
    }

    override fun getStringLiteralElements(): TokenSet {
        return PSTokens.kStrings
    }

    override fun createElement(node: ASTNode): PsiElement {
        val type = node.elementType
        return if (type == PSElements.ProperName || type == PSElements.Qualified || type == PSElements.pClassName || type == PSElements.pModuleName || type == PSElements.importModuleName) {
            PSProperNameImpl(node)
        } else if (type == PSElements.Identifier || type == PSElements.GenericIdentifier || type == PSElements.TypeConstructor || type == PSElements.Constructor || type == PSElements.LocalIdentifier) {
            PSIdentifierImpl(node)
        } else if (type == PSElements.ImportDeclaration) {
            PSImportDeclarationImpl(node)
        } else if (type == PSElements.DataDeclaration) {
            PSDataDeclarationImpl(node)
        } else if (type == PSElements.Binder) {
            PSBinderImpl(node)
        } else if (type == PSElements.Program) {
            PSProgramImpl(node)
        } else if (type == PSElements.Module) {
            PSModuleImpl(node)
        } else if (type == PSElements.Star) {
            PSStarImpl(node)
        } else if (type == PSElements.Bang) {
            PSBangImpl(node)
        } else if (type == PSElements.RowKind) {
            PSRowKindImpl(node)
        } else if (type == PSElements.FunKind) {
            PSFunKindImpl(node)

//        } else if (type.equals(PSElements.Qualified)) {
//            return new PSQualifiedImpl(node);
        } else if (type == PSElements.Type) {
            PSTypeImpl(node)
        } else if (type == PSElements.TypeArgs) {
            PSTypeArgsImpl(node)
        } else if (type == PSElements.TypeAnnotationName) {
            PSTypeAnnotationNameImpl(node)
        } else if (type == PSElements.ForAll) {
            PSForAllImpl(node)
        } else if (type == PSElements.ConstrainedType) {
            PSConstrainedTypeImpl(node)
        } else if (type == PSElements.Row) {
            PSRowImpl(node)
        } else if (type == PSElements.ObjectType) {
            PSObjectTypeImpl(node)
        } else if (type == PSElements.TypeVar) {
            PSTypeVarImpl(node)

//        } else if (type.equals(PSElements.TypeConstructor)) {
//            return new PSTypeConstructorImpl(node);
        } else if (type == PSElements.TypeAtom) {
            PSTypeAtomImpl(node)
        } else if (type == PSElements.GenericIdentifier) {
            PSGenericIdentifierImpl(node)
        } else if (type == PSElements.LocalIdentifier) {
            PSLocalIdentifierImpl(node)
        } else if (type == PSElements.TypeDeclaration) {
            PSTypeDeclarationImpl(node)
        } else if (type == PSElements.TypeSynonymDeclaration) {
            PSTypeSynonymDeclarationImpl(node)
        } else if (type == PSElements.ValueDeclaration) {
            PSValueDeclarationImpl(node)
        } else if (type == PSElements.ExternDataDeclaration) {
            PSExternDataDeclarationImpl(node)
        } else if (type == PSElements.ExternInstanceDeclaration) {
            PSExternInstanceDeclarationImpl(node)
        } else if (type == PSElements.ExternDeclaration) {
            PSExternDeclarationImpl(node)
        } else if (type == PSElements.FixityDeclaration) {
            PSFixityDeclarationImpl(node)
        } else if (type == PSElements.PositionedDeclarationRef) {
            PSPositionedDeclarationRefImpl(node)
        } else if (type == PSElements.TypeClassDeclaration) {
            PSTypeClassDeclarationImpl(node)
        } else if (type == PSElements.TypeInstanceDeclaration) {
            PSTypeInstanceDeclarationImpl(node)
        } else if (type == PSElements.NewtypeDeclaration) {
            PSNewTypeDeclarationImpl(node)
        } else if (type == PSElements.Guard) {
            PSGuardImpl(node)
        } else if (type == PSElements.NullBinder) {
            PSNullBinderImpl(node)
        } else if (type == PSElements.StringBinder) {
            PSStringBinderImpl(node)
        } else if (type == PSElements.BooleanBinder) {
            PSBooleanBinderImpl(node)
        } else if (type == PSElements.NumberBinder) {
            PSNumberBinderImpl(node)
        } else if (type == PSElements.NamedBinder) {
            PSNamedBinderImpl(node)
        } else if (type == PSElements.VarBinder) {
            PSVarBinderImpl(node)
        } else if (type == PSElements.ConstructorBinder) {
            PSConstructorBinderImpl(node)
        } else if (type == PSElements.ObjectBinder) {
            PSObjectBinderImpl(node)
        } else if (type == PSElements.ObjectBinderField) {
            PSObjectBinderFieldImpl(node)
        } else if (type == PSElements.BinderAtom) {
            PSBinderAtomImpl(node)
        } else if (type == PSElements.Binder) {
            PSBinderImpl(node)
        } else if (type == PSElements.ValueRef) {
            PSValueRefImpl(node)
        } else if (type == PSElements.BooleanLiteral) {
            PSBooleanLiteralImpl(node)
        } else if (type == PSElements.NumericLiteral) {
            PSNumericLiteralImpl(node)
        } else if (type == PSElements.StringLiteral) {
            PSStringLiteralImpl(node)
        } else if (type == PSElements.ArrayLiteral) {
            PSArrayLiteralImpl(node)
        } else if (type == PSElements.ObjectLiteral) {
            PSObjectLiteralImpl(node)
        } else if (type == PSElements.Abs) {
            PSAbsImpl(node)
        } else if (type == PSElements.IdentInfix) {
            PSIdentInfixImpl(node)
        } else if (type == PSElements.Var) {
            PSVarImpl(node)
            //
//        } else if (type.equals(PSElements.Constructor)) {
//            return new PSConstructorImpl(node);
        } else if (type == PSElements.Case) {
            PSCaseImpl(node)
        } else if (type == PSElements.CaseAlternative) {
            PSCaseAlternativeImpl(node)
        } else if (type == PSElements.IfThenElse) {
            PSIfThenElseImpl(node)
        } else if (type == PSElements.Let) {
            PSLetImpl(node)
        } else if (type == PSElements.Parens) {
            PSParensImpl(node)
        } else if (type == PSElements.UnaryMinus) {
            PSUnaryMinusImpl(node)
        } else if (type == PSElements.PrefixValue) {
            PSPrefixValueImpl(node)
        } else if (type == PSElements.Accessor) {
            PSAccessorImpl(node)
        } else if (type == PSElements.DoNotationLet) {
            PSDoNotationLetImpl(node)
        } else if (type == PSElements.DoNotationBind) {
            PSDoNotationBindImpl(node)
        } else if (type == PSElements.DoNotationValue) {
            PSDoNotationValueImpl(node)
        } else if (type == PSElements.Value) {
            PSValueImpl(node)
        } else if (type == PSElements.Fixity) {
            PSFixityImpl(node)
        } else if (type == PSElements.JSRaw) {
            PSJSRawImpl(node)

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
        } else if (type == PSElements.pImplies) {
            PSImpliesImpl(node)
        } else if (type == PSElements.TypeHole) {
            PSTypeHoleImpl(node)
        } else {
            PSASTWrapperElement(node)
        }
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return PSFile(viewProvider)
    }

    override fun spaceExistenceTypeBetweenTokens(
        left: ASTNode,
        right: ASTNode
    ): SpaceRequirements {
        return SpaceRequirements.MAY
    }
}