package net.kenro.ji.jin.purescript.psi;

public interface PSElements {
    PSElementType Identifier = new PSElementType("identifier");
    PSElementType ProperName = new PSElementType("ProperName");

    PSElementType Program = new PSElementType("Program");
    PSElementType Module = new PSElementType("Module");

    PSElementType Star = new PSElementType("Star");
    PSElementType Bang = new PSElementType("Bang");
    PSElementType RowKind = new PSElementType("RowKind");
    PSElementType FunKind = new PSElementType("FunKind");

    PSElementType Qualified = new PSElementType("Qualified");
    PSElementType Type = new PSElementType("Type");
    PSElementType TypeArgs = new PSElementType("TypeArgs");
    PSElementType TypeAnnotationName = new PSElementType("TypeAnnotationName");

    PSElementType ForAll = new PSElementType("ForAll");
    PSElementType ConstrainedType = new PSElementType("ConstrainedType");
    PSElementType Row = new PSElementType("Row");
    PSElementType ObjectType = new PSElementType("ObjectType");
    PSElementType TypeVar = new PSElementType("TypeVar");
    PSElementType TypeConstructor = new PSElementType("TypeConstructor");
    PSElementType TypeApp = new PSElementType("TypeApp");
    PSElementType TypeAtom = new PSElementType("TypeAtom");
    PSElementType PolyType = new PSElementType("PolyType");
    PSElementType GenericIdentifier = new PSElementType("GenericIdentifier");
    PSElementType LocalIdentifier = new PSElementType("LocalIdentifier");

    PSElementType DataDeclaration = new PSElementType("DataDeclaration");
    PSElementType PositionedDeclaration = new PSElementType("PositionedDeclaration");
    PSElementType TypeDeclaration = new PSElementType("TypeDeclaration");
    PSElementType TypeSynonymDeclaration = new PSElementType("TypeSynonymDeclaration");
    PSElementType ValueDeclaration = new PSElementType("ValueDeclaration");
    PSElementType ExternDataDeclaration = new PSElementType("ExternDataDeclaration");
    PSElementType ExternInstanceDeclaration = new PSElementType("ExternInstanceDeclaration");
    PSElementType ExternDeclaration = new PSElementType("ExternDeclaration");
    PSElementType FixityDeclaration = new PSElementType("FixityDeclaration");
    PSElementType ImportDeclaration = new PSElementType("ImportDeclaration");
    PSElementType LocalDeclaration = new PSElementType("LocalDeclaration");
    PSElementType PositionedDeclarationRef = new PSElementType("PositionedDeclarationRef");
    PSElementType TypeClassDeclaration = new PSElementType("TypeClassDeclaration");
    PSElementType TypeInstanceDeclaration = new PSElementType("TypeInstanceDeclaration");
    PSElementType NewtypeDeclaration = new PSElementType("NewtypeDeclaration");

    PSElementType Guard = new PSElementType("Guard");
    PSElementType NullBinder = new PSElementType("NullBinder");
    PSElementType StringBinder = new PSElementType("StringBinder");
    PSElementType BooleanBinder = new PSElementType("BooleanBinder");
    PSElementType NumberBinder = new PSElementType("NumberBinder");
    PSElementType NamedBinder = new PSElementType("NamedBinder");
    PSElementType VarBinder = new PSElementType("VarBinder");
    PSElementType ConstructorBinder = new PSElementType("ConstructorBinder");
    PSElementType ObjectBinder = new PSElementType("ObjectBinder");
    PSElementType ObjectBinderField = new PSElementType("ObjectBinderField");
    PSElementType BinderAtom = new PSElementType("BinderAtom");
    PSElementType Binder = new PSElementType("Binder");

    PSElementType ValueRef = new PSElementType("ValueRef");

    PSElementType BooleanLiteral = new PSElementType("BooleanLiteral");
    PSElementType NumericLiteral = new PSElementType("NumericLiteral");
    PSElementType StringLiteral = new PSElementType("StringLiteral");
    PSElementType ArrayLiteral = new PSElementType("ArrayLiteral");
    PSElementType ObjectLiteral = new PSElementType("ObjectLiteral");

    PSElementType Abs = new PSElementType("Abs");
    PSElementType IdentInfix = new PSElementType("IdentInfix");
    PSElementType Var = new PSElementType("Var");
    PSElementType Constructor = new PSElementType("Constructor");
    PSElementType Case = new PSElementType("Case");
    PSElementType CaseAlternative = new PSElementType("CaseAlternative");
    PSElementType IfThenElse = new PSElementType("IfThenElse");
    PSElementType Let = new PSElementType("Let");
    PSElementType Parens = new PSElementType("Parens");
    PSElementType UnaryMinus = new PSElementType("UnaryMinus");
    PSElementType PrefixValue = new PSElementType("PrefixValue");
    PSElementType Accessor = new PSElementType("Accessor");
    PSElementType DoNotationLet = new PSElementType("DoNotationLet");
    PSElementType DoNotationBind = new PSElementType("DoNotationBind");
    PSElementType DoNotationValue = new PSElementType("DoNotationValue");
    PSElementType Value = new PSElementType("Value");

    PSElementType Fixity = new PSElementType("Fixity");
    PSElementType JSRaw = new PSElementType("JavaScript");
    PSElementType pModuleName = new PSElementType("ModuleName");
    PSElementType importModuleName = new PSElementType("ImportModuleName");
    PSElementType qualifiedModuleName = new PSElementType("QualifiedModuleName");
    PSElementType pClassName = new PSElementType("ClassName");
    PSElementType pImplies = new PSElementType("Implies");
    PSElementType TypeHole = new PSElementType("TypeHole");
}

