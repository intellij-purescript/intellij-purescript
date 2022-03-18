package org.purescript.parser

import org.purescript.parser.Combinators.attempt
import org.purescript.parser.Combinators.braces
import org.purescript.parser.Combinators.choice
import org.purescript.parser.Combinators.commaSep
import org.purescript.parser.Combinators.commaSep1
import org.purescript.parser.Combinators.guard
import org.purescript.parser.Combinators.many
import org.purescript.parser.Combinators.many1
import org.purescript.parser.Combinators.manyOrEmpty
import org.purescript.parser.Combinators.optional
import org.purescript.parser.Combinators.parens
import org.purescript.parser.Combinators.ref
import org.purescript.parser.Combinators.sepBy
import org.purescript.parser.Combinators.sepBy1
import org.purescript.parser.Combinators.squares
import org.purescript.parser.Combinators.token
import org.purescript.parser.PSTokens.Companion.ADO
import org.purescript.parser.PSTokens.Companion.BANG
import org.purescript.parser.PSTokens.Companion.FLOAT
import org.purescript.parser.PSTokens.Companion.FOREIGN
import org.purescript.parser.PSTokens.Companion.HIDING
import org.purescript.parser.PSTokens.Companion.IMPORT
import org.purescript.parser.PSTokens.Companion.KIND
import org.purescript.parser.PSTokens.Companion.LET
import org.purescript.parser.PSTokens.Companion.MODULE
import org.purescript.parser.PSTokens.Companion.MODULE_PREFIX
import org.purescript.parser.PSTokens.Companion.NATURAL
import org.purescript.parser.PSTokens.Companion.OPERATOR
import org.purescript.parser.PSTokens.Companion.OPTIMISTIC
import org.purescript.parser.PSTokens.Companion.PIPE
import org.purescript.parser.PSTokens.Companion.PROPER_NAME
import org.purescript.parser.PSTokens.Companion.START

class PureParsecParser {

    private val moduleName =
        (optional(token(MODULE_PREFIX)) + token(PROPER_NAME)).`as`(ModuleName)
    private val qualifier = token(MODULE_PREFIX).`as`(ModuleName)
    private fun qualified(p: Parsec) = optional(qualifier) + p

    // tokens

    private val idents =
        choice(
            token(PSTokens.IDENT),
            `as`,
            token(HIDING),
            forall,
            token(PSTokens.QUALIFIED),
            token(KIND),
        )

    private val lname = choice(
        token(PSTokens.IDENT),
        data,
        `'newtype'`,
        `'type'`,
        token(FOREIGN),
        token(IMPORT),
        infixl,
        infixr,
        infix,
        `class`,
        `'derive'`,
        token(KIND),
        `'instance'`,
        token(MODULE),
        case,
        of,
        `if`,
        token(PSTokens.THEN),
        `else`,
        `do`,
        token(ADO),
        token(LET),
        `true`,
        `false`,
        `in`,
        where,
        forall,
        token(PSTokens.QUALIFIED),
        token(HIDING),
        `as`
    ).`as`(Identifier)

    private val label = choice(
        string,
        lname
    )

    // this doesn't match parser.y but i dont feel like changing it right now
    // it might be due to differences in the lexer
    private val operator =
        choice(
            token(OPERATOR),
            dot,
            ddot,
            ldarrow,
            token(OPTIMISTIC),
            token("<="),
            token("-"),
            token("#"),
            token(":"),
        )

    private val properName: Parsec = token(PROPER_NAME).`as`(ProperName)

    // Kinds.hs
    private val parseKind = ref()
    private val parseKindPrefixRef = ref()
    private val parseKindAtom = choice(
        token("*").`as`(START).`as`(Star),
        token("!").`as`(BANG).`as`(Bang),
        attempt(qualified(properName)).`as`(TypeConstructor),
        parens(parseKind)
    )
    private val parseKindPrefix =
        choice(
            (token("#") + parseKindPrefixRef).`as`(RowKind),
            parseKindAtom
        )

    private val type = ref()

    private val parseForAll = ref()

    private val parseTypeVariable: Parsec =
        guard(
            idents,
            { content: String? -> !(content == "∀" || content == "forall") },
            "not `forall`"
        )
            .`as`(GenericIdentifier)

    private val rowLabel =
        lname.or(attempt(string)).`as`(GenericIdentifier) +
            dcolon + type

    private val parseRow: Parsec =
        choice(
            pipe + type,
            commaSep(rowLabel) + optional(pipe + type)
        ).`as`(Row)

    private val typeAtom: Parsec =
        choice(
            attempt(squares(optional(type))),
            attempt(parens(arrow)),
            attempt(braces(parseRow).`as`(ObjectType)),
            attempt(`_`),
            attempt(string),
            attempt(parseForAll),
            attempt(parseTypeVariable),
            attempt(
                attempt(qualified(properName))
                    .`as`(TypeConstructor)
            ),
            attempt(parens(parseRow)),
            attempt(parens(type))
        )
            .`as`(TypeAtom)
    private val parseConstrainedType: Parsec =
        optional(
            attempt(
                parens(
                    commaSep1(
                        attempt(qualified(properName)).`as`(TypeConstructor) +
                            manyOrEmpty(typeAtom)
                    )
                ) + darrow
            )
        ).then(type).`as`(ConstrainedType)

    private val ident =
        idents.`as`(Identifier)
            .or(attempt(parens(operator.`as`(Identifier))))

    private val typeVarBinding =
        choice(
            idents.`as`(TypeVarName),
            parens(idents.`as`(GenericIdentifier) + dcolon + type)
                .`as`(TypeVarKinded)
        )
    private val binderAtom = ref()
    private val binder = ref()
    private val expr = ref()

    // TODO: pattern guards should parse expr1 not expr
    private val patternGuard = optional(attempt(binder + larrow)) + expr
    private val parseGuard = (pipe + commaSep(patternGuard)).`as`(Guard)
    private val dataHead =
        data + properName + manyOrEmpty(typeVarBinding).`as`(TypeArgs)
    private val dataCtor =
        properName.then(manyOrEmpty(typeAtom)).`as`(DataConstructor)
    private val parseTypeDeclaration =
        (ident + dcolon + type).`as`(Signature)
    private val newtypeHead =
        `'newtype'` + properName + manyOrEmpty(typeVarBinding).`as`(TypeArgs)
    private val exprWhere = ref()
    private val guardedDeclExpr = parseGuard + eq + exprWhere
    private val guardedDecl =
        choice(attempt(eq) + exprWhere, many1(guardedDeclExpr))
    private val instBinder =
        choice(
            attempt(ident + dcolon) + type,
            (ident + manyOrEmpty(binderAtom) + guardedDecl)
                .`as`(ValueDeclaration)
        )
    private val parseDeps =
        parens(
            commaSep1(
                attempt(qualified(properName)).`as`(TypeConstructor)
                    .then(manyOrEmpty(typeAtom))
            )
        ).then(darrow)
    private val parseForeignDeclaration =
        `'foreign'` + `'import'` + choice(
            data + properName + dcolon + type `as` ForeignDataDeclaration,
            attempt(ident) + optional(attempt(string) `as` JSRaw) + dcolon + type `as` ForeignValueDeclaration
        )
    private val parseAssociativity = choice(
        infixl,
        infixr,
        infix
    )
    private val parseFixity =
        parseAssociativity.then(token(NATURAL)).`as`(Fixity)
    private val parseFixityDeclaration = parseFixity
        .then(optional(`'type'`))
        .then(
            // TODO Should use qualified proper name instead of module name
            moduleName.or(ident.`as`(ProperName))
        )
        .then(`as`)
        .then(operator.`as`(OperatorName))
        .`as`(FixityDeclaration)

    private val fundep = type.`as`(ClassFunctionalDependency)
    private val fundeps = pipe.then(commaSep1(fundep))
    private val qualProperName =
        (optional(qualifier) + properName).`as`(QualifiedProperName)
    private val constraint =
        (qualProperName.`as`(ClassName) + manyOrEmpty(typeAtom))
            .`as`(ClassConstraint)
    private val constraints = choice(
        parens(commaSep1(constraint)),
        constraint
    )

    private val classSuper =
        (constraints + ldarrow.`as`(pImplies)).`as`(ClassConstraintList)
    private val classNameAndFundeps =
        properName.`as`(ClassName) + manyOrEmpty(typeVarBinding) +
            optional(fundeps.`as`(ClassFunctionalDependencyList))
    private val classSignature = properName.`as`(ClassName) + dcolon + type
    private val classHead = choice(
        // this first is described in haskell code and not in normal happy expression
        // see `fmap (Left . DeclKindSignature () $1) parseClassSignature`
        attempt(`class` + classSignature),
        attempt(`class` + classSuper + classNameAndFundeps),
        `class` + classNameAndFundeps
    )
    private val classMember =
        (idents.`as`(Identifier) + dcolon + type).`as`(ClassMember)

    private val classDeclaration =
        (classHead + optional(
            attempt(where + `L{` + (classMember).sepBy1(`L-sep`) + `L}`)
                .`as`(ClassMemberList)
        )).`as`(ClassDeclaration)
    private val instHead =
        `'instance'` + optional(ident + dcolon) +
            optional(attempt(constraints + darrow)) +
            constraint // this constraint is the instance type
    private val importedDataMembers = parens(
        choice(
            ddot,
            commaSep(properName.`as`(ImportedDataMember))
        )
    ).`as`(ImportedDataMemberList)
    val symbol = parens(operator.`as`(OperatorName)).`as`(Symbol)
    private val importedItem =
        choice(
            `'type'`
                .then(parens(operator.`as`(Identifier)))
                .`as`(ImportedType),
            `class`.then(properName).`as`(ImportedClass),
            token(KIND).then(properName).`as`(ImportedKind),
            symbol.`as`(ImportedOperator),
            ident.`as`(ImportedValue),
            properName
                .then(optional(importedDataMembers))
                .`as`(ImportedData),
        )
    private val importList =
        optional(token(HIDING))
            .then(parens(commaSep(importedItem)))
            .`as`(ImportList)
    private val parseImportDeclaration =
        token(IMPORT)
            .then(moduleName)
            .then(optional(importList))
            .then(optional(`as`.then(moduleName).`as`(ImportAlias)))
            .`as`(ImportDeclaration)

    /**
     * nominal = the type can never be coerced to another type.
     * representational = the type can be coerced to another type if certain conditions apply.
     * phantom - the type can always be coerced to another type.
     * */
    private val role = choice(
        nominal,
        representational,
        phantom,
    )
    private val decl = choice(
        attempt(dataHead + dcolon) + type,
        (dataHead +
            optional((eq + sepBy1(dataCtor, PIPE)).`as`(DataConstructorList))
            ).`as`(DataDeclaration),
        attempt(`'newtype'` + properName + dcolon) + type,
        (newtypeHead + eq + (properName + typeAtom).`as`(NewTypeConstructor))
            .`as`(NewtypeDeclaration),
        attempt(parseTypeDeclaration),
        (attempt(`'type'` + `'role'`) + properName + many(role)),
        (attempt(`'type'` + properName + dcolon) + type),
        (`'type'` + properName + many(typeVarBinding) + eq + type)
            .`as`(TypeSynonymDeclaration),
        (attempt(ident) + many(binderAtom) + guardedDecl).`as`(ValueDeclaration),
        parseForeignDeclaration,
        parseFixityDeclaration,
        classDeclaration,
        optional(`'derive'`.then(optional(`'newtype'`)))
            .then(instHead)
            .then(optional(where + `L{` + instBinder.sepBy1(`L-sep`) + `L}`))
            .`as`(InstanceDeclaration)
    )
    private val exportedClass =
        `class`.then(properName).`as`(ExportedClass)
    private val dataMembers =
        parens(ddot.or(commaSep(properName.`as`(ExportedDataMember))))
            .`as`(ExportedDataMemberList)
    private val exportedData =
        (properName + optional(dataMembers)).`as`(ExportedData)
    private val exportedKind =
        token(KIND).then(properName).`as`(ExportedKind)
    private val exportedModule =
        token(MODULE).then(moduleName).`as`(ExportedModule)
    private val exportedOperator =
        symbol.`as`(ExportedOperator)
    private val exportedType =
        `'type'`.then(parens(operator.`as`(Identifier)))
            .`as`(ExportedType)
    private val exportedValue = ident.`as`(ExportedValue)
    private val exportList =
        parens(
            commaSep1(
                choice(
                    exportedClass,
                    exportedData,
                    exportedKind,
                    exportedModule,
                    exportedOperator,
                    exportedType,
                    exportedValue,
                )
            )
        ).`as`(ExportList)

    private val elseDecl = token("else") + optional(`L-sep`)
    private val moduleDecl =
        choice(
            parseImportDeclaration,
            sepBy(decl, elseDecl)
        )

    val parseModule = token(MODULE)
        .then(moduleName)
        .then(optional(exportList))
        .then(where)
        .then(`L{` + moduleDecl.sepBy(`L-sep`) + `L}`)
        .`as`(Module)

    // Literals
    private val boolean = `true`.or(`false`)
    private val number = token(NATURAL).or(token(FLOAT)).`as`(NumericLiteral)

    private val hole = (token("?") + idents).`as`(TypeHole)
    private val recordLabel =
        choice(
            attempt(label + token(":")) + expr,
            attempt(label + eq) + expr,
            label
                .`as`(QualifiedIdentifier)
                .`as`(ExpressionIdentifier),
        ).`as`(ObjectBinderField)

    private val qualOp =
        qualified(operator.`as`(OperatorName)).`as`(QualifiedOperatorName)

    private val binder2 = choice(
        attempt(
            qualProperName
                .`as`(ConstructorBinder)
                .then(manyOrEmpty(binderAtom))
        ),
        attempt(token("-") + number).`as`(NumberBinder),
        binderAtom,
    )
    private val binder1 = binder2.sepBy1(token(OPERATOR))

    private val guardedCaseExpr = parseGuard + (arrow + exprWhere)

    private val guardedCase =
        choice(
            attempt(arrow + exprWhere),
            manyOrEmpty(guardedCaseExpr)
        )
    private val caseBranch =
        (commaSep1(binder1) + guardedCase).`as`(CaseAlternative)

    private val parseIfThenElse = `if`
        .then(expr)
        .then(token(PSTokens.THEN))
        .then(expr)
        .then(`else`)
        .then(expr)
        .`as`(IfThenElse)
    private val letBinding =
        choice(
            attempt(parseTypeDeclaration),
            attempt(
                (ident + many(binderAtom) + guardedDecl)
                    .`as`(ValueDeclaration)
            ),
            attempt(binder1 + eq + exprWhere),
            attempt(ident + many(binderAtom) + guardedDecl)
        )
    private val parseLet = token(LET)
        .then(`L{` + (letBinding).sepBy1(`L-sep`) + `L}`)
        .then(`in`)
        .then(expr)
        .`as`(Let)
    private val doStatement =
        choice(
            token(LET).then(`L{` + (letBinding).sepBy1(`L-sep`) + `L}`)
                .`as`(DoNotationLet),
            attempt(binder + larrow + expr).`as`(DoNotationBind),
            attempt(expr.`as`(DoNotationValue))
        )
    private val doBlock =
        (attempt(qualified(`do`)) + `L{` + (doStatement).sepBy1(
            `L-sep`
        ) + `L}`).`as`(DoBlock)

    private val adoBlock =
        token(ADO) + `L{` + (doStatement).sepBy(`L-sep`) + `L}`

    init {
        parseKindPrefixRef.setRef(parseKindPrefix)
        parseKind.setRef(
            (parseKindPrefix +
                optional(
                    arrow
                        .or(
                            optional(
                                attempt(qualified(properName)).`as`(
                                    TypeConstructor
                                )
                            )
                        ) +
                        optional(parseKind)
                )).`as`(FunKind)
        )

        val type5 = many1(typeAtom)
        val type4 = manyOrEmpty(token("#")) + type5
        val type3 = type4.sepBy1(qualOp)
        val type2 = ref()
        val type1 = manyOrEmpty(forall + many1(typeVarBinding) + dot) + type2
        type2.setRef(type3 + optional(arrow.or(darrow) + type1))
        type.setRef((type1.sepBy1(dcolon)).`as`(Type))
        parseForAll.setRef(
            forall
                .then(many1(idents.`as`(GenericIdentifier)))
                .then(dot)
                .then(parseConstrainedType).`as`(ForAll)
        )
        val parsePropertyUpdate =
            label + optional(eq) + expr
        val exprAtom = choice(
            `_`,
            attempt(hole),
            attempt(
                qualified(idents.`as`(Identifier))
                    .`as`(QualifiedIdentifier)
                    .`as`(ExpressionIdentifier)
            ),
            attempt(
                qualified(symbol)
                    .`as`(QualifiedSymbol)
                    .`as`(ExpressionSymbol)
            ),
            attempt(
                qualified(properName)
                    .`as`(QualifiedProperName)
                    .`as`(ExpressionConstructor)
            ),
            boolean.`as`(BooleanLiteral),
            char.`as`(CharLiteral),
            string.`as`(StringLiteral),
            number,
            squares(commaSep(expr)).`as`(ArrayLiteral),
            braces(commaSep(recordLabel)).`as`(ObjectLiteral),
            parens(expr).`as`(Parens),
        )
        val expr7 = exprAtom + manyOrEmpty((dot + label).`as`(Accessor))
        val expr5 = choice(
            attempt(braces(commaSep1(parsePropertyUpdate))),
            expr7,
            (backslash + many1(binderAtom) + arrow + expr).`as`(Abs),
            /*
            * if there is only one case branch it can ignore layout so we need
            * to allow layout end at any time.
            */
            (case + commaSep1(expr) + of + choice(
                attempt(`L{` + caseBranch.sepBy1(`L-sep`) + `L}`),
                attempt(`L{` + commaSep(binder1) + arrow + `L}` + exprWhere),
                `L{` + commaSep(binder1) + `L}` + guardedCase,
            )).`as`(Case),
            parseIfThenElse,
            doBlock,
            adoBlock + `in` + expr,
            parseLet
        )
        val expr4 = many1(expr5)
        val expr3 =
            choice(
                (many1(token("-")) + expr4).`as`(UnaryMinus),
                expr4
            )
        val exprBacktick2 = expr3.sepBy1(qualOp)
        val expr2 = expr3.sepBy1(tick + exprBacktick2 + tick)
        val expr1 = expr2.sepBy1(attempt(qualOp).`as`(ExpressionOperator))


        expr.setRef((expr1 + optional(dcolon + type)).`as`(Value))
        val recordBinder =
            choice(
                attempt(label + eq.or(token(":"))) + binder,
                label.`as`(VarBinder)
            )
        binder.setRef(binder1 + optional(dcolon + type))
        binderAtom.setRef(
            choice(
                attempt(`_`.`as`(NullBinder)),
                attempt(ident.`as`(VarBinder) + `@` + binderAtom)
                    .`as`(NamedBinder),
                attempt(ident.`as`(VarBinder)),
                attempt(qualProperName.`as`(ConstructorBinder)),
                attempt(boolean.`as`(BooleanBinder)),
                attempt(char).`as`(CharBinder),
                attempt(string.`as`(StringBinder)),
                attempt(number.`as`(NumberBinder)),
                attempt(squares(commaSep(binder)).`as`(ObjectBinder)),
                attempt(braces(commaSep(recordBinder))),
                attempt(parens(binder))
            )
        )
        exprWhere.setRef(
            expr + optional(
                (where + `L{` + letBinding.sepBy1(`L-sep`) + `L}`)
                    .`as`(ExpressionWhere)
            )
        )
    }
}
