package org.purescript.parser

import org.purescript.psi.PSElementType

interface PSElements {
    companion object {

        @kotlin.jvm.JvmField
        val Module = PSElementType("Module")

        @kotlin.jvm.JvmField
        val ExportList = PSElementType("ExportList")

        @kotlin.jvm.JvmField
        val ExportedClass = PSElementType("ExportedClass")

        @kotlin.jvm.JvmField
        val ExportedData = PSElementType("ExportedData")

        @kotlin.jvm.JvmField
        val ExportedDataMember = PSElementType("ExportedDataMember")

        @kotlin.jvm.JvmField
        val ExportedDataMemberList = PSElementType("ExportedDataMemberList")

        @kotlin.jvm.JvmField
        val ExportedKind = PSElementType("ExportedKind")

        @kotlin.jvm.JvmField
        val ExportedModule = PSElementType("ExportedModule")

        @kotlin.jvm.JvmField
        val ExportedOperator = PSElementType("ExportedOperator")

        @kotlin.jvm.JvmField
        val ExportedType = PSElementType("ExportedType")

        @kotlin.jvm.JvmField
        val ExportedValue = PSElementType("ExportedValue")

        @kotlin.jvm.JvmField
        val Star = PSElementType("Star")

        @kotlin.jvm.JvmField
        val Bang = PSElementType("Bang")

        @kotlin.jvm.JvmField
        val RowKind = PSElementType("RowKind")

        @kotlin.jvm.JvmField
        val FunKind = PSElementType("FunKind")

        @kotlin.jvm.JvmField
        val DocComment = PSElementType("DocComment")

        @kotlin.jvm.JvmField
        val Qualified = PSElementType("Qualified")

        @kotlin.jvm.JvmField
        val Type = PSElementType("Type")

        @kotlin.jvm.JvmField
        val TypeArgs = PSElementType("TypeArgs")

        @kotlin.jvm.JvmField
        val TypeAnnotationName = PSElementType("TypeAnnotationName")

        @kotlin.jvm.JvmField
        val ForAll = PSElementType("ForAll")

        @kotlin.jvm.JvmField
        val ConstrainedType = PSElementType("ConstrainedType")

        @kotlin.jvm.JvmField
        val Row = PSElementType("Row")

        @kotlin.jvm.JvmField
        val ObjectType = PSElementType("ObjectType")

        @kotlin.jvm.JvmField
        val TypeVar = PSElementType("TypeVar")

        @kotlin.jvm.JvmField
        val TypeVarName = PSElementType("TypeVarName")

        @kotlin.jvm.JvmField
        val TypeVarKinded = PSElementType("TypeVarKinded")

        @kotlin.jvm.JvmField
        val TypeConstructor = PSElementType("TypeConstructor")

        @kotlin.jvm.JvmField
        val TypeAtom = PSElementType("TypeAtom")

        @kotlin.jvm.JvmField
        val GenericIdentifier = PSElementType("GenericIdentifier")

        @kotlin.jvm.JvmField
        val LocalIdentifier = PSElementType("LocalIdentifier")

        @kotlin.jvm.JvmField
        val DataDeclaration = PSElementType("DataDeclaration")

        @kotlin.jvm.JvmField
        val DataConstructorList = PSElementType("DataConstructorList")

        @kotlin.jvm.JvmField
        val DataConstructor = PSElementType("DataConstructor")

        @kotlin.jvm.JvmField
        val TypeDeclaration = PSElementType("TypeDeclaration")

        @kotlin.jvm.JvmField
        val TypeSynonymDeclaration = PSElementType("TypeSynonymDeclaration")

        @kotlin.jvm.JvmField
        val ValueDeclaration = PSElementType("ValueDeclaration")

        @kotlin.jvm.JvmField
        val ExternDataDeclaration = PSElementType("ExternDataDeclaration")

        @kotlin.jvm.JvmField
        val ExternInstanceDeclaration = PSElementType("ExternInstanceDeclaration")

        @kotlin.jvm.JvmField
        val ForeignValueDeclaration = PSElementType("ForeignValueDeclaration")

        @kotlin.jvm.JvmField
        val FixityDeclaration = PSElementType("FixityDeclaration")

        @kotlin.jvm.JvmField
        val ImportDeclaration = PSElementType("ImportDeclaration")

        @kotlin.jvm.JvmField
        val ImportAlias = PSElementType("ImportAlias")

        @kotlin.jvm.JvmField
        val ImportList = PSElementType("ImportList")

        @kotlin.jvm.JvmField
        val ImportedClass = PSElementType("ImportedClass")

        @kotlin.jvm.JvmField
        val ImportedData = PSElementType("ImportedData")

        @kotlin.jvm.JvmField
        val ImportedDataMemberList = PSElementType("ImportedDataMemberList")

        @kotlin.jvm.JvmField
        val ImportedDataMember = PSElementType("ImportedDataMember")

        @kotlin.jvm.JvmField
        val ImportedKind = PSElementType("ImportedKind")

        @kotlin.jvm.JvmField
        val ImportedOperator = PSElementType("ImportedOperator")

        @kotlin.jvm.JvmField
        val ImportedType = PSElementType("ImportedType")

        @kotlin.jvm.JvmField
        val ImportedValue = PSElementType("ImportedValue")

        @kotlin.jvm.JvmField
        val PositionedDeclarationRef = PSElementType("PositionedDeclarationRef")

        @kotlin.jvm.JvmField
        val ClassDeclaration = PSElementType("ClassDeclaration")

        @kotlin.jvm.JvmField
        val ClassConstraintList = PSElementType("ClassConstraintList")

        @kotlin.jvm.JvmField
        val ClassConstraint = PSElementType("ClassConstraint")

        @kotlin.jvm.JvmField
        val ClassFunctionalDependencyList = PSElementType("ClassFunctionalDependencyList")

        @kotlin.jvm.JvmField
        val ClassFunctionalDependency = PSElementType("ClassFunctionalDependency")

        @kotlin.jvm.JvmField
        val ClassMember = PSElementType("ClassMember")

        @kotlin.jvm.JvmField
        val ClassMemberList = PSElementType("ClassMemberList")

        @kotlin.jvm.JvmField
        val TypeInstanceDeclaration = PSElementType("TypeInstanceDeclaration")

        @kotlin.jvm.JvmField
        val NewtypeDeclaration = PSElementType("NewtypeDeclaration")

        @kotlin.jvm.JvmField
        val NewTypeConstructor = PSElementType("NewTypeConstructor")

        @kotlin.jvm.JvmField
        val Guard = PSElementType("Guard")

        @kotlin.jvm.JvmField
        val NullBinder = PSElementType("NullBinder")

        @kotlin.jvm.JvmField
        val StringBinder = PSElementType("StringBinder")

        @kotlin.jvm.JvmField
        val CharBinder = PSElementType("CharBinder")

        @kotlin.jvm.JvmField
        val BooleanBinder = PSElementType("BooleanBinder")

        @kotlin.jvm.JvmField
        val NumberBinder = PSElementType("NumberBinder")

        @kotlin.jvm.JvmField
        val NamedBinder = PSElementType("NamedBinder")

        @kotlin.jvm.JvmField
        val VarBinder = PSElementType("VarBinder")

        @kotlin.jvm.JvmField
        val ConstructorBinder = PSElementType("ConstructorBinder")

        @kotlin.jvm.JvmField
        val ObjectBinder = PSElementType("ObjectBinder")

        @kotlin.jvm.JvmField
        val ObjectBinderField = PSElementType("ObjectBinderField")

        @kotlin.jvm.JvmField
        val BinderAtom = PSElementType("BinderAtom")

        @kotlin.jvm.JvmField
        val Binder = PSElementType("Binder")

        @kotlin.jvm.JvmField
        val ValueRef = PSElementType("ValueRef")

        @kotlin.jvm.JvmField
        val BooleanLiteral = PSElementType("BooleanLiteral")

        @kotlin.jvm.JvmField
        val NumericLiteral = PSElementType("NumericLiteral")

        @kotlin.jvm.JvmField
        val StringLiteral = PSElementType("StringLiteral")

        @kotlin.jvm.JvmField
        val CharLiteral = PSElementType("CharLiteral")

        @kotlin.jvm.JvmField
        val ArrayLiteral = PSElementType("ArrayLiteral")

        @kotlin.jvm.JvmField
        val ObjectLiteral = PSElementType("ObjectLiteral")

        @kotlin.jvm.JvmField
        val Abs = PSElementType("Abs")

        @kotlin.jvm.JvmField
        val IdentInfix = PSElementType("IdentInfix")

        @kotlin.jvm.JvmField
        val Var = PSElementType("Var")

        @kotlin.jvm.JvmField
        val ExpressionConstructor = PSElementType("ExpressionConstructor")

        @kotlin.jvm.JvmField
        val Case = PSElementType("Case")

        @kotlin.jvm.JvmField
        val CaseAlternative = PSElementType("CaseAlternative")

        @kotlin.jvm.JvmField
        val IfThenElse = PSElementType("IfThenElse")

        @kotlin.jvm.JvmField
        val Let = PSElementType("Let")

        @kotlin.jvm.JvmField
        val Parens = PSElementType("Parens")

        @kotlin.jvm.JvmField
        val UnaryMinus = PSElementType("UnaryMinus")

        @kotlin.jvm.JvmField
        val Accessor = PSElementType("Accessor")

        @kotlin.jvm.JvmField
        val DoNotationLet = PSElementType("DoNotationLet")

        @kotlin.jvm.JvmField
        val DoNotationBind = PSElementType("DoNotationBind")

        @kotlin.jvm.JvmField
        val DoNotationValue = PSElementType("DoNotationValue")

        @kotlin.jvm.JvmField
        val Value = PSElementType("Value")

        @kotlin.jvm.JvmField
        val Fixity = PSElementType("Fixity")

        @kotlin.jvm.JvmField
        val JSRaw = PSElementType("JavaScript")

        @kotlin.jvm.JvmField
        val ModuleName = PSElementType("ModuleName")

        @kotlin.jvm.JvmField
        val Identifier = PSElementType("identifier")

        @kotlin.jvm.JvmField
        val ProperName = PSElementType("ProperName")

        @kotlin.jvm.JvmField
        val OperatorName = PSElementType("OperatorName")

        @kotlin.jvm.JvmField
        val QualifiedIdentifier = PSElementType("QualifiedIdentifier")

        @kotlin.jvm.JvmField
        val QualifiedProperName = PSElementType("QualifiedProperName")

        @kotlin.jvm.JvmField
        val QualifiedOperatorName = PSElementType("QualifiedOperatorName")

        @kotlin.jvm.JvmField
        val importModuleName = PSElementType("ImportModuleName")

        @kotlin.jvm.JvmField
        val pClassName = PSElementType("ClassName")

        @kotlin.jvm.JvmField
        val pImplies = PSElementType("Implies")

        @kotlin.jvm.JvmField
        val TypeHole = PSElementType("TypeHole")
    }
}
