package org.purescript.parser

import org.purescript.psi.*
import org.purescript.psi.PSElementType.WithPsi
import org.purescript.psi.binder.*
import org.purescript.psi.caseof.PSCaseAlternative
import org.purescript.psi.caseof.PSConstrainedType
import org.purescript.psi.char.PSCharBinder
import org.purescript.psi.char.PSCharLiteral
import org.purescript.psi.declaration.classes.*
import org.purescript.psi.declaration.data.DataConstructor
import org.purescript.psi.declaration.data.DataDeclaration
import org.purescript.psi.declaration.data.DataConstructorList
import org.purescript.psi.declaration.fixity.FixityDeclaration
import org.purescript.psi.declaration.fixity.PSFixity
import org.purescript.psi.declaration.imports.*
import org.purescript.psi.declaration.signature.PSSignature
import org.purescript.psi.declaration.value.PSValueDeclaration
import org.purescript.psi.dostmt.PSDoBlock
import org.purescript.psi.dostmt.PSDoNotationBind
import org.purescript.psi.dostmt.PSDoNotationLet
import org.purescript.psi.dostmt.PSDoNotationValue
import org.purescript.psi.exports.*
import org.purescript.psi.expression.*
import org.purescript.psi.foreign.PSForeignDataDeclaration
import org.purescript.psi.foreign.PSForeignValueDeclaration
import org.purescript.psi.imports.*
import org.purescript.psi.literals.*
import org.purescript.psi.module.Module
import org.purescript.psi.name.*
import org.purescript.psi.declaration.newtype.PSNewTypeConstructor
import org.purescript.psi.declaration.newtype.PSNewTypeDeclaration
import org.purescript.psi.type.*
import org.purescript.psi.type.typeconstructor.PSTypeConstructor
import org.purescript.psi.declaration.typesynonym.PSTypeSynonymDeclaration
import org.purescript.psi.type.PSTypeVarKinded
import org.purescript.psi.type.PSTypeVarName

val ModuleType = Module.Type
val FixityDeclType = FixityDeclaration.Type
val ExportListType = ExportList.Type
val ExportedClassType = ExportedClass.Type
val ExportedDataType = ExportedData.Type
val ExportedModuleType = ExportedModule.Type
val ExportedOperatorType = ExportedOperator.Type
val ExportedTypeType = ExportedType.Type
val ExportedValueType = ExportedValue.Type
val ExportedDataMember = WithPsi("ExportedDataMember") { PSExportedDataMember(it) }
val ExportedDataMemberList = WithPsi("ExportedDataMemberList") { PSExportedDataMemberList(it) }
val Type = WithPsi("Type") { PSType(it) }
val TypeArgs = WithPsi("TypeArgs") { PSTypeArgs(it) }
val ForAll = WithPsi("ForAll") { PSForAll(it) }
val ConstrainedType = WithPsi("ConstrainedType") { PSConstrainedType(it) }
val Row = WithPsi("Row") { PSRow(it) }
val ObjectType = WithPsi("ObjectType") { PSObjectType(it) }
val TypeVarName = WithPsi("TypeVarName") { PSTypeVarName(it) }
val TypeVarKinded = WithPsi("TypeVarKinded") { PSTypeVarKinded(it) }
val TypeCtor = WithPsi("TypeConstructor") { PSTypeConstructor(it) }
val TypeAtom = WithPsi("TypeAtom") { PSTypeAtom(it) }
val DataDecl = DataDeclaration.Type
val DataCtorList = DataConstructorList.Type
val DataCtor = DataConstructor.Type
val Signature = WithPsi("Signature") { PSSignature(it) }
val TypeSynonymDecl =
    WithPsi("TypeSynonymDeclaration") { PSTypeSynonymDeclaration(it) }
val ValueDecl = WithPsi("ValueDeclaration") { PSValueDeclaration(it) }
val ForeignDataDecl =
    WithPsi("ForeignDataDeclaration") { PSForeignDataDeclaration(it) }
val ForeignValueDecl =
    WithPsi("ForeignValueDeclaration") { PSForeignValueDeclaration(it) }
val ImportType = Import.Type
val ImportAlias = WithPsi("ImportAlias") { PSImportAlias(it) }
val ImportList = WithPsi("ImportList") { PSImportList(it) }
val ImportedClass = WithPsi("ImportedClass") { PSImportedClass(it) }
val ImportedData = WithPsi("ImportedData") { PSImportedData(it) }
val ImportedDataMemberList =
    WithPsi("ImportedDataMemberList") { PSImportedDataMemberList(it) }
val ImportedDataMember =
    WithPsi("ImportedDataMember") { PSImportedDataMember(it) }
val ImportedOperator = WithPsi("ImportedOperator") { PSImportedOperator(it) }
val ImportedType = WithPsi("ImportedType") { PSImportedType(it) }
val ImportedValue = WithPsi("ImportedValue") { PSImportedValue(it) }
val ClassDecl = WithPsi("ClassDeclaration") { PSClassDeclaration(it) }
val ClassConstraintList =
    WithPsi("ClassConstraintList") { PSClassConstraintList(it) }
val ClassConstraint = WithPsi("ClassConstraint") { PSClassConstraint(it) }
val ClassFunctionalDependencyList =
    WithPsi("ClassFunctionalDependencyList")
    { PSClassFunctionalDependencyList(it) }
val ClassFunctionalDependency =
    WithPsi("ClassFunctionalDependency") { PSClassFunctionalDependency(it) }
val ClassMember = WithPsi("ClassMember") { PSClassMember(it) }
val ClassMemberList = WithPsi("ClassMemberList") { PSClassMemberList(it) }
val InstanceDecl =
    WithPsi("TypeInstanceDeclaration") { PSInstanceDeclaration(it) }
val NewtypeDecl = WithPsi("NewtypeDeclaration") { PSNewTypeDeclaration(it) }
val NewTypeCtor = WithPsi("NewTypeConstructor") { PSNewTypeConstructor(it) }
val Guard = WithPsi("Guard") { PSGuard(it) }
val NullBinder = WithPsi("NullBinder") { PSNullBinder(it) }
val StringBinder = WithPsi("StringBinder") { PSStringBinder(it) }
val CharBinder = WithPsi("CharBinder") { PSCharBinder(it) }
val BooleanBinder = WithPsi("BooleanBinder") { PSBooleanBinder(it) }
val NumberBinder = WithPsi("NumberBinder") { PSNumberBinder(it) }
val NamedBinder = WithPsi("NamedBinder") { PSNamedBinder(it) }
val VarBinder = WithPsi("VarBinder") { PSVarBinder(it) }
val CtorBinder = WithPsi("ConstructorBinder") { PSConstructorBinder(it) }
val ObjectBinder = WithPsi("ObjectBinder") { PSObjectBinder(it) }
val ObjectBinderField = WithPsi("ObjectBinderField") { PSObjectBinderField(it) }
val BooleanLiteral = WithPsi("BooleanLiteral") { PSBooleanLiteral(it) }
val NumericLiteral = WithPsi("NumericLiteral") { PSNumericLiteral(it) }
val StringLiteral = WithPsi("StringLiteral") { PSStringLiteral(it) }
val CharLiteral = WithPsi("CharLiteral") { PSCharLiteral(it) }
val ArrayLiteral = WithPsi("ArrayLiteral") { PSArrayLiteral(it) }
val ObjectLiteral = WithPsi("ObjectLiteral") { PSObjectLiteral(it) }
val Lambda = WithPsi("Lambda") { PSLambda(it) }
val ExpressionCtor =
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
