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
import org.purescript.parser.PSElements.Companion.Abs
import org.purescript.parser.PSElements.Companion.Accessor
import org.purescript.parser.PSElements.Companion.ArrayLiteral
import org.purescript.parser.PSElements.Companion.Bang
import org.purescript.parser.PSElements.Companion.Binder
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
import org.purescript.parser.PSElements.Companion.ConstrainedType
import org.purescript.parser.PSElements.Companion.ConstructorBinder
import org.purescript.parser.PSElements.Companion.DataConstructor
import org.purescript.parser.PSElements.Companion.DataConstructorList
import org.purescript.parser.PSElements.Companion.DoBlock
import org.purescript.parser.PSElements.Companion.DoNotationBind
import org.purescript.parser.PSElements.Companion.DoNotationLet
import org.purescript.parser.PSElements.Companion.DoNotationValue
import org.purescript.parser.PSElements.Companion.ExportedDataMember
import org.purescript.parser.PSElements.Companion.ExportedDataMemberList
import org.purescript.parser.PSElements.Companion.ExpressionConstructor
import org.purescript.parser.PSElements.Companion.ExpressionIdentifier
import org.purescript.parser.PSElements.Companion.ExpressionWhere
import org.purescript.parser.PSElements.Companion.ExternDataDeclaration
import org.purescript.parser.PSElements.Companion.GenericIdentifier
import org.purescript.parser.PSElements.Companion.Guard
import org.purescript.parser.PSElements.Companion.Identifier
import org.purescript.parser.PSElements.Companion.ModuleName
import org.purescript.parser.PSElements.Companion.NamedBinder
import org.purescript.parser.PSElements.Companion.NewTypeConstructor
import org.purescript.parser.PSElements.Companion.NumberBinder
import org.purescript.parser.PSElements.Companion.NumericLiteral
import org.purescript.parser.PSElements.Companion.ObjectBinder
import org.purescript.parser.PSElements.Companion.ObjectBinderField
import org.purescript.parser.PSElements.Companion.ObjectLiteral
import org.purescript.parser.PSElements.Companion.ProperName
import org.purescript.parser.PSElements.Companion.Qualified
import org.purescript.parser.PSElements.Companion.QualifiedIdentifier
import org.purescript.parser.PSElements.Companion.QualifiedProperName
import org.purescript.parser.PSElements.Companion.Row
import org.purescript.parser.PSElements.Companion.RowKind
import org.purescript.parser.PSElements.Companion.Star
import org.purescript.parser.PSElements.Companion.StringBinder
import org.purescript.parser.PSElements.Companion.StringLiteral
import org.purescript.parser.PSElements.Companion.TypeArgs
import org.purescript.parser.PSElements.Companion.TypeConstructor
import org.purescript.parser.PSElements.Companion.TypeHole
import org.purescript.parser.PSElements.Companion.TypeInstanceDeclaration
import org.purescript.parser.PSElements.Companion.TypeSynonymDeclaration
import org.purescript.parser.PSElements.Companion.TypeVarKinded
import org.purescript.parser.PSElements.Companion.TypeVarName
import org.purescript.parser.PSElements.Companion.UnaryMinus
import org.purescript.parser.PSElements.Companion.Value
import org.purescript.parser.PSElements.Companion.ValueDeclaration
import org.purescript.parser.PSElements.Companion.VarBinder
import org.purescript.parser.PSElements.Companion.pClassName
import org.purescript.parser.PSElements.Companion.pImplies
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

    private val operator =
        choice(
            token(OPERATOR),
            dot,
            ddot,
            larrow,
            ldarrow,
            token(PSTokens.OPTIMISTIC)
        )
    private val properName: Parsec = token(PROPER_NAME).`as`(ProperName)

    // Kinds.hs
    private val parseKind = ref()
    private val parseKindPrefixRef = ref()
    private val parseKindAtom = choice(
        token("*").`as`(START).`as`(Star),
        token("!").`as`(BANG).`as`(Bang),
        attempt(qualified(properName)).`as`(Qualified).`as`(TypeConstructor),
        parens(parseKind)
    )
    private val parseKindPrefix =
        choice(
            (token("#") + parseKindPrefixRef).`as`(RowKind),
            parseKindAtom
        )

    // Types.hs
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
            attempt(braces(parseRow).`as`(PSElements.ObjectType)),
            attempt(`_`),
            attempt(string),
            attempt(parseForAll),
            attempt(parseTypeVariable),
            attempt(
                attempt(qualified(properName))
                    .`as`(Qualified)
                    .`as`(TypeConstructor)
            ),
            attempt(parens(parseRow)),
            attempt(parens(type))
        )
            .`as`(PSElements.TypeAtom)
    private val parseConstrainedType: Parsec =
        optional(
            attempt(
                parens(
                    commaSep1(
                        attempt(qualified(properName))
                            .`as`(Qualified)
                            .`as`(TypeConstructor) +
                            manyOrEmpty(typeAtom)
                    )
                ) + darrow
            )
        ).then(type).`as`(ConstrainedType)

    private val ident =
        idents.`as`(Identifier)
            .or(attempt(parens(operator.`as`(Identifier))))

    // Declarations.hs
    private val typeVarBinding =
        choice(
            idents.`as`(TypeVarName),
            parens(
                idents.`as`(GenericIdentifier)
                    .then(dcolon)
                    .then(parseKind)
            ).`as`(TypeVarKinded)
        )
    private val binderAtom = ref()
    private val binder = ref()
    private val expr = ref()
    private val parseGuard = (pipe + commaSep(expr)).`as`(Guard)
    private val dataHead =
        data + properName + manyOrEmpty(typeVarBinding).`as`(TypeArgs)
    private val dataCtor =
        properName.then(manyOrEmpty(typeAtom)).`as`(DataConstructor)
    private val parseTypeDeclaration =
        (ident.`as`(PSElements.TypeAnnotationName) + dcolon + type)
            .`as`(PSElements.TypeDeclaration)
    private val newtypeHead =
        `'newtype'` + properName + manyOrEmpty(typeVarBinding).`as`(TypeArgs)
    private val exprWhere = ref()
    private val parsePatternMatchObject =
        braces(
            commaSep(
                idents.or(lname).or(attempt(string))
                    .then(optional(eq.or(token(OPERATOR))))
                    .then(optional(binder))
            )
        )
            .`as`(Binder)
    private val parseRowPatternBinder = token(OPERATOR).then(binder)
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
                attempt(qualified(properName))
                    .`as`(Qualified)
                    .`as`(TypeConstructor)
                    .then(manyOrEmpty(typeAtom))
            )
        ).then(darrow)
    private val parseExternDeclaration =
        token(FOREIGN)
            .then(token(IMPORT))
            .then(
                choice(
                    data
                        .then(token(PROPER_NAME).`as`(TypeConstructor))
                        .then(dcolon).then(parseKind)
                        .`as`(ExternDataDeclaration),
                    `'instance'`
                        .then(ident).then(dcolon)
                        .then(optional(parseDeps))
                        .then(
                            attempt(qualified(properName))
                                .`as`(Qualified)
                                .`as`(pClassName)
                        )
                        .then(manyOrEmpty(typeAtom))
                        .`as`(PSElements.ExternInstanceDeclaration),
                    attempt(ident)
                        .then(optional(attempt(string).`as`(PSElements.JSRaw)))
                        .then(dcolon)
                        .then(type)
                        .`as`(PSElements.ForeignValueDeclaration)
                )
            )
    private val parseAssociativity = choice(
        infixl,
        infixr,
        infix
    )
    private val parseFixity =
        parseAssociativity.then(token(NATURAL)).`as`(PSElements.Fixity)
    private val parseFixityDeclaration = parseFixity
        .then(optional(`'type'`))
        .then(
            // TODO Should use qualified proper name instead of module name
            moduleName.or(ident.`as`(ProperName))
        )
        .then(`as`)
        .then(operator)
        .`as`(PSElements.FixityDeclaration)

    private val fundep = type.`as`(ClassFunctionalDependency)
    private val fundeps = pipe.then(commaSep1(fundep))
    private val qualProperName =
        (optional(qualifier) + properName).`as`(QualifiedProperName)
    private val constraint =
        (attempt(qualProperName.`as`(pClassName)) +
            manyOrEmpty(typeAtom)).`as`(ClassConstraint)
    private val constraints = choice(
        parens(commaSep1(constraint)),
        constraint
    )

    private val classSuper =
        (constraints + ldarrow.`as`(pImplies)).`as`(ClassConstraintList)
    private val classNameAndFundeps =
        properName.`as`(pClassName) + manyOrEmpty(typeVarBinding) +
            optional(fundeps.`as`(ClassFunctionalDependencyList))
    private val classSignature = properName.`as`(pClassName) + dcolon + type
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
        `'instance'` + ident + dcolon +
            optional(attempt(
                optional(lparen)
                    + commaSep1(constraint)
                    + optional(rparen) + darrow
            )) +
            qualProperName +
            manyOrEmpty(typeAtom)
    private val importedDataMembers = parens(
        choice(
            ddot,
            commaSep(properName.`as`(PSElements.ImportedDataMember))
        )
    ).`as`(PSElements.ImportedDataMemberList)
    private val importedItem =
        choice(
            `'type'`
                .then(parens(operator.`as`(Identifier)))
                .`as`(PSElements.ImportedType),
            `class`.then(properName).`as`(PSElements.ImportedClass),
            token(KIND).then(properName).`as`(PSElements.ImportedKind),
            parens(operator.`as`(Identifier)).`as`(PSElements.ImportedOperator),
            ident.`as`(PSElements.ImportedValue),
            properName
                .then(optional(importedDataMembers))
                .`as`(PSElements.ImportedData),
        )
    private val importList =
        optional(token(HIDING))
            .then(parens(commaSep(importedItem)))
            .`as`(PSElements.ImportList)
    private val parseImportDeclaration =
        token(IMPORT)
            .then(moduleName)
            .then(optional(importList))
            .then(optional(`as`.then(moduleName).`as`(PSElements.ImportAlias)))
            .`as`(PSElements.ImportDeclaration)

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
            ).`as`(PSElements.DataDeclaration),
        attempt(`'newtype'` + properName + dcolon) + type,
        (newtypeHead + eq + (properName + typeAtom).`as`(NewTypeConstructor))
            .`as`(PSElements.NewtypeDeclaration),
        attempt(parseTypeDeclaration),
        (attempt(`'type'` + `'role'`) + properName + many(role)),
        (attempt(`'type'` + properName + dcolon) + type),
        (`'type'` + properName + many(typeVarBinding) + eq + type)
            .`as`(TypeSynonymDeclaration),
        (attempt(ident) + many(binderAtom) + guardedDecl).`as`(ValueDeclaration),
        parseExternDeclaration,
        parseFixityDeclaration,
        classDeclaration,
        optional(`'derive'`.then(optional(`'newtype'`)))
            .then(instHead)
            .then(optional(where + `L{` + instBinder.sepBy1(`L-sep`) + `L}`))
            .`as`(TypeInstanceDeclaration)
    )
    private val exportedClass =
        `class`.then(properName).`as`(PSElements.ExportedClass)
    private val exportedData =
        properName
            .then(
                optional(
                    parens(
                        choice(
                            ddot,
                            commaSep1(properName.`as`(ExportedDataMember))
                        )
                    ).`as`(ExportedDataMemberList)
                )
            )
            .`as`(PSElements.ExportedData)
    private val exportedKind =
        token(KIND).then(properName).`as`(PSElements.ExportedKind)
    private val exportedModule =
        token(MODULE).then(moduleName).`as`(PSElements.ExportedModule)
    private val exportedOperator =
        parens(operator.`as`(Identifier)).`as`(PSElements.ExportedOperator)
    private val exportedType =
        `'type'`.then(parens(operator.`as`(Identifier)))
            .`as`(PSElements.ExportedType)
    private val exportedValue = ident.`as`(PSElements.ExportedValue)
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
        ).`as`(PSElements.ExportList)

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
        .`as`(PSElements.Module)

    // Literals
    private val boolean = `true`.or(`false`)
    private val number = token(NATURAL).or(token(FLOAT)).`as`(NumericLiteral)

    private val hole = token("?").`as`(TypeHole)
    private val recordLabel =
        choice(
            attempt(label + token(":")) + expr,
            attempt(label + eq) + expr,
            label
                .`as`(QualifiedIdentifier)
                .`as`(ExpressionIdentifier),
        ).`as`(ObjectBinderField)

    private val qualOp = choice(
        operator,
        attempt(qualified(operator)).`as`(Qualified),
        token("<="),
        token("-"),
        token("#"),
        token(":"),
    )

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
        .`as`(PSElements.IfThenElse)
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
        .`as`(PSElements.Let)
    private val doStatement =
        choice(
            token(LET).then(`L{` + (letBinding).sepBy1(`L-sep`) + `L}`)
                .`as`(DoNotationLet),
            attempt(binder + larrow + expr).`as`(DoNotationBind),
            attempt(expr.`as`(DoNotationValue))
        )
    private val doBlock =
        (attempt(qualified(`do`)).`as`(Qualified) + `L{` + (doStatement).sepBy1(
            `L-sep`
        ) + `L}`).`as`(DoBlock)

    private val adoBlock =
        token(ADO) + `L{` + (doStatement).sepBy(`L-sep`) + `L}`

    private val type0 = ref()
    private val type1 = ref()
    private val type2 = ref()
    private val type3 = ref()
    private val type4 = ref()
    private val type5 = ref()

    init {
        type0.setRef(type1 + optional(dcolon + type0))
        type1.setRef(type2.or(forall + many1(typeVarBinding) + dot + type1))
        type2.setRef(type3 + optional(arrow.or(darrow) + type1))
        type3.setRef(type4 + optional(qualOp + type4))
        type4.setRef(type5.or(token("#") + type4))
        type5.setRef(many1(typeAtom))
        parseKindPrefixRef.setRef(parseKindPrefix)
        parseKind.setRef(
            (parseKindPrefix +
                optional(
                    arrow
                        .or(
                            optional(
                                attempt(qualified(properName))
                                    .`as`(Qualified)
                                    .`as`(TypeConstructor)
                            )
                        ) +
                        optional(parseKind)
                )).`as`(PSElements.FunKind)
        )
        type.setRef(
            many1(typeAtom.or(string) + optional(dcolon + parseKind))
                .then(
                    optional(
                        choice(
                            arrow,
                            darrow,
                            token(PSTokens.OPTIMISTIC),
                            token(OPERATOR)
                        )
                            .then(type)
                    )
                ).`as`(PSElements.Type)
        )
        parseForAll.setRef(
            forall
                .then(many1(idents.`as`(GenericIdentifier)))
                .then(dot)
                .then(parseConstrainedType).`as`(PSElements.ForAll)
        )
        val parsePropertyUpdate =
            label + optional(eq) + expr
        val exprAtom = choice(
            `_`,
            attempt(hole),
            attempt(
                qualified(ident)
                    .`as`(QualifiedIdentifier)
                    .`as`(ExpressionIdentifier)
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
            parens(expr).`as`(PSElements.Parens),
        )
        val exprBacktick = properName.`as`(ProperName)
            .or(many1(idents.`as`(ProperName)))
        val expr7 = exprAtom + manyOrEmpty((dot + label).`as`(Accessor))
        val expr5 = choice(
            attempt(braces(commaSep1(parsePropertyUpdate))),
            expr7,
            attempt(tick + exprBacktick + tick),
            (backslash + many1(binderAtom) + arrow + expr).`as`(Abs),
            (case + commaSep1(expr) + of + `L{` +
                caseBranch.sepBy1(`L-sep`) + `L}`).`as`(Case),
            parseIfThenElse,
            doBlock,
            adoBlock + `in` + expr,
            parseLet
        )
        val expr4 = expr5 + manyOrEmpty(expr5)
        val expr3 =
            choice(
                (many1(token("-")) + expr4).`as`(UnaryMinus),
                expr4
            )

        val expr2 = expr3.sepBy1(
            tick + attempt(qualified(idents)).`as`(
                Qualified
            ) + tick
        )
        val expr1 = expr2.sepBy1(attempt(qualified(operator)).`as`(Qualified))


        expr.setRef((expr1 + optional(dcolon + type)).`as`(Value))
        val recordBinder =
            choice(
                attempt(label + eq.or(token(":"))) + binder,
                label.`as`(VarBinder)
            )
        binder.setRef(binder1 + optional(dcolon + type))
        binderAtom.setRef(
            choice(
                attempt(`_`.`as`(PSElements.NullBinder)),
                attempt(ident + `@` + binderAtom).`as`(NamedBinder),
                attempt(ident.`as`(VarBinder)),
                attempt(qualProperName.`as`(ConstructorBinder)),
                attempt(boolean.`as`(BooleanBinder)),
                attempt(char).`as`(CharBinder),
                attempt(string.`as`(StringBinder)),
                attempt(number.`as`(NumberBinder)),
                attempt(squares(commaSep(expr)).`as`(ObjectBinder)),
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
