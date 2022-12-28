package org.purescript.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import org.purescript.psi.*
import org.purescript.psi.PSElementType.WithPsi
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.binder.*
import org.purescript.psi.char.PSCharBinder
import org.purescript.psi.char.PSCharLiteral
import org.purescript.psi.classes.*
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataConstructorList
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.declaration.*
import org.purescript.psi.dostmt.PSDoBlock
import org.purescript.psi.dostmt.PSDoNotationBind
import org.purescript.psi.dostmt.PSDoNotationLet
import org.purescript.psi.dostmt.PSDoNotationValue
import org.purescript.psi.exports.*
import org.purescript.psi.expression.*
import org.purescript.psi.imports.*
import org.purescript.psi.module.ModuleNameIndex
import org.purescript.psi.module.Module.*
import org.purescript.psi.module.PSModuleStub
import org.purescript.psi.name.*
import org.purescript.psi.newtype.PSNewTypeConstructor
import org.purescript.psi.newtype.PSNewTypeDeclaration
import org.purescript.psi.typeconstructor.PSTypeConstructor
import org.purescript.psi.typesynonym.PSTypeSynonymDeclaration
import org.purescript.psi.typevar.PSTypeVarKinded
import org.purescript.psi.typevar.PSTypeVarName

val ModuleType = object : WithPsiAndStub<PSModuleStub, PSModule>("Module") {

    override fun createStub(
        psi: PSModule,
        parent: StubElement<out PsiElement>?
    ): PSModuleStub {
        return PSModuleStub(psi.name, parent)
    }

    override fun createPsi(node: ASTNode): PsiElement {
        return PSModule(node)
    }

    override fun createPsi(stub: PSModuleStub): PSModule {
        return PSModule(stub, this)
    }

    override fun serialize(stub: PSModuleStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
    }

    override fun deserialize(
        dataStream: StubInputStream,
        parentStub: StubElement<*>?
    ): PSModuleStub {
        return PSModuleStub(dataStream.readNameString()!!, parentStub)
    }

    override fun indexStub(stub: PSModuleStub, sink: IndexSink) {
        sink.occurrence(ModuleNameIndex.KEY, stub.name)
    }

}
val FixityDeclaration = object :
    WithPsiAndStub<PSFixityDeclarationStub, PSFixityDeclaration>("FixityDeclaration") {
    override fun createPsi(node: ASTNode) = PSFixityDeclaration(node)
    override fun createPsi(stub: PSFixityDeclarationStub) =
        PSFixityDeclaration(stub, this)

    override fun createStub(psi: PSFixityDeclaration, parent: StubElement<*>?) =
        PSFixityDeclarationStub(psi.name, parent)

    override fun indexStub(stub: PSFixityDeclarationStub, sink: IndexSink) {
        // if there is a parser error the module might not exist
        stub.getParentStubOfType(PSModule::class.java)?.let { module ->
            // TODO only index exported declarations
            sink.occurrence(ExportedFixityDeclarationsIndex.KEY, module.name)
        }
    }

    override fun serialize(stub: PSFixityDeclarationStub, data: StubOutputStream) {
        data.writeName(stub.name)
    }

    override fun deserialize(
        dataStream: StubInputStream,
        parent: StubElement<*>?
    ): PSFixityDeclarationStub =
        PSFixityDeclarationStub(dataStream.readNameString()!!, parent)

}
val ExportList = WithPsi("ExportList") { PSExportList(it) }
val ExportedClass = WithPsi("ExportedClass") { PSExportedClass(it) }
val ExportedData = WithPsi("ExportedData") { PSExportedData(it) }
val ExportedDataMember =
    WithPsi("ExportedDataMember") { PSExportedDataMember(it) }
val ExportedDataMemberList =
    WithPsi("ExportedDataMemberList") { PSExportedDataMemberList(it) }
val ExportedKind = WithPsi("ExportedKind") { PSExportedKind(it) }
val ExportedModule = WithPsi("ExportedModule") { PSExportedModule(it) }
val ExportedOperator = WithPsi("ExportedOperator") { PSExportedOperator(it) }
val ExportedType = WithPsi("ExportedType") { PSExportedType(it) }
val ExportedValue = WithPsi("ExportedValue") { PSExportedValue(it) }
val Star = WithPsi("Star") { PSStar(it) }
val Bang = WithPsi("Bang") { PSBang(it) }
val RowKind = WithPsi("RowKind") { PSRowKind(it) }
val FunKind = WithPsi("FunKind") { PSFunKind(it) }
val Type = WithPsi("Type") { PSType(it) }
val TypeArgs = WithPsi("TypeArgs") { PSTypeArgs(it) }
val ForAll = WithPsi("ForAll") { PSForAll(it) }
val ConstrainedType = WithPsi("ConstrainedType") { PSConstrainedType(it) }
val Row = WithPsi("Row") { PSRow(it) }
val ObjectType = WithPsi("ObjectType") { PSObjectType(it) }
val TypeVarName = WithPsi("TypeVarName") { PSTypeVarName(it) }
val TypeVarKinded = WithPsi("TypeVarKinded") { PSTypeVarKinded(it) }
val TypeConstructor = WithPsi("TypeConstructor") { PSTypeConstructor(it) }
val TypeAtom = WithPsi("TypeAtom") { PSTypeAtom(it) }
val GenericIdentifier = WithPsi("GenericIdentifier") { PSIdentifier(it) }
val LocalIdentifier = WithPsi("LocalIdentifier") { PSIdentifier(it) }
val DataDeclaration = WithPsi("DataDeclaration") { PSDataDeclaration(it) }
val DataConstructorList =
    WithPsi("DataConstructorList") { PSDataConstructorList(it) }
val DataConstructor = WithPsi("DataConstructor") { PSDataConstructor(it) }
val Signature = WithPsi("Signature") { PSSignature(it) }
val TypeSynonymDeclaration =
    WithPsi("TypeSynonymDeclaration") { PSTypeSynonymDeclaration(it) }
val ValueDeclaration = WithPsi("ValueDeclaration") { PSValueDeclaration(it) }
val ForeignDataDeclaration =
    WithPsi("ForeignDataDeclaration") { PSForeignDataDeclaration(it) }
val ForeignValueDeclaration =
    WithPsi("ForeignValueDeclaration") { PSForeignValueDeclaration(it) }
val ImportDeclaration = WithPsi("ImportDeclaration") { PSImportDeclaration(it) }
val ImportAlias = WithPsi("ImportAlias") { PSImportAlias(it) }
val ImportList = WithPsi("ImportList") { PSImportList(it) }
val ImportedClass = WithPsi("ImportedClass") { PSImportedClass(it) }
val ImportedData = WithPsi("ImportedData") { PSImportedData(it) }
val ImportedDataMemberList =
    WithPsi("ImportedDataMemberList") { PSImportedDataMemberList(it) }
val ImportedDataMember =
    WithPsi("ImportedDataMember") { PSImportedDataMember(it) }
val ImportedKind = WithPsi("ImportedKind") { PSImportedKind(it) }
val ImportedOperator = WithPsi("ImportedOperator") { PSImportedOperator(it) }
val ImportedType = WithPsi("ImportedType") { PSImportedType(it) }
val ImportedValue = WithPsi("ImportedValue") { PSImportedValue(it) }
val PositionedDeclarationRef =
    WithPsi("PositionedDeclarationRef") { PSPositionedDeclarationRef(it) }
val ClassDeclaration = WithPsi("ClassDeclaration") { PSClassDeclaration(it) }
val ClassConstraintList =
    WithPsi("ClassConstraintList") { PSClassConstraintList(it) }
val ClassConstraint = WithPsi("ClassConstraint") { PSClassConstraint(it) }
val ClassFunctionalDependencyList =
    WithPsi("ClassFunctionalDependencyList")
    { PSClassFunctionalDependencyList(it) }
val ClassFunctionalDependency =
    WithPsi("ClassFunctionalDependency")
    { PSClassFunctionalDependency(it) }
val ClassMember = WithPsi("ClassMember") { PSClassMember(it) }
val ClassMemberList = WithPsi("ClassMemberList") { PSClassMemberList(it) }
val InstanceDeclaration =
    WithPsi("TypeInstanceDeclaration") { PSInstanceDeclaration(it) }
val NewtypeDeclaration =
    WithPsi("NewtypeDeclaration") { PSNewTypeDeclaration(it) }
val NewTypeConstructor =
    WithPsi("NewTypeConstructor") { PSNewTypeConstructor(it) }
val Guard = WithPsi("Guard") { PSGuard(it) }
val NullBinder = WithPsi("NullBinder") { PSNullBinder(it) }
val StringBinder = WithPsi("StringBinder") { PSStringBinder(it) }
val CharBinder = WithPsi("CharBinder") { PSCharBinder(it) }
val BooleanBinder = WithPsi("BooleanBinder") { PSBooleanBinder(it) }
val NumberBinder = WithPsi("NumberBinder") { PSNumberBinder(it) }
val NamedBinder = WithPsi("NamedBinder") { PSNamedBinder(it) }
val VarBinder = WithPsi("VarBinder") { PSVarBinder(it) }
val ConstructorBinder = WithPsi("ConstructorBinder") { PSConstructorBinder(it) }
val ObjectBinder = WithPsi("ObjectBinder") { PSObjectBinder(it) }
val ObjectBinderField = WithPsi("ObjectBinderField") { PSObjectBinderField(it) }
val ValueRef = WithPsi("ValueRef") { PSValueRef(it) }
val BooleanLiteral = WithPsi("BooleanLiteral") { PSBooleanLiteral(it) }
val NumericLiteral = WithPsi("NumericLiteral") { PSNumericLiteral(it) }
val StringLiteral = WithPsi("StringLiteral") { PSStringLiteral(it) }
val CharLiteral = WithPsi("CharLiteral") { PSCharLiteral(it) }
val ArrayLiteral = WithPsi("ArrayLiteral") { PSArrayLiteral(it) }
val ObjectLiteral = WithPsi("ObjectLiteral") { PSObjectLiteral(it) }
val Lambda = WithPsi("Lambda") { PSLambda(it) }
val ExpressionConstructor =
    WithPsi("ExpressionConstructor") { PSExpressionConstructor(it) }
val ExpressionIdentifier =
    WithPsi("ExpressionIdentifier") { PSExpressionIdentifier(it) }

/** Symbol is a operator in parenthesis
`(+)`
in
```
addOne = (+) 1
```
 */
val ExpressionSymbol = WithPsi("ExpressionSymbol") { PSExpressionSymbol(it) }

/**  Operator in expression
`+`
in
```
addOne a = a + 1
```
 */
val ExpressionOperator =
    WithPsi("ExpressionOperator") { PSExpressionOperator(it) }
val ExpressionWhere = WithPsi("ExpressionWhere") { PSExpressionWhere(it) }
val Case = WithPsi("Case") { PSCase(it) }
val CaseAlternative = WithPsi("CaseAlternative") { PSCaseAlternative(it) }
val IfThenElse = WithPsi("IfThenElse") { PSIfThenElse(it) }
val Let = WithPsi("Let") { PSLet(it) }
val Parens = WithPsi("Parens") { PSParens(it) }
val UnaryMinus = WithPsi("UnaryMinus") { PSUnaryMinus(it) }
val Accessor = WithPsi("Accessor") { PSAccessor(it) }
val DoBlock = WithPsi("DoBlock") { PSDoBlock(it) }
val DoNotationLet = WithPsi("DoNotationLet") { PSDoNotationLet(it) }
val DoNotationBind = WithPsi("DoNotationBind") { PSDoNotationBind(it) }
val DoNotationValue = WithPsi("DoNotationValue") { PSDoNotationValue(it) }
val Value = WithPsi("Value") { PSValue(it) }
val Fixity = WithPsi("Fixity") { PSFixity(it) }
val ModuleName = WithPsi("ModuleName") { PSModuleName(it) }
val Identifier = WithPsi("identifier") { PSIdentifier(it) }
val Symbol = WithPsi("symbol") { PSSymbol(it) }
val QualifiedSymbol = WithPsi("symbol") { PSQualifiedSymbol(it) }
val ProperName = WithPsi("ProperName") { PSProperName(it) }
val OperatorName = WithPsi("OperatorName") { PSOperatorName(it) }
val QualifiedIdentifier =
    WithPsi("QualifiedIdentifier") { PSQualifiedIdentifier(it) }
val QualifiedProperName =
    WithPsi("QualifiedProperName") { PSQualifiedProperName(it) }
val QualifiedOperatorName =
    WithPsi("QualifiedOperatorName") { PSQualifiedOperatorName(it) }
val ClassName = WithPsi("ClassName") { PSClassName(it) }
val pImplies = WithPsi("Implies") { PSImplies(it) }
val TypeHole = WithPsi("TypeHole") { PSTypeHole(it) }
