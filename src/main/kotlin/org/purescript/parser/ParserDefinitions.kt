package org.purescript.parser

import org.purescript.parser.dsl.MatchName
import org.purescript.parser.dsl.OnOffside
import org.purescript.parser.dsl.SameLineOrIndented
import org.purescript.parser.dsl.Dedent
import org.purescript.parser.dsl.SetName
import org.purescript.parser.dsl.SetOffside

// Literals
val boolean = `'true'` / `'false'`
val number = NumericLiteral(NATURAL / FLOAT)
val moduleName = Choice(
    ModuleNameType(MODULE_PREFIX + PROPER_NAME),
    ModuleNameType(PROPER_NAME)
)
val qualifier = ModuleNameType(MODULE_PREFIX)

// Utils
fun qualified(p: DSL) = Choice(qualifier + p, p)
fun braces(p: DSL) = LCURLY + p + RCURLY
fun parens(p: DSL) = LPAREN + p + RPAREN
fun squares(p: DSL) = LBRACK + p + RBRACK
fun block(p:DSL) = SetOffside(+OnOffside(p))


// TODO: add 'representational' and 'phantom'
val ident = Identifier(Choice(LOWER.dsl, `'as'`, `'hiding'`, `'role'`, `'nominal'`))
val label = Identifier(
    Choice(
        LOWER.dsl,
        string,
        `'ado'`,
        `'as'`,
        `'case'`,
        `'class'`,
        `'data'`,
        `'derive'`,
        `'do'`,
        `'else'`,
        `'false'`,
        `'forall'`,
        `'foreign'`,
        `'hiding'`,
        `'import'`,
        `'if'`,
        `'in'`,
        `'infix'`,
        `'infixl'`,
        `'infixr'`,
        `'instance'`,
        `'let'`,
        `'module'`,
        `'newtype'`,
        `'nominal'`,
        `'of'`,
        // TODO: phantom
        // TODO: representational
        `'role'`,
        `'then'`,
        `'true'`,
        `'type'`,
        `'where'`
    )
)

// this doesn't match parser.y but i dont feel like changing it right now
// it might be due to differences in the lexer
val operator = OPERATOR.dsl / `@` / dot / ddot / ldarrow / OPTIMISTIC.dsl / `-` / colon
val properName: DSL = ProperName(PROPER_NAME)

/**
 * ProperName with optional qualification
 */
val qualProperName = QualifiedProperName(qualified(properName))
val typeRef = Reference { type }
val type: DSL = KindedType.cont(Reference { type1 }, dcolon + typeRef)
val typeVarPlain = Choice(
    TypeVarNameType(ident),
    TypeVarKindedType(parens(TypeVarNameType(ident) + dcolon + type))
)
val typeVar = Choice(
    `@` + typeVarPlain,
    typeVarPlain
)
val rowLabel = LabeledType(label + dcolon + type.relax("malformed type"))
val row = Choice(
    `|` + type,
    rowLabel + !+(`,` + rowLabel.relaxTo(RCURLY.dsl / `,`, "malformed row label")).heal + !(`|` + type)
)

val typeAtom: DSL = Choice(
    TypeHole(hole),
    TypeRecordType(LCURLY + RCURLY),
    TypeRecordType(braces(row)),
    TypeWildcardType(`_`),
    TypeStringType(string),
    TypeIntType(number),
    TypeCtor(qualProperName),
    TypeIdentifierType(ident),
    TypeRowType(LPAREN + RPAREN),
    TypeArrNameType(parens(arrow)),
    TypeRowType(parens(row)),
    TypeParenthesisType(parens(type))
)
val binderAtomRef: DSL = Reference { binderAtom }
val binderAtom: DSL =
    Choice(
        WildcardBinderType(`_`),
        CtorBinderType(qualProperName),
        NamedBinderType(VarBinderType(ident) + `@` + binderAtomRef),
        VarBinderType(ident),
        CharBinderType(char),
        StringBinderType(string),
        NumberBinderType(number),
        ArrayBinderType(squares(Reference { binder }.sepBy(`,`))),
        RecordBinderType(recordLayout(Reference { recordBinder }, "record binder")),
        ParensBinderType(parens(Reference { binder })),
        BooleanBinderType(boolean)
    )
val binder: DSL = Reference { binder1 } + !(dcolon + type)
val operatorName = OperatorName(operator)
val qualOp = QualifiedOperatorName(qualified(operatorName))
val type5: DSL = TypeAppType.fold(typeAtom, SameLineOrIndented(typeAtom))
val type4 = Choice(
    TypeIntType(`-` + number),
    type5
)
val type3 = TypeOperatorExpressionType.cont(
    type4,
    qualOp + type4.sepBy1(qualOp)
)

/**
 * Function or constraint
 */
val type2: DSL = ChoiceMap(
    type3,
    arrow + Reference { type1 } to TypeArrType,
    darrow + Reference { type1 } to ConstrainedType
)

/**
 * Forall
 */
val type1 = Choice(
    ForAllType(`'forall'` + +typeVar + dot + type2),
    type2
)

val expr = TypedExpressionType.cont(Reference { expr1 }, dcolon + type)

val `expr?` = expr.relax("missing expression")
val propertyUpdate: DSL = label + !eq + expr
val symbol = Symbol(parens(operatorName))

val recordLabel = RecordLabelType(
    ((label + ":").heal + expr.relaxTo(RCURLY.dsl / `,`, "malformed expression")) / ((label + eq).heal + expr.relaxTo(
        RCURLY.dsl / `,`,
        "malformed expression"
    )) / ExpressionIdentifier(QualifiedIdentifier(label)).relaxTo(RCURLY.dsl / `,`, "malformed label")
)

/**
 * exprAtom :: { Expr () }
 *   : '_' { ExprSection () $1 }
 *   | hole { ExprHole () $1 }
 *   | qualIdent { ExprIdent () $1 }
 *   | qualProperName { ExprConstructor () (getQualifiedProperName $1) }
 *   | qualSymbol { ExprOpName () (getQualifiedOpName $1) }
 *   | boolean { uncurry (ExprBoolean ()) $1 }
 *   | char { uncurry (ExprChar ()) $1 }
 *   | string { uncurry (ExprString ()) $1 }
 *   | number { uncurry (ExprNumber ()) $1 }
 *   | delim('[', expr, ',', ']') { ExprArray () $1 }
 *   | delim('{', recordLabel, ',', '}') { ExprRecord () $1 }
 *   | '(' expr ')' { ExprParens () (Wrapped $1 $2 $3) }
 *   */
val exprAtom = Choice(
    ExpressionWildcardType(`_`),
    ExpressionHoleType(hole),
    ExpressionIdentifier(QualifiedIdentifier(qualified(ident))),
    ExpressionSymbol(QualifiedSymbol(qualified(symbol))),
    ExpressionCtor(qualProperName),
    BooleanLiteral(boolean),
    CharLiteral(char),
    StringLiteral(string),
    number,
    ArrayLiteral(squares(!(expr + !+(`,` + expr.relax("missing array element"))))),
    RecordLiteralType(recordLayout(recordLabel, "record label")),
    Parens(parens(expr.relax("empty parenthesis")))
)
val expr7 = RecordAccessType.fold(exprAtom, dot + Accessor(label))

val badSingleCaseBranch =
    Reference { binder1 + (arrow + exprWhere) / ((arrow + exprWhere).heal / !+guardedCaseExpr) }

/*
* if there is only one case branch it can ignore layout so we need
* to allow layout end at any time.
*/
val exprCase: DSL =
    Case(`'case'` + `expr?`.sepBy1(`,`) + `'of'` + block(Reference{ caseBranch }))


val expr5 = Reference {
    Choice(
        RecordUpdateType(recordLayout1(propertyUpdate, "property update")),
        expr7,
        Lambda(
            backslash +
                    ParametersType(!+ParameterType(binderAtom)) +
                    arrow.relax("missing lambda arrow") +
                    expr.relax("missing expression in lambda")
        ),
        exprCase,
        ifThenElse,
        doBlock,
        ChoiceMap(
            qualified(`'ado'`),
            SetOffside(DoStatementsType.fold(DoStatementsType(OnOffside(doStatement)), OnOffside(doStatement))) +
                    `'in'` + SetOffside(expr) to AdoBlockType,
            `'in'` + SetOffside(expr) to EmptyAdoBlockType,
        ),
        letIn,
        EmptyDoBlockType(DoStatementsType(letStatement).error("let statement outside of do"))
    )
}

/**
 * Function application
 */
val expr4 = CallType.fold(
    expr5, SameLineOrIndented(ArgumentType(expr5) / TypeArgumentType(`@` + typeAtom))
)

val expr3 = UnaryMinus(+`-` + expr4) / expr4
val exprBacktick2 = expr3.sepBy1(qualOp)
val expr2 = expr3.sepBy1(tick + exprBacktick2 + tick)
val expr1: DSL = (OperatorExpressionType.cont(
    expr2,
    ExpressionOperator(qualOp) + expr2.sepBy1(ExpressionOperator(qualOp.heal))
) + !(ExpressionOperator(qualOp.heal) + expr2.relax("missing value")).heal) /
        parens(`_`.relax("missing hole") + qualOp + expr2).heal
val patternGuard = !(binder + larrow).heal + Reference { expr1 }
val guard = GuardType(`|` + patternGuard.sepBy(`,`))
val dataCtor = DataCtor(properName + !+SameLineOrIndented(typeAtom))

val exprWhere: DSL =`expr?` + !Dedent(ExpressionWhere(`'where'` + block(Reference { letBinding })))

val guardedDeclExpr = GuardBranchType(guard + eq + exprWhere)
val guardedDecl = Choice(
    eq.heal + exprWhere.relax("Missing Value"),
    +guardedDeclExpr
)
val namedValueDecl = ValueDeclType(
    MatchName(ident.heal) + Choice(
        ParametersType(+ParameterType(binderAtom)) + guardedDecl,
        Empty(ParametersType, before = guardedDecl)
    )
)

val valueDeclarationGroup = ValueDeclarationGroupType(
    SetName(
        +Choice(
            SignatureType(MatchName(ident.heal) + dcolon + type),
            namedValueDecl
        )
    )
)
val instBinder = Choice((ident + dcolon) + type, valueDeclarationGroup)
val foreignDeclaration = `'foreign'` + `'import'` + Choice(
    ForeignDataDeclType(`'data'` + properName + dcolon + type), ForeignValueDeclType(ident.heal + dcolon + type)
)
val fixity = Fixity(`'infixl'` / `'infixr'` / `'infix'` + NATURAL)
val qualIdentifier = QualifiedIdentifier(!qualifier + ident)

// TODO Should we differentiate Types and DataConstructors?
// that would mean that when there is a `type` prefix we parse as Type
// otherwise if it's a capital name it's a DataConstructor
val fixityDeclaration = ChoiceMap(
    fixity,
    `'type'` + qualProperName + `'as'` + operatorName to TypeFixityDeclType,
    qualProperName + `'as'` + operatorName to ConstructorFixityDeclType,
    qualIdentifier + `'as'` + operatorName to FixityDeclType
)

val fundep = ClassFunctionalDependency(type)
val fundeps = `|` + fundep.sepBy1(`,`)
val constraint = ClassConstraint(ClassName(qualProperName) + !+SameLineOrIndented(typeAtom))
val constraints = parens(constraint.sepBy1(`,`)) / constraint
val classSuper = ClassConstraintList(constraints + pImplies(ldarrow))
val classNameAndFundeps = ClassName(properName) + !+typeVar + !ClassFunctionalDependencyList(fundeps)
val classSignature = ClassName(properName) + dcolon + type

// this first is described in haskell code and not in normal happy expression
// see `fmap (Left . DeclKindSignature () $1) parseClassSignature`
val classHead = `'class'` + classSignature.heal / (!classSuper.heal + classNameAndFundeps)
val classMember = valueDeclarationGroup
val classDeclaration =
    ClassDeclType(classHead + !ClassMemberList(`'where'` + block(classMember)).heal)

val instName = ident + dcolon
val instConstraint = TypeCtor(qualProperName) + !+SameLineOrIndented(typeAtom)
val instConstraints = parens((instConstraint).sepBy1(`,`)) / instConstraint + darrow
val instHead = `'instance'` + !instName + !instConstraints.heal + (TypeCtor(qualProperName) + !+SameLineOrIndented(typeAtom))

val importedDataMembers = ImportedDataMemberList(parens(ddot / ImportedDataMember(properName).sepBy(`,`)))
val importedItem = Choice(
    ImportedType(`'type'` + parens(Identifier(operator))),
    ImportedClass(`'class'` + properName),
    ImportedOperator(symbol),
    ImportedValue(ident),
    ImportedData(properName + !importedDataMembers)
)
val importList = ImportList(
    Choice(
        HIDING + parens(importedItem.sepBy(`,`)),
        parens(importedItem.sepBy(`,`))
    )
)
val importDeclaration = ImportType(
    `'import'` + moduleName +
            Choice(
                importList + ImportAlias(`'as'` + moduleName),
                importList,
                ImportAlias(`'as'` + moduleName),
                True
            )
)

/**
 * nominal = the type can never be coerced to another type.
 * representational = the type can be coerced to another type if certain conditions apply.
 * phantom - the type can always be coerced to another type.
 * */
val role = `'nominal'` / representational / phantom



val decl = Choice(
    (`'data'` + properName + TypeParametersType(!+SameLineOrIndented(typeVar)) + dcolon) + type,
    DataDecl(
        `'data'` + properName + TypeParametersType(!+SameLineOrIndented(typeVar)) + !(eq + DataCtorList(dataCtor.sepBy1(`|`)))
    ),
    (`'newtype'` + properName + dcolon) + type,
    NewtypeDeclType(
        `'newtype'` + properName + TypeParametersType(!+typeVar) + eq + NewtypeCtorType(properName + typeAtom)
    ),
    (`'type'` + `'role'`) + properName + !+role,
    (`'type'` + properName + dcolon) + type,
    TypeDeclType(`'type'` + properName + TypeParametersType(!+typeVar) + eq + type),
    valueDeclarationGroup,
    foreignDeclaration,
    fixityDeclaration,
    classDeclaration,
    InstanceDeclType(`'derive'` + !`'newtype'` + instHead + !(`'where'` + block(instBinder))),
    InstanceDeclType(instHead + !(`'where'` + block(instBinder)))
)
val dataMembers = ExportedDataMemberListType(parens(ddot / ExportedDataMember(properName).sepBy(`,`)))
val exportedItem = Choice(
    ExportedClassType(`'class'` + properName),
    ExportedDataType(properName + !dataMembers),
    ExportedModuleType(`'module'` + moduleName),
    ExportedOperatorType(symbol),
    ExportedTypeOperatorType(`'type'` + symbol),
    ExportedValueType(ident)
)
val exportList = ExportListType(parens(exportedItem.sepBy1(`,`)))
val elseDecl = `'else'`

val module = ModuleType(
    `'module'` + moduleName + !exportList + `'where'` +
            SetOffside( !+importDeclaration +
            !+decl.sepBy1(elseDecl))
)

val binder2 = Choice(
    AppBinderType.fold(CtorBinderType(qualProperName), binderAtom),
    NumberBinderType((`-` + number)),
    binderAtom
)
val binder1 = BinderOperatorExpressionType(binder2.sepBy1(BinderOperatorType(qualOp)))

val caseBranchBody = arrow + exprWhere
val guardedCaseExpr = GuardBranchType(guard + caseBranchBody)
val caseBranch = CaseAlternativeType(
    binder1.sepBy1(`,`) + Choice(
        caseBranchBody,
        +guardedCaseExpr,
    )
)
val ifThenElse =
    IfThenElseType(`'if'` + `expr?` + `'then'` + `expr?` + `'else'` + `expr?`) /
            ErrorIfThenType(`'if'` + `expr?` + `'then'` + `expr?` + (`'else'` + `expr?`).relax("missing else")) /
            ErrorIfType(`'if'` + `expr?` + (`'then'` + `expr?` + `'else'` + `expr?`).relax("missing then"))


fun recordLayout(statement: DSL, name: String): DSL = generalLayout(statement, LCURLY.dsl, `,`, RCURLY.dsl, name)


fun generalLayout(dsl: DSL, left: DSL, sep: DSL, right: DSL, name: String): DSL {
    val stop = sep / right
    val relaxedDsl = dsl.relaxTo(stop, "malformed $name")
    val relaxedSep = sep / (sep.relaxTo(stop, "malformed $name separator") + sep)
    val relaxedRight = right / (right.relaxTo(right, "malformed end of $name") + right)
    return left + right / (relaxedDsl.sepBy(relaxedSep) + relaxedRight)
}

fun recordLayout1(statement: DSL, name: String): DSL {
    val relaxedStatement = statement.relaxTo(`,` / RCURLY, "malformed $name")
    return LCURLY + (statement + !+(`,` + relaxedStatement).heal).heal + RCURLY
}

val letBinding = Choice(
    valueDeclarationGroup,
    LetBinderType(binder1 + eq + exprWhere),
    (ident + !+binderAtom + guardedDecl)
)
val letIn = Let(`'let'` + block(letBinding) + `'in'` + SetOffside(expr))
val letStatement = DoNotationLetType(`'let'` + block(letBinding).relax("missing binding"))
val doStatement = Choice(
    letStatement,
    DoNotationBindType(binder + larrow + `expr?`),
    DoNotationValueType(expr)
)
val doBlock = ChoiceMap(
    qualified(`'do'`),
    SetOffside(DoStatementsType.fold(DoStatementsType(OnOffside(doStatement)), OnOffside(doStatement))) to DoBlock,
    True to EmptyDoBlockType
)
val recordBinder =
    RecordLabelBinderType((label + eq / colon).heal + RecordLabelExprBinderType(binder)) / PunBinderType(label)

