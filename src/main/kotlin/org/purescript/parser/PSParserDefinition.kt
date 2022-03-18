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
import org.purescript.lexer.LayoutLexer
import org.purescript.lexer.PSLexer
import org.purescript.parser.PSElements.Companion.Abs
import org.purescript.parser.PSElements.Companion.Accessor
import org.purescript.parser.PSElements.Companion.ArrayLiteral
import org.purescript.parser.PSElements.Companion.Bang
import org.purescript.parser.PSElements.Companion.Binder
import org.purescript.parser.PSElements.Companion.BinderAtom
import org.purescript.parser.PSElements.Companion.BooleanBinder
import org.purescript.parser.PSElements.Companion.BooleanLiteral
import org.purescript.parser.PSElements.Companion.Case
import org.purescript.parser.PSElements.Companion.CaseAlternative
import org.purescript.parser.PSElements.Companion.CharBinder
import org.purescript.parser.PSElements.Companion.CharLiteral
import org.purescript.parser.PSElements.Companion.ClassConstraint
import org.purescript.parser.PSElements.Companion.ClassConstraintList
import org.purescript.parser.PSElements.Companion.ClassDeclaration
import org.purescript.parser.PSElements.Companion.ClassFunctionalDependency
import org.purescript.parser.PSElements.Companion.ClassFunctionalDependencyList
import org.purescript.parser.PSElements.Companion.ClassMember
import org.purescript.parser.PSElements.Companion.ClassMemberList
import org.purescript.parser.PSElements.Companion.ClassName
import org.purescript.parser.PSElements.Companion.ConstrainedType
import org.purescript.parser.PSElements.Companion.ConstructorBinder
import org.purescript.parser.PSElements.Companion.DataConstructor
import org.purescript.parser.PSElements.Companion.DataConstructorList
import org.purescript.parser.PSElements.Companion.DataDeclaration
import org.purescript.parser.PSElements.Companion.DoBlock
import org.purescript.parser.PSElements.Companion.DoNotationBind
import org.purescript.parser.PSElements.Companion.DoNotationLet
import org.purescript.parser.PSElements.Companion.DoNotationValue
import org.purescript.parser.PSElements.Companion.ExportList
import org.purescript.parser.PSElements.Companion.ExportedClass
import org.purescript.parser.PSElements.Companion.ExportedData
import org.purescript.parser.PSElements.Companion.ExportedDataMember
import org.purescript.parser.PSElements.Companion.ExportedDataMemberList
import org.purescript.parser.PSElements.Companion.ExportedKind
import org.purescript.parser.PSElements.Companion.ExportedModule
import org.purescript.parser.PSElements.Companion.ExportedOperator
import org.purescript.parser.PSElements.Companion.ExportedType
import org.purescript.parser.PSElements.Companion.ExportedValue
import org.purescript.parser.PSElements.Companion.ExpressionConstructor
import org.purescript.parser.PSElements.Companion.ExpressionIdentifier
import org.purescript.parser.PSElements.Companion.ExpressionOperator
import org.purescript.parser.PSElements.Companion.ExpressionSymbol
import org.purescript.parser.PSElements.Companion.ExpressionWhere
import org.purescript.parser.PSElements.Companion.ExternInstanceDeclaration
import org.purescript.parser.PSElements.Companion.Fixity
import org.purescript.parser.PSElements.Companion.FixityDeclaration
import org.purescript.parser.PSElements.Companion.ForAll
import org.purescript.parser.PSElements.Companion.ForeignDataDeclaration
import org.purescript.parser.PSElements.Companion.ForeignValueDeclaration
import org.purescript.parser.PSElements.Companion.FunKind
import org.purescript.parser.PSElements.Companion.GenericIdentifier
import org.purescript.parser.PSElements.Companion.Guard
import org.purescript.parser.PSElements.Companion.Identifier
import org.purescript.parser.PSElements.Companion.IfThenElse
import org.purescript.parser.PSElements.Companion.ImportAlias
import org.purescript.parser.PSElements.Companion.ImportDeclaration
import org.purescript.parser.PSElements.Companion.ImportList
import org.purescript.parser.PSElements.Companion.ImportedClass
import org.purescript.parser.PSElements.Companion.ImportedData
import org.purescript.parser.PSElements.Companion.ImportedDataMember
import org.purescript.parser.PSElements.Companion.ImportedDataMemberList
import org.purescript.parser.PSElements.Companion.ImportedKind
import org.purescript.parser.PSElements.Companion.ImportedOperator
import org.purescript.parser.PSElements.Companion.ImportedType
import org.purescript.parser.PSElements.Companion.ImportedValue
import org.purescript.parser.PSElements.Companion.InstanceDeclaration
import org.purescript.parser.PSElements.Companion.JSRaw
import org.purescript.parser.PSElements.Companion.Let
import org.purescript.parser.PSElements.Companion.LocalIdentifier
import org.purescript.parser.PSElements.Companion.Module
import org.purescript.parser.PSElements.Companion.ModuleName
import org.purescript.parser.PSElements.Companion.NamedBinder
import org.purescript.parser.PSElements.Companion.NewTypeConstructor
import org.purescript.parser.PSElements.Companion.NewtypeDeclaration
import org.purescript.parser.PSElements.Companion.NullBinder
import org.purescript.parser.PSElements.Companion.NumberBinder
import org.purescript.parser.PSElements.Companion.NumericLiteral
import org.purescript.parser.PSElements.Companion.ObjectBinder
import org.purescript.parser.PSElements.Companion.ObjectBinderField
import org.purescript.parser.PSElements.Companion.ObjectLiteral
import org.purescript.parser.PSElements.Companion.ObjectType
import org.purescript.parser.PSElements.Companion.OperatorName
import org.purescript.parser.PSElements.Companion.Parens
import org.purescript.parser.PSElements.Companion.PositionedDeclarationRef
import org.purescript.parser.PSElements.Companion.ProperName
import org.purescript.parser.PSElements.Companion.QualifiedIdentifier
import org.purescript.parser.PSElements.Companion.QualifiedOperatorName
import org.purescript.parser.PSElements.Companion.QualifiedProperName
import org.purescript.parser.PSElements.Companion.QualifiedSymbol
import org.purescript.parser.PSElements.Companion.Row
import org.purescript.parser.PSElements.Companion.RowKind
import org.purescript.parser.PSElements.Companion.Signature
import org.purescript.parser.PSElements.Companion.Star
import org.purescript.parser.PSElements.Companion.StringBinder
import org.purescript.parser.PSElements.Companion.StringLiteral
import org.purescript.parser.PSElements.Companion.Symbol
import org.purescript.parser.PSElements.Companion.Type
import org.purescript.parser.PSElements.Companion.TypeArgs
import org.purescript.parser.PSElements.Companion.TypeAtom
import org.purescript.parser.PSElements.Companion.TypeConstructor
import org.purescript.parser.PSElements.Companion.TypeHole
import org.purescript.parser.PSElements.Companion.TypeSynonymDeclaration
import org.purescript.parser.PSElements.Companion.TypeVar
import org.purescript.parser.PSElements.Companion.TypeVarKinded
import org.purescript.parser.PSElements.Companion.TypeVarName
import org.purescript.parser.PSElements.Companion.UnaryMinus
import org.purescript.parser.PSElements.Companion.Value
import org.purescript.parser.PSElements.Companion.ValueDeclaration
import org.purescript.parser.PSElements.Companion.ValueRef
import org.purescript.parser.PSElements.Companion.VarBinder
import org.purescript.parser.PSElements.Companion.importModuleName
import org.purescript.parser.PSElements.Companion.pImplies
import org.purescript.psi.*
import org.purescript.psi.binder.*
import org.purescript.psi.char.PSCharBinder
import org.purescript.psi.char.PSCharLiteral
import org.purescript.psi.classes.*
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataConstructorList
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.declaration.PSFixityDeclaration
import org.purescript.psi.declaration.PSSignature
import org.purescript.psi.declaration.PSValueDeclaration
import org.purescript.psi.dostmt.PSDoBlock
import org.purescript.psi.dostmt.PSDoNotationBind
import org.purescript.psi.dostmt.PSDoNotationLet
import org.purescript.psi.dostmt.PSDoNotationValue
import org.purescript.psi.exports.*
import org.purescript.psi.expression.*
import org.purescript.psi.imports.*
import org.purescript.psi.name.*
import org.purescript.psi.newtype.PSNewTypeConstructor
import org.purescript.psi.newtype.PSNewTypeDeclaration
import org.purescript.psi.typeconstructor.PSTypeConstructor
import org.purescript.psi.typesynonym.PSTypeSynonymDeclaration
import org.purescript.psi.typevar.PSTypeVarKinded
import org.purescript.psi.typevar.PSTypeVarName

class PSParserDefinition : ParserDefinition, PSTokens {
    override fun createLexer(project: Project): Lexer {
        return LayoutLexer(PSLexer())
    }

    override fun createParser(project: Project): PsiParser {
        return PureParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return PSFileStubType.INSTANCE
    }

    override fun getWhitespaceTokens(): TokenSet {
        return TokenSet.create(
            PSTokens.WS,
        )
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

    override fun createElement(node: ASTNode): PsiElement =
        when (node.elementType) {
            ProperName, importModuleName -> PSProperName(node)
            ClassName -> PSClassName(node)
            OperatorName -> PSOperatorName(node)
            Symbol -> PSSymbol(node)
            ModuleName -> PSModuleName(node)
            QualifiedProperName -> PSQualifiedProperName(node)
            QualifiedOperatorName -> PSQualifiedOperatorName(node)
            QualifiedIdentifier -> PSQualifiedIdentifier(node)
            QualifiedSymbol -> PSQualifiedSymbol(node)
            Identifier, GenericIdentifier, LocalIdentifier -> PSIdentifier(node)
            ExpressionConstructor -> PSExpressionConstructor(node)
            ExpressionIdentifier -> PSExpressionIdentifier(node)
            ExpressionOperator -> PSExpressionOperator(node)
            ExpressionSymbol -> PSExpressionSymbol(node)
            ExpressionWhere -> PSExpressionWhere(node)
            TypeConstructor -> PSTypeConstructor(node)
            ImportDeclaration -> PSImportDeclaration(node)
            ImportAlias -> PSImportAlias(node)
            ImportList -> PSImportList(node)
            ImportedClass -> PSImportedClass(node)
            ImportedData -> PSImportedData(node)
            ImportedDataMember -> PSImportedDataMember(node)
            ImportedDataMemberList -> PSImportedDataMemberList(node)
            ImportedKind -> PSImportedKind(node)
            ImportedOperator -> PSImportedOperator(node)
            ImportedType -> PSImportedType(node)
            ImportedValue -> PSImportedValue(node)
            DataDeclaration -> PSDataDeclaration(node)
            DataConstructorList -> PSDataConstructorList(node)
            DataConstructor -> PSDataConstructor(node)
            Binder -> PSBinder(node)
            Module -> PSModule(node)
            ExportList -> PSExportList(node)
            ExportedClass -> PSExportedClass(node)
            ExportedData -> PSExportedData(node)
            ExportedDataMember -> PSExportedDataMember(node)
            ExportedDataMemberList -> PSExportedDataMemberList(node)
            ExportedKind -> PSExportedKind(node)
            ExportedModule -> PSExportedModule(node)
            ExportedOperator -> PSExportedOperator(node)
            ExportedType -> PSExportedType(node)
            ExportedValue -> PSExportedValue(node)
            Star -> PSStar(node)
            Bang -> PSBang(node)
            RowKind -> PSRowKind(node)
            FunKind -> PSFunKind(node)
            Type -> PSType(node)
            TypeArgs -> PSTypeArgs(node)
            ForAll -> PSForAll(node)
            ConstrainedType -> PSConstrainedType(node)
            Row -> PSRow(node)
            ObjectType -> PSObjectType(node)
            TypeVar -> PSTypeVar(node)
            TypeVarName -> PSTypeVarName(node)
            TypeVarKinded -> PSTypeVarKinded(node)
            TypeAtom -> PSTypeAtom(node)
            Signature -> PSSignature(node)
            TypeSynonymDeclaration -> PSTypeSynonymDeclaration(node)
            ValueDeclaration -> PSValueDeclaration(node)
            ForeignDataDeclaration -> PSForeignDataDeclaration(node)
            ExternInstanceDeclaration -> PSExternInstanceDeclaration(node)
            ForeignValueDeclaration -> PSForeignValueDeclaration(node)
            FixityDeclaration -> PSFixityDeclaration(node)
            PositionedDeclarationRef -> PSPositionedDeclarationRef(node)
            ClassDeclaration -> PSClassDeclaration(node)
            ClassConstraintList -> PSClassConstraintList(node)
            ClassConstraint -> PSClassConstraint(node)
            ClassFunctionalDependencyList -> PSClassFunctionalDependencyList(node)
            ClassFunctionalDependency -> PSClassFunctionalDependency(node)
            ClassMemberList -> PSClassMemberList(node)
            ClassMember -> PSClassMember(node)
            InstanceDeclaration -> PSInstanceDeclaration(node)
            NewtypeDeclaration -> PSNewTypeDeclaration(node)
            NewTypeConstructor -> PSNewTypeConstructor(node)
            Guard -> PSGuard(node)
            NullBinder -> PSNullBinder(node)
            StringBinder -> PSStringBinder(node)
            CharBinder -> PSCharBinder(node)
            BooleanBinder -> PSBooleanBinder(node)
            NumberBinder -> PSNumberBinder(node)
            NamedBinder -> PSNamedBinder(node)
            VarBinder -> PSVarBinder(node)
            ConstructorBinder -> PSConstructorBinder(node)
            ObjectBinder -> PSObjectBinder(node)
            ObjectBinderField -> PSObjectBinderField(node)
            BinderAtom -> PSBinderAtom(node)
            ValueRef -> PSValueRef(node)
            BooleanLiteral -> PSBooleanLiteral(node)
            NumericLiteral -> PSNumericLiteral(node)
            StringLiteral -> PSStringLiteral(node)
            CharLiteral -> PSCharLiteral(node)
            ArrayLiteral -> PSArrayLiteral(node)
            ObjectLiteral -> PSObjectLiteral(node)
            Abs -> PSAbs(node)
            Case -> PSCase(node)
            CaseAlternative -> PSCaseAlternative(node)
            IfThenElse -> PSIfThenElse(node)
            Let -> PSLet(node)
            Parens -> PSParens(node)
            UnaryMinus -> PSUnaryMinus(node)
            Accessor -> PSAccessor(node)
            DoBlock -> PSDoBlock(node)
            DoNotationLet -> PSDoNotationLet(node)
            DoNotationBind -> PSDoNotationBind(node)
            DoNotationValue -> PSDoNotationValue(node)
            Value -> PSValue(node)
            Fixity -> PSFixity(node)
            JSRaw -> PSJSRaw(node)
            pImplies -> PSImplies(node)
            TypeHole -> PSTypeHole(node)
            else -> PSASTWrapperElement(node)
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
