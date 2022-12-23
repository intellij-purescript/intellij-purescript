package org.purescript.parser

import com.intellij.extapi.psi.ASTWrapperPsiElement
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
        when (node.elementType.hashCode()) {
            ProperName.hashCode(), importModuleName.hashCode() -> PSProperName(node)
            ClassName.hashCode() -> PSClassName(node)
            OperatorName.hashCode() -> PSOperatorName(node)
            Symbol.hashCode() -> PSSymbol(node)
            ModuleName.hashCode() -> PSModuleName(node)
            QualifiedProperName.hashCode() -> PSQualifiedProperName(node)
            QualifiedOperatorName.hashCode() -> PSQualifiedOperatorName(node)
            QualifiedIdentifier.hashCode() -> PSQualifiedIdentifier(node)
            QualifiedSymbol.hashCode() -> PSQualifiedSymbol(node)
            Identifier.hashCode(), GenericIdentifier.hashCode(), LocalIdentifier.hashCode() -> PSIdentifier(node)
            ExpressionConstructor.hashCode() -> PSExpressionConstructor(node)
            ExpressionIdentifier.hashCode() -> PSExpressionIdentifier(node)
            ExpressionOperator.hashCode() -> PSExpressionOperator(node)
            ExpressionSymbol.hashCode() -> PSExpressionSymbol(node)
            ExpressionWhere.hashCode() -> PSExpressionWhere(node)
            TypeConstructor.hashCode() -> PSTypeConstructor(node)
            ImportDeclaration.hashCode() -> PSImportDeclaration(node)
            ImportAlias.hashCode() -> PSImportAlias(node)
            ImportList.hashCode() -> PSImportList(node)
            ImportedClass.hashCode() -> PSImportedClass(node)
            ImportedData.hashCode() -> PSImportedData(node)
            ImportedDataMember.hashCode() -> PSImportedDataMember(node)
            ImportedDataMemberList.hashCode() -> PSImportedDataMemberList(node)
            ImportedKind.hashCode() -> PSImportedKind(node)
            ImportedOperator.hashCode() -> PSImportedOperator(node)
            ImportedType.hashCode() -> PSImportedType(node)
            ImportedValue.hashCode() -> PSImportedValue(node)
            DataDeclaration.hashCode() -> PSDataDeclaration(node)
            DataConstructorList.hashCode() -> PSDataConstructorList(node)
            DataConstructor.hashCode() -> PSDataConstructor(node)
            Module.hashCode() -> PSModule(node)
            ExportList.hashCode() -> PSExportList(node)
            ExportedClass.hashCode() -> PSExportedClass(node)
            ExportedData.hashCode() -> PSExportedData(node)
            ExportedDataMember.hashCode() -> PSExportedDataMember(node)
            ExportedDataMemberList.hashCode() -> PSExportedDataMemberList(node)
            ExportedKind.hashCode() -> PSExportedKind(node)
            ExportedModule.hashCode() -> PSExportedModule(node)
            ExportedOperator.hashCode() -> PSExportedOperator(node)
            ExportedType.hashCode() -> PSExportedType(node)
            ExportedValue.hashCode() -> PSExportedValue(node)
            Star.hashCode() -> PSStar(node)
            Bang.hashCode() -> PSBang(node)
            RowKind.hashCode() -> PSRowKind(node)
            FunKind.hashCode() -> PSFunKind(node)
            Type.hashCode() -> PSType(node)
            TypeArgs.hashCode() -> PSTypeArgs(node)
            ForAll.hashCode() -> PSForAll(node)
            ConstrainedType.hashCode() -> PSConstrainedType(node)
            Row.hashCode() -> PSRow(node)
            ObjectType.hashCode() -> PSObjectType(node)
            TypeVar.hashCode() -> PSTypeVar(node)
            TypeVarName.hashCode() -> PSTypeVarName(node)
            TypeVarKinded.hashCode() -> PSTypeVarKinded(node)
            TypeAtom.hashCode() -> PSTypeAtom(node)
            Signature.hashCode() -> PSSignature(node)
            TypeSynonymDeclaration.hashCode() -> PSTypeSynonymDeclaration(node)
            ValueDeclaration.hashCode() -> PSValueDeclaration(node)
            ForeignDataDeclaration.hashCode() -> PSForeignDataDeclaration(node)
            ExternInstanceDeclaration.hashCode() -> PSExternInstanceDeclaration(node)
            ForeignValueDeclaration.hashCode() -> PSForeignValueDeclaration(node)
            FixityDeclaration.hashCode() -> PSFixityDeclaration(node)
            PositionedDeclarationRef.hashCode() -> PSPositionedDeclarationRef(node)
            ClassDeclaration.hashCode() -> PSClassDeclaration(node)
            ClassConstraintList.hashCode() -> PSClassConstraintList(node)
            ClassConstraint.hashCode() -> PSClassConstraint(node)
            ClassFunctionalDependencyList.hashCode() -> PSClassFunctionalDependencyList(node)
            ClassFunctionalDependency.hashCode() -> PSClassFunctionalDependency(node)
            ClassMemberList.hashCode() -> PSClassMemberList(node)
            ClassMember.hashCode() -> PSClassMember(node)
            InstanceDeclaration.hashCode() -> PSInstanceDeclaration(node)
            NewtypeDeclaration.hashCode() -> PSNewTypeDeclaration(node)
            NewTypeConstructor.hashCode() -> PSNewTypeConstructor(node)
            Guard.hashCode() -> PSGuard(node)
            NullBinder.hashCode() -> PSNullBinder(node)
            StringBinder.hashCode() -> PSStringBinder(node)
            CharBinder.hashCode() -> PSCharBinder(node)
            BooleanBinder.hashCode() -> PSBooleanBinder(node)
            NumberBinder.hashCode() -> PSNumberBinder(node)
            NamedBinder.hashCode() -> PSNamedBinder(node)
            VarBinder.hashCode() -> PSVarBinder(node)
            ConstructorBinder.hashCode() -> PSConstructorBinder(node)
            ObjectBinder.hashCode() -> PSObjectBinder(node)
            ObjectBinderField.hashCode() -> PSObjectBinderField(node)
            ValueRef.hashCode() -> PSValueRef(node)
            BooleanLiteral.hashCode() -> PSBooleanLiteral(node)
            NumericLiteral.hashCode() -> PSNumericLiteral(node)
            StringLiteral.hashCode() -> PSStringLiteral(node)
            CharLiteral.hashCode() -> PSCharLiteral(node)
            ArrayLiteral.hashCode() -> PSArrayLiteral(node)
            ObjectLiteral.hashCode() -> PSObjectLiteral(node)
            Lambda.hashCode() -> PSLambda(node)
            Case.hashCode() -> PSCase(node)
            CaseAlternative.hashCode() -> PSCaseAlternative(node)
            IfThenElse.hashCode() -> PSIfThenElse(node)
            Let.hashCode() -> PSLet(node)
            Parens.hashCode() -> PSParens(node)
            UnaryMinus.hashCode() -> PSUnaryMinus(node)
            Accessor.hashCode() -> PSAccessor(node)
            DoBlock.hashCode() -> PSDoBlock(node)
            DoNotationLet.hashCode() -> PSDoNotationLet(node)
            DoNotationBind.hashCode() -> PSDoNotationBind(node)
            DoNotationValue.hashCode() -> PSDoNotationValue(node)
            Value.hashCode() -> PSValue(node)
            Fixity.hashCode() -> PSFixity(node)
            pImplies.hashCode() -> PSImplies(node)
            TypeHole.hashCode() -> PSTypeHole(node)
            else -> ASTWrapperPsiElement(node) // this should never happen
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
