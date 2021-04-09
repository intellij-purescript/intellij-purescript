package org.purescript.parser

import org.purescript.parser.Combinators.attempt
import org.purescript.parser.Combinators.braces
import org.purescript.parser.Combinators.choice
import org.purescript.parser.Combinators.commaSep
import org.purescript.parser.Combinators.commaSep1
import org.purescript.parser.Combinators.guard
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
import org.purescript.parser.PSElements.Companion.DoNotationBind
import org.purescript.parser.PSElements.Companion.DoNotationLet
import org.purescript.parser.PSElements.Companion.DoNotationValue
import org.purescript.parser.PSElements.Companion.ExportedDataMember
import org.purescript.parser.PSElements.Companion.ExportedDataMemberList
import org.purescript.parser.PSElements.Companion.ExpressionConstructor
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
import org.purescript.parser.PSTokens.Companion.DERIVE
import org.purescript.parser.PSTokens.Companion.FLOAT
import org.purescript.parser.PSTokens.Companion.FOREIGN
import org.purescript.parser.PSTokens.Companion.HIDING
import org.purescript.parser.PSTokens.Companion.IMPORT
import org.purescript.parser.PSTokens.Companion.INSTANCE
import org.purescript.parser.PSTokens.Companion.KIND
import org.purescript.parser.PSTokens.Companion.LET
import org.purescript.parser.PSTokens.Companion.MODULE
import org.purescript.parser.PSTokens.Companion.NATURAL
import org.purescript.parser.PSTokens.Companion.NEWTYPE
import org.purescript.parser.PSTokens.Companion.OPERATOR
import org.purescript.parser.PSTokens.Companion.PIPE
import org.purescript.parser.PSTokens.Companion.PROPER_NAME
import org.purescript.parser.PSTokens.Companion.START
import org.purescript.parser.PSTokens.Companion.TYPE

class PureParsecParser {

    private val moduleName = sepBy1(token(PROPER_NAME), dot).`as`(ModuleName)
    private val qualifier =
        many1(attempt(token(PROPER_NAME) + dot)).`as`(ModuleName)

    private fun qualified(p: Parsec) = optional(qualifier) + p

    private fun parseQualified(p: Parsec): Parsec =
        attempt(
            manyOrEmpty(
                attempt(token(PROPER_NAME).`as`(ProperName) + dot)
            ) + p
        ).`as`(Qualified)

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
        token(NEWTYPE),
        token(TYPE),
        token(FOREIGN),
        token(IMPORT),
        infixl,
        infixr,
        infix,
        `class`,
        token(DERIVE),
        token(KIND),
        token(INSTANCE),
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
        parseQualified(properName).`as`(TypeConstructor),
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
            attempt(parseForAll),
            attempt(parseTypeVariable),
            attempt(parseQualified(properName).`as`(TypeConstructor)),
            attempt(parens(parseRow)),
            attempt(parens(type))
        )
            .`as`(PSElements.TypeAtom)
    private val parseConstrainedType: Parsec =
        optional(
            attempt(
                parens(
                    commaSep1(
                        parseQualified(properName).`as`(TypeConstructor) +
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
    private val parseLocalDeclaration = ref()
    private val parseGuard = (pipe + commaSep(expr)).`as`(Guard)
    private val dataHead =
        data + properName + manyOrEmpty(typeVarBinding).`as`(TypeArgs)
    private val dataCtor =
        properName.then(manyOrEmpty(typeAtom)).`as`(DataConstructor)
    private val parseTypeDeclaration =
        (ident.`as`(PSElements.TypeAnnotationName) + dcolon + type)
            .`as`(PSElements.TypeDeclaration)
    private val newtypeHead =
        token(NEWTYPE) + properName + manyOrEmpty(typeVarBinding).`as`(TypeArgs)
    private val typeHead =
        token(TYPE) + properName + manyOrEmpty(typeVarBinding)
    private val exprWhere =
        expr + optional(where + `L{` + parseLocalDeclaration.sepBy1(`L-sep`) + `L}`)

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


    private val parseValueDeclaration =
        attempt(many1(ident))
            .then(attempt(manyOrEmpty(binderAtom)))
            .then(guardedDecl).`as`(ValueDeclaration)
    private val parseDeps =
        parens(
            commaSep1(
                parseQualified(properName).`as`(TypeConstructor)
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
                    token(INSTANCE)
                        .then(ident).then(dcolon)
                        .then(optional(parseDeps))
                        .then(parseQualified(properName).`as`(pClassName))
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
        .then(optional(token(TYPE)))
        .then(
            // TODO Should use qualified proper name instead of module name
            moduleName.or(ident.`as`(ProperName))
        )
        .then(`as`)
        .then(operator)
        .`as`(PSElements.FixityDeclaration)

    private val fundep = type.`as`(ClassFunctionalDependency)
    private val fundeps = pipe.then(commaSep1(fundep))
    private val constraint =
        parseQualified(properName).`as`(pClassName).then(manyOrEmpty(typeAtom))
            .`as`(ClassConstraint)
    private val constraints = choice(
        parens(commaSep1(constraint)),
        constraint
    )

    private val classSuper =
        optional(
            attempt(constraints + ldarrow.`as`(pImplies))
                .`as`(ClassConstraintList)
        )

    private val classHead = `class`
        .then(classSuper)
        .then(optional(properName.`as`(pClassName)))
        .then(optional(manyOrEmpty(typeVarBinding)))
        .then(optional(fundeps.`as`(ClassFunctionalDependencyList)))

    private val classMember =
        (idents.`as`(Identifier) + dcolon + type).`as`(ClassMember)

    private val classDeclaration =
        classHead
            .then(
                optional(
                    attempt(
                        where
                            .then(`L{` + (classMember).sepBy1(`L-sep`) + `L}`)
                            .`as`(ClassMemberList)
                    )
                )
            ).`as`(ClassDeclaration)
    private val parseTypeInstanceDeclaration =
        optional(token(DERIVE))
            .then(optional(token(NEWTYPE)))
            .then(
                token(INSTANCE)
                    .then(ident.`as`(GenericIdentifier).then(dcolon))
                    .then(
                        optional(
                            optional(lparen)
                                .then(
                                    commaSep1(
                                        parseQualified(properName)
                                            .`as`(TypeConstructor)
                                            .then(manyOrEmpty(typeAtom))
                                    )
                                )
                                .then(optional(rparen))
                                .then(optional(darrow))
                        )
                    )
                    .then(
                        optional(
                            parseQualified(properName).`as`(
                                pClassName
                            )
                        )
                    )
                    .then(manyOrEmpty(typeAtom.or(string)))
                    .then(
                        optional(
                            darrow
                                .then(optional(lparen))
                                .then(
                                    parseQualified(properName).`as`(
                                        TypeConstructor
                                    )
                                )
                                .then(manyOrEmpty(typeAtom))
                                .then(optional(rparen))
                        )
                    )
                    .then(
                        optional(
                            attempt(
                                where
                                    .then(
                                        `L{` +
                                            parseValueDeclaration.sepBy1(`L-sep`) +
                                            `L}`
                                    )
                            )
                        )
                    )
            ).`as`(TypeInstanceDeclaration)
    private val importedDataMembers = parens(
        choice(
            ddot,
            commaSep(properName.`as`(PSElements.ImportedDataMember))
        )
    ).`as`(PSElements.ImportedDataMemberList)
    private val importedItem =
        choice(
            token(TYPE)
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
            .then(parens(commaSep1(importedItem)))
            .`as`(PSElements.ImportList)
    private val parseImportDeclaration =
        token(IMPORT)
            .then(moduleName)
            .then(optional(importList))
            .then(optional(`as`.then(moduleName).`as`(PSElements.ImportAlias)))
            .`as`(PSElements.ImportDeclaration)
    private val decl = choice(
        (dataHead +
            optional((eq + sepBy1(dataCtor, PIPE)).`as`(DataConstructorList))
            ).`as`(PSElements.DataDeclaration),
        (newtypeHead + eq + (properName + typeAtom).`as`(NewTypeConstructor))
            .`as`(PSElements.NewtypeDeclaration),
        attempt(parseTypeDeclaration),
        (typeHead + eq + type).`as`(TypeSynonymDeclaration),
        attempt(ident)
            .then(manyOrEmpty(binderAtom))
            .then(guardedDecl).`as`(ValueDeclaration),
        parseExternDeclaration,
        parseFixityDeclaration,
        classDeclaration,
        parseTypeInstanceDeclaration
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
        token(TYPE).then(parens(operator.`as`(Identifier)))
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
            attempt(label + token("=")) + expr,
            label,
        ).`as`(ObjectBinderField)

    private val qualOp = choice(
        operator,
        parseQualified(operator),
        token("<="),
        token("-"),
        token("#"),
        token(":"),
    )


    private val qualPropName = parseQualified(properName)
    private val binder2 = choice(
        attempt(
            qualPropName.`as`(ConstructorBinder).then(manyOrEmpty(binderAtom))
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
    private val parseLet = token(LET)
        .then(`L{` + (parseLocalDeclaration).sepBy1(`L-sep`) + `L}`)
        .then(`in`)
        .then(expr)
        .`as`(PSElements.Let)
    private val letBinding =
        choice(
            attempt(parseTypeDeclaration),
            optional(attempt(lparen))
                .then(optional(attempt(properName).`as`(ExpressionConstructor)))
                .then(optional(attempt(many1(ident))))
                .then(
                    optional(
                        attempt(squares(commaSep(binder)).`as`(ObjectBinder))
                    )
                )
                .then(
                    optional(attempt(`@`.then(braces(commaSep(idents)))))
                        .`as`(NamedBinder)
                )
                .then(optional(attempt(parsePatternMatchObject)))
                .then(optional(attempt(parseRowPatternBinder)))
                .then(optional(attempt(rparen)))
                .then(attempt(manyOrEmpty(binderAtom)))
                .then(
                    choice(
                        attempt(many1(parseGuard + (eq + exprWhere))),
                        attempt(eq + (exprWhere))
                    )
                ).`as`(ValueDeclaration)
        )
    private val doStatement =
        choice(
            token(LET).then(`L{` + (letBinding).sepBy1(`L-sep`) + `L}`)
                .`as`(DoNotationLet),
            attempt(binder + larrow + expr).`as`(DoNotationBind),
            attempt(expr.`as`(DoNotationValue))
        )
    private val doBlock =
        parseQualified(`do`) + `L{` + (doStatement).sepBy1(`L-sep`) + `L}`

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
                                parseQualified(properName).`as`(TypeConstructor)
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
        parseLocalDeclaration.setRef(
            choice(
                attempt(parseTypeDeclaration),
                // this is for when used with LET
                optional(attempt(lparen))
                    .then(
                        optional(
                            attempt(properName).`as`(ExpressionConstructor)
                        )
                    )
                    .then(optional(attempt(many1(ident))))
                    .then(
                        optional(
                            attempt(
                                squares(commaSep(binder)).`as`(ObjectBinder)
                            )
                        )
                    )
                    .then(
                        optional(attempt(`@`.then(braces(commaSep(idents)))))
                            .`as`(NamedBinder)
                    )
                    .then(optional(attempt(parsePatternMatchObject)))
                    .then(optional(attempt(parseRowPatternBinder)))
                    .then(optional(attempt(rparen)))
                    // ---------- end of LET stuff -----------
                    .then(attempt(manyOrEmpty(binderAtom)))
                    .then(guardedDecl).`as`(ValueDeclaration)
            )
        )
        val parsePropertyUpdate =
            label + optional(eq) + expr
        val exprAtom = choice(
            attempt(hole),
            attempt(parseQualified(ident)).`as`(PSElements.Var),
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

        val expr2 = expr3.sepBy1(tick + parseQualified(idents) + tick)
        val expr1 = expr2.sepBy1(parseQualified(operator))


        expr.setRef((expr1 + optional(dcolon + type)).`as`(Value))
        val recordBinder =
            idents +
                optional(token("=").or(token(":") + binder))
        binder.setRef(binder1 + optional(dcolon + type))
        binderAtom.setRef(
            choice(
                attempt(`_`.`as`(PSElements.NullBinder)),
                attempt(ident + `@` + binderAtom).`as`(NamedBinder),
                attempt(ident.`as`(VarBinder)),
                attempt(qualPropName.`as`(ConstructorBinder)),
                attempt(boolean.`as`(BooleanBinder)),
                attempt(char).`as`(CharBinder),
                attempt(string.`as`(StringBinder)),
                attempt(number.`as`(NumberBinder)),
                attempt(squares(commaSep(expr)).`as`(ObjectBinder)),
                attempt(braces(commaSep(recordBinder))),
                attempt(parens(binder))
            )
        )
    }
}
