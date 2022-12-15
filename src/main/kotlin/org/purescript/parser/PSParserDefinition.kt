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

class PSParserDefinition : ParserDefinition {
    override fun createLexer(project: Project): Lexer {
        return LayoutLexer(PSLexer())
    }

    override fun createParser(project: Project): PsiParser {
        return PureParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return PSFileStubType.INSTANCE
    }

    override fun getCommentTokens(): TokenSet {
        return TokenSet.create(DOC_COMMENT, MLCOMMENT, SLCOMMENT)
    }

    override fun getStringLiteralElements(): TokenSet {
        return kStrings
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
            ValueRef -> PSValueRef(node)
            BooleanLiteral -> PSBooleanLiteral(node)
            NumericLiteral -> PSNumericLiteral(node)
            StringLiteral -> PSStringLiteral(node)
            CharLiteral -> PSCharLiteral(node)
            ArrayLiteral -> PSArrayLiteral(node)
            ObjectLiteral -> PSObjectLiteral(node)
            Lambda -> PSLambda(node)
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
