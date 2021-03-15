package org.purescript.parser

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
import org.purescript.file.PSFile
import org.purescript.file.PSFileStubType
import org.purescript.lexer.PSLexer
import org.purescript.psi.*
import org.purescript.psi.`var`.PSVar
import org.purescript.psi.char.PSCharBinder
import org.purescript.psi.char.PSCharLiteral
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataConstructorList
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.exports.*
import org.purescript.psi.imports.*

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
        return TokenSet.create(PSTokens.WS)
    }

    override fun getCommentTokens(): TokenSet {
        return TokenSet.create(
            PSTokens.DOC_COMMENT,
            PSTokens.MLCOMMENT,
            PSTokens.SLCOMMENT,
        )
    }

    override fun getStringLiteralElements(): TokenSet {
        return PSTokens.kStrings
    }

    override fun createElement(node: ASTNode): PsiElement {
        val type = node.elementType
        return if (type == PSElements.ProperName || type == PSElements.Qualified || type == PSElements.pClassName || type == PSElements.pModuleName || type == PSElements.importModuleName) {
            PSProperName(node)
        } else if (type == PSElements.Identifier || type == PSElements.GenericIdentifier || type == PSElements.TypeConstructor || type == PSElements.Constructor || type == PSElements.LocalIdentifier) {
            PSIdentifier(node)
        } else if (type == PSElements.ImportDeclaration) {
            PSImportDeclarationImpl(node)
        } else if (type == PSElements.ImportAlias) {
            PSImportAlias(node)
        } else if (type == PSElements.ImportList) {
            PSImportList(node)
        } else if (type == PSElements.ImportedClass) {
            PSImportedClass(node)
        } else if (type == PSElements.ImportedData) {
            PSImportedData(node)
        } else if (type == PSElements.ImportedDataMember) {
            PSImportedDataMember(node)
        } else if (type == PSElements.ImportedDataMemberList) {
            PSImportedDataMemberList(node)
        } else if (type == PSElements.ImportedKind) {
            PSImportedKind(node)
        } else if (type == PSElements.ImportedOperator) {
            PSImportedOperator(node)
        } else if (type == PSElements.ImportedType) {
            PSImportedType(node)
        } else if (type == PSElements.ImportedValue) {
            PSImportedValue(node)
        } else if (type == PSElements.DataDeclaration) {
            PSDataDeclaration(node)
        } else if (type == PSElements.DataConstructorList) {
            PSDataConstructorList(node)
        } else if (type == PSElements.DataConstructor) {
            PSDataConstructor(node)
        } else if (type == PSElements.Binder) {
            PSBinderImpl(node)
        } else if (type == PSElements.Module) {
            PSModule(node)
        } else if (type == PSElements.ExportList) {
            PSExportList(node)
        } else if (type == PSElements.ExportedClass) {
            PSExportedClass(node)
        } else if (type == PSElements.ExportedData) {
            PSExportedData(node)
        } else if (type == PSElements.ExportedDataMember) {
            PSExportedDataMember(node)
        } else if (type == PSElements.ExportedDataMemberList) {
            PSExportedDataMemberList(node)
        } else if (type == PSElements.ExportedKind) {
            PSExportedKind(node)
        } else if (type == PSElements.ExportedModule) {
            PSExportedModule(node)
        } else if (type == PSElements.ExportedOperator) {
            PSExportedOperator(node)
        } else if (type == PSElements.ExportedType) {
            PSExportedType(node)
        } else if (type == PSElements.ExportedValue) {
            PSExportedValue(node)
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
            PSValueDeclaration(node)
        } else if (type == PSElements.ExternDataDeclaration) {
            PSExternDataDeclarationImpl(node)
        } else if (type == PSElements.ExternInstanceDeclaration) {
            PSExternInstanceDeclarationImpl(node)
        } else if (type == PSElements.ForeignValueDeclaration) {
            PSForeignValueDeclaration(node)
        } else if (type == PSElements.FixityDeclaration) {
            PSFixityDeclarationImpl(node)
        } else if (type == PSElements.PositionedDeclarationRef) {
            PSPositionedDeclarationRefImpl(node)
        } else if (type == PSElements.ClassDeclaration) {
            PSClassDeclaration(node)
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
        }  else if (type == PSElements.CharBinder) {
            PSCharBinder(node)
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
        } else if (type == PSElements.CharLiteral) {
            PSCharLiteral(node)
        } else if (type == PSElements.ArrayLiteral) {
            PSArrayLiteralImpl(node)
        } else if (type == PSElements.ObjectLiteral) {
            PSObjectLiteralImpl(node)
        } else if (type == PSElements.Abs) {
            PSAbsImpl(node)
        } else if (type == PSElements.Var) {
            PSVar(node)
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
