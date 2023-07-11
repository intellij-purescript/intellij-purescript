package org.purescript.parser

import org.purescript.module.Module
import org.purescript.module.declaration.Signature
import org.purescript.module.declaration.classes.*
import org.purescript.module.declaration.data.DataConstructor
import org.purescript.module.declaration.data.DataConstructorList
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.fixity.ConstructorFixityDeclaration
import org.purescript.module.declaration.fixity.PSFixity
import org.purescript.module.declaration.fixity.TypeFixityDeclaration
import org.purescript.module.declaration.fixity.ValueFixityDeclaration
import org.purescript.module.declaration.foreign.ForeignValueDecl
import org.purescript.module.declaration.foreign.PSForeignDataDeclaration
import org.purescript.module.declaration.imports.*
import org.purescript.module.declaration.newtype.NewtypeCtor
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.module.declaration.type.Labeled
import org.purescript.module.declaration.type.TypeDecl
import org.purescript.module.declaration.type.TypeParameters
import org.purescript.module.declaration.type.type.*
import org.purescript.module.declaration.value.ValueDecl
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.*
import org.purescript.module.declaration.value.binder.literals.BooleanBinder
import org.purescript.module.declaration.value.binder.literals.CharBinder
import org.purescript.module.declaration.value.binder.literals.NumberBinder
import org.purescript.module.declaration.value.binder.literals.StringBinder
import org.purescript.module.declaration.value.binder.record.PunBinder
import org.purescript.module.declaration.value.binder.record.RecordBinder
import org.purescript.module.declaration.value.binder.record.RecordLabelBinder
import org.purescript.module.declaration.value.binder.record.RecordLabelExprBinder
import org.purescript.module.declaration.value.expression.*
import org.purescript.module.declaration.value.expression.controll.Guard
import org.purescript.module.declaration.value.expression.controll.GuardBranch
import org.purescript.module.declaration.value.expression.controll.caseof.CaseAlternative
import org.purescript.module.declaration.value.expression.controll.caseof.PSCase
import org.purescript.module.declaration.value.expression.controll.ifthenelse.ErrorIf
import org.purescript.module.declaration.value.expression.controll.ifthenelse.ErrorIfThen
import org.purescript.module.declaration.value.expression.controll.ifthenelse.IfThenElse
import org.purescript.module.declaration.value.expression.dostmt.*
import org.purescript.module.declaration.value.expression.identifier.*
import org.purescript.module.declaration.value.expression.literals.*
import org.purescript.module.declaration.value.expression.namespace.Let
import org.purescript.module.declaration.value.expression.namespace.LetBinder
import org.purescript.module.declaration.value.expression.namespace.PSExpressionWhere
import org.purescript.module.declaration.value.expression.namespace.PSLambda
import org.purescript.module.declaration.value.parameters.Parameter
import org.purescript.module.declaration.value.parameters.Parameters
import org.purescript.module.exports.*
import org.purescript.name.*
import org.purescript.psi.PSElementType.WithPsi

val ModuleType = Module.Type
val FixityDeclType = ValueFixityDeclaration.Type
val ConstructorFixityDeclType = ConstructorFixityDeclaration.Type
val TypeFixityDeclType = TypeFixityDeclaration.Type
val ExportListType = ExportList.Type
val ExportedClassType = ExportedClass.Type
val ExportedDataType = ExportedData.Type
val ExportedModuleType = ExportedModule.Type
val ExportedOperatorType = ExportedOperator.Type
val ExportedTypeOperatorType = ExportedTypeOperator.Type
val ExportedValueType = ExportedValue.Type
val ExportedDataMember = PSExportedDataMember.Type
val ExportedDataMemberListType = PSExportedDataMemberList.Type
val TypeAppType = WithPsi("TypeApp") { TypeApp(it) }
val TypeOperatorExpressionType = WithPsi("TypeOperatorExpression") { TypeOperatorExpression(it) }
val TypeOperatorType = WithPsi("TypeOperatorExpression") { TypeOperatorExpression(it) }
val TypeStringType = WithPsi("TypeString") { TypeString(it) }
val TypeWildcardType = WithPsi("TypeWildcard") { TypeWildcard(it) }
val TypeIntType = WithPsi("TypeInt") { TypeInt(it) }
val KindedType = WithPsi("Kinded") { Kinded(it) }
val TypeParametersType = WithPsi("TypeParameters") { TypeParameters(it) }
val ForAllType = ForAll.Type
val TypeArrType = WithPsi("TypeArr") { TypeArr(it) }
val ConstrainedType = WithPsi("ConstrainedType") { PSConstrainedType(it) }
val LabeledType = Labeled.Type
val TypeRecordType = WithPsi("TypeRecord") { TypeRecord(it) }
val TypeVarNameType = TypeVarName.Type
val TypeIdentifierType = WithPsi("TypeIdentifier") { TypeIdentifier(it) }
val TypeParenthesisType = WithPsi("TypeParenthesis") { TypeParenthesis(it) }
val TypeRowType = WithPsi("TypeRow") { TypeRow(it) }
val TypeArrNameType = WithPsi("TypeArrName") { TypeArrName(it) }
val TypeVarKindedType = WithPsi("TypeVarKinded") { PSTypeVarKinded(it) }
val TypeCtor = WithPsi("TypeConstructor") { PSTypeConstructor(it) }
val DataDecl = DataDeclaration.Type
val DataCtorList = DataConstructorList.Type
val DataCtor = DataConstructor.Type
val SignatureType = Signature.Type
val LetBinderType = WithPsi("LetBinder") { LetBinder(it) }
val TypeDeclType = TypeDecl.Type
val ValueDeclType = ValueDecl.Type 
val ParametersType = WithPsi("Parameters") { Parameters(it) } 
val ParameterType = WithPsi("Parameter") { Parameter(it) } 
val ValueDeclarationGroupType = ValueDeclarationGroup.Type 
val ForeignDataDeclType = PSForeignDataDeclaration.Type
val ForeignValueDeclType = ForeignValueDecl.Type
val ImportType = Import.Type
val ImportAlias = WithPsi("ImportAlias") { PSImportAlias(it) }
val ImportList = PSImportList.Type
val ImportedValue = PSImportedValue.Type
val ImportedType = PSImportedType.Type
val ImportedOperator = PSImportedOperator.Type
val ImportedClass = PSImportedClass.Type
val ImportedData = PSImportedData.Type
val ImportedDataMemberList = WithPsi("ImportedDataMemberList") { PSImportedDataMemberList(it) }
val ImportedDataMember = WithPsi("ImportedDataMember") { PSImportedDataMember(it) }
val ClassDeclType = ClassDecl.Type
val ClassConstraintList = WithPsi("ClassConstraintList") { PSClassConstraintList(it) }
val ClassConstraint = WithPsi("ClassConstraint") { PSClassConstraint(it) }
val ClassFunctionalDependencyList = WithPsi("ClassFunctionalDependencyList") { PSClassFunctionalDependencyList(it) }
val ClassFunctionalDependency = WithPsi("ClassFunctionalDependency") { PSClassFunctionalDependency(it) }
val ClassMember = PSClassMember.Type
val ClassMemberList = PSClassMemberList.Type
val InstanceDeclType = PSInstanceDeclaration.Type
val NewtypeDeclType = NewtypeDecl.Type
val NewtypeCtorType = NewtypeCtor.Type
val GuardType = WithPsi("Guard") { Guard(it) }
val GuardBranchType = WithPsi("GuardBranch") { GuardBranch(it) }

/*Binders*/
val WildcardBinderType = WithPsi("WildcardBinder") { WildcardBinder(it) }
val StringBinderType = WithPsi("StringBinder") { StringBinder(it) }
val CharBinderType = WithPsi("CharBinder") { CharBinder(it) }
val BooleanBinderType = WithPsi("BooleanBinder") { BooleanBinder(it) }
val NumberBinderType = WithPsi("NumberBinder") { NumberBinder(it) }
val NamedBinderType = WithPsi("NamedBinder") { NamedBinder(it) }
val VarBinderType = WithPsi("VarBinder") { VarBinder(it) }
val RecordBinderType = WithPsi("RecordBinder") { RecordBinder(it) }
val PunBinderType = WithPsi("PunBinder") { PunBinder(it) }
val RecordLabelBinderType = WithPsi("RecordLabelBinder") { RecordLabelBinder(it) }
val RecordLabelExprBinderType = WithPsi("RecordLabelExprBinder") { RecordLabelExprBinder(it) }
val CtorBinderType = WithPsi("ConstructorBinder") { ConstructorBinder(it) }
val AppBinderType = WithPsi("AppBinder") { AppBinder(it) }
val ArrayBinderType = WithPsi("ArrayBinder") { ArrayBinder(it) }
val BinderOperatorType = WithPsi("BinderOperator") { BinderOperator(it) }
val BinderOperatorExpressionType = WithPsi("BinderOperatorExpression") { BinderOperatorExpression(it) }

val ParensBinderType = WithPsi("ParensBinder") { ParensBinder(it) }
val RecordLabelType = WithPsi("RecordLabel") { RecordLabel(it) }
val RecordUpdateType = WithPsi("RecordUpdate") { RecordUpdate(it) }
val BooleanLiteral = WithPsi("BooleanLiteral") { PSBooleanLiteral(it) }
val NumericLiteral = WithPsi("NumericLiteral") { PSNumericLiteral(it) }
val StringLiteral = WithPsi("StringLiteral") { PSStringLiteral(it) }
val CharLiteral = WithPsi("CharLiteral") { PSCharLiteral(it) }
val ArrayLiteral = WithPsi("ArrayLiteral") { PSArrayLiteral(it) }
val RecordLiteralType = WithPsi("RecordLiteral") { RecordLiteral(it) }
val Lambda = WithPsi("Lambda") { PSLambda(it) }

val ExpressionCtor = WithPsi("ExpressionConstructor") { PSExpressionConstructor(it) }
val ExpressionIdentifier = WithPsi("ExpressionIdentifier") { PSExpressionIdentifier(it) }
val ExpressionWildcardType = WithPsi("ExpressionWildcard") { ExpressionWildcard(it) }

val ExpressionHoleType = WithPsi("ExpressionHole") { ExpressionHole(it) }
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
val ExpressionOperator = WithPsi("ExpressionOperator") { PSExpressionOperator(it) }
val ExpressionWhere = WithPsi("ExpressionWhere") { PSExpressionWhere(it) }
val Case = WithPsi("Case") { PSCase(it) }
val CaseAlternativeType = WithPsi("CaseAlternative") { CaseAlternative(it) }
val IfThenElseType = WithPsi("IfThenElse") { IfThenElse(it) }
val ErrorIfThenType = WithPsi("IfThenElse") { ErrorIfThen(it) }
val ErrorIfType = WithPsi("IfThenElse") { ErrorIf(it) }
val Let = WithPsi("Let") { Let(it) }
val Parens = WithPsi("Parens") { PSParens(it) }
val TypedExpressionType = WithPsi("TypedExpression") { TypedExpression(it) }
val UnaryMinus = WithPsi("UnaryMinus") { PSUnaryMinus(it) }
val CallType = WithPsi("Call") { Call(it) }
val ArgumentType = WithPsi("Argument") { Argument(it) }
val TypeArgumentType = WithPsi("TypeArgument") { TypeArgument(it) }
val Accessor = WithPsi("Accessor") { PSAccessor(it) }
val RecordAccessType = WithPsi("RecordAccess") { RecordAccess(it) }
val DoBlock = WithPsi("DoBlock") { PSDoBlock(it) }
val EmptyDoBlockType = WithPsi("EmptyBlock") { EmptyDoBlock(it) }
val AdoBlockType = WithPsi("AdoBlock") { AdoBlock(it) }
val EmptyAdoBlockType = WithPsi("EmptyAdoBlock") { EmptyAdoBlock(it) }
val DoNotationLetType = WithPsi("DoNotationLet") { PSDoNotationLet(it) }
val DoStatementsType = WithPsi("DoNotationLet") { DoStatements(it) }
val DoNotationBindType = WithPsi("DoNotationBind") { PSDoNotationBind(it) }
val DoNotationValueType = WithPsi("DoNotationValue") { PSDoNotationValue(it) }
val OperatorExpressionType = WithPsi("OperatorExpression") { OperatorExpression(it) }
val Fixity = PSFixity.Type
val ModuleNameType = WithPsi("ModuleName") { PSModuleName(it) }
val Identifier = WithPsi("identifier") { PSIdentifier(it) }
val Symbol = WithPsi("symbol") { PSSymbol(it) }
val QualifiedSymbol = WithPsi("symbol") { PSQualifiedSymbol(it) }
val ProperName = WithPsi("ProperName") { PSProperName(it) }
val OperatorName = WithPsi("OperatorName") { PSOperatorName(it) }
val QualifiedIdentifier = WithPsi("QualifiedIdentifier") { PSQualifiedIdentifier(it) }
val QualifiedProperName = WithPsi("QualifiedProperName") { PSQualifiedProperName(it) }
val QualifiedOperatorName = WithPsi("QualifiedOperatorName") { PSQualifiedOperatorName(it) }
val ClassName = WithPsi("ClassName") { PSClassName(it) }
val pImplies = WithPsi("Implies") { PSImplies(it) }
val TypeHole = WithPsi("TypeHole") { PSTypeHole(it) }
