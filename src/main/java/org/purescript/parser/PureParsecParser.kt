package org.purescript.parser

import org.purescript.parser.Combinators.attempt
import org.purescript.parser.Combinators.braces
import org.purescript.parser.Combinators.choice
import org.purescript.parser.Combinators.commaSep
import org.purescript.parser.Combinators.commaSep1
import org.purescript.parser.Combinators.guard
import org.purescript.parser.Combinators.indented
import org.purescript.parser.Combinators.many1
import org.purescript.parser.Combinators.manyOrEmpty
import org.purescript.parser.Combinators.mark
import org.purescript.parser.Combinators.optional
import org.purescript.parser.Combinators.parens
import org.purescript.parser.Combinators.ref
import org.purescript.parser.Combinators.same
import org.purescript.parser.Combinators.sepBy
import org.purescript.parser.Combinators.sepBy1
import org.purescript.parser.Combinators.squares
import org.purescript.parser.Combinators.token
import org.purescript.parser.Combinators.untilSame
import org.purescript.parser.PSElements.Companion.Abs
import org.purescript.parser.PSElements.Companion.ArrayLiteral
import org.purescript.parser.PSElements.Companion.Bang
import org.purescript.parser.PSElements.Companion.Binder
import org.purescript.parser.PSElements.Companion.BooleanBinder
import org.purescript.parser.PSElements.Companion.BooleanLiteral
import org.purescript.parser.PSElements.Companion.CaseAlternative
import org.purescript.parser.PSElements.Companion.ConstrainedType
import org.purescript.parser.PSElements.Companion.Constructor
import org.purescript.parser.PSElements.Companion.ConstructorBinder
import org.purescript.parser.PSElements.Companion.DoNotationLet
import org.purescript.parser.PSElements.Companion.ExportedDataMember
import org.purescript.parser.PSElements.Companion.ExportedDataMemberList
import org.purescript.parser.PSElements.Companion.ExternDataDeclaration
import org.purescript.parser.PSElements.Companion.GenericIdentifier
import org.purescript.parser.PSElements.Companion.Guard
import org.purescript.parser.PSElements.Companion.Identifier
import org.purescript.parser.PSElements.Companion.NamedBinder
import org.purescript.parser.PSElements.Companion.NumberBinder
import org.purescript.parser.PSElements.Companion.NumericLiteral
import org.purescript.parser.PSElements.Companion.ObjectBinder
import org.purescript.parser.PSElements.Companion.ObjectBinderField
import org.purescript.parser.PSElements.Companion.PrefixValue
import org.purescript.parser.PSElements.Companion.ProperName
import org.purescript.parser.PSElements.Companion.Qualified
import org.purescript.parser.PSElements.Companion.Row
import org.purescript.parser.PSElements.Companion.RowKind
import org.purescript.parser.PSElements.Companion.Star
import org.purescript.parser.PSElements.Companion.StringBinder
import org.purescript.parser.PSElements.Companion.StringLiteral
import org.purescript.parser.PSElements.Companion.TypeArgs
import org.purescript.parser.PSElements.Companion.TypeClassDeclaration
import org.purescript.parser.PSElements.Companion.TypeConstructor
import org.purescript.parser.PSElements.Companion.TypeHole
import org.purescript.parser.PSElements.Companion.TypeInstanceDeclaration
import org.purescript.parser.PSElements.Companion.UnaryMinus
import org.purescript.parser.PSElements.Companion.ValueDeclaration
import org.purescript.parser.PSElements.Companion.VarBinder
import org.purescript.parser.PSElements.Companion.importModuleName
import org.purescript.parser.PSElements.Companion.pClassName
import org.purescript.parser.PSElements.Companion.pImplies
import org.purescript.parser.PSElements.Companion.qualifiedModuleName
import org.purescript.parser.PSTokens.Companion.ARROW
import org.purescript.parser.PSTokens.Companion.AS
import org.purescript.parser.PSTokens.Companion.BANG
import org.purescript.parser.PSTokens.Companion.CLASS
import org.purescript.parser.PSTokens.Companion.COMMA
import org.purescript.parser.PSTokens.Companion.DARROW
import org.purescript.parser.PSTokens.Companion.DATA
import org.purescript.parser.PSTokens.Companion.DCOLON
import org.purescript.parser.PSTokens.Companion.DDOT
import org.purescript.parser.PSTokens.Companion.DERIVE
import org.purescript.parser.PSTokens.Companion.DOT
import org.purescript.parser.PSTokens.Companion.EQ
import org.purescript.parser.PSTokens.Companion.FALSE
import org.purescript.parser.PSTokens.Companion.FLOAT
import org.purescript.parser.PSTokens.Companion.FORALL
import org.purescript.parser.PSTokens.Companion.FOREIGN
import org.purescript.parser.PSTokens.Companion.HIDING
import org.purescript.parser.PSTokens.Companion.IMPORT
import org.purescript.parser.PSTokens.Companion.INSTANCE
import org.purescript.parser.PSTokens.Companion.KIND
import org.purescript.parser.PSTokens.Companion.LDARROW
import org.purescript.parser.PSTokens.Companion.LET
import org.purescript.parser.PSTokens.Companion.LPAREN
import org.purescript.parser.PSTokens.Companion.MODULE
import org.purescript.parser.PSTokens.Companion.NATURAL
import org.purescript.parser.PSTokens.Companion.NEWTYPE
import org.purescript.parser.PSTokens.Companion.OPERATOR
import org.purescript.parser.PSTokens.Companion.PIPE
import org.purescript.parser.PSTokens.Companion.PROPER_NAME
import org.purescript.parser.PSTokens.Companion.RPAREN
import org.purescript.parser.PSTokens.Companion.START
import org.purescript.parser.PSTokens.Companion.STRING
import org.purescript.parser.PSTokens.Companion.TRUE
import org.purescript.parser.PSTokens.Companion.TYPE
import org.purescript.parser.PSTokens.Companion.WHERE

class PureParsecParser {
    private fun parseQualified(p: Parsec): Parsec =
        attempt(
            manyOrEmpty(
                attempt(token(PROPER_NAME).`as`(ProperName) + token(DOT))
            ) + p
        ).`as`(Qualified)

    // tokens
    private val char = token("'")
    private val dcolon = token(DCOLON)
    private val dot = token(DOT)
    private val ddot = token(DDOT)
    private val eq = token(EQ)
    private val string = token(STRING)
    private val where = token(WHERE)

    @Suppress("PrivatePropertyName", "unused")
    private val `@` = token("@")

    private val number =
        optional(token("+").or(token("-")))
            .then(token(NATURAL).or(token(FLOAT)))


    private val idents =
        choice(
            token(PSTokens.IDENT),
            token(AS),
            token(HIDING),
            token(FORALL),
            token(PSTokens.QUALIFIED),
            token(KIND),
        )
    private val case = token(PSTokens.CASE)
    private val of = token(PSTokens.OF)

    private val lname = choice(
        token(PSTokens.IDENT),
        token(DATA),
        token(NEWTYPE),
        token(TYPE),
        token(FOREIGN),
        token(IMPORT),
        token(PSTokens.INFIXL),
        token(PSTokens.INFIXR),
        token(PSTokens.INFIX),
        token(CLASS),
        token(DERIVE),
        token(KIND),
        token(INSTANCE),
        token(MODULE),
        case,
        of,
        token(PSTokens.IF),
        token(PSTokens.THEN),
        token(PSTokens.ELSE),
        token(PSTokens.DO),
        token(LET),
        token(TRUE),
        token(FALSE),
        token(PSTokens.IN),
        token(WHERE),
        token(FORALL),
        token(PSTokens.QUALIFIED),
        token(HIDING),
        token(AS)
    ).`as`(Identifier)
    private val operator =
        choice(
            token(OPERATOR),
            token(DOT),
            token(DDOT),
            token(PSTokens.LARROW),
            token(LDARROW),
            token(PSTokens.OPTIMISTIC)
        )
    private val properName: Parsec = token(PROPER_NAME).`as`(ProperName)
    private val moduleName = parseQualified(token(PROPER_NAME))
    private val stringLiteral = attempt(token(STRING))

    private fun indentedList(p: Parsec): Parsec =
        mark(manyOrEmpty(untilSame(same(p))))

    private fun indentedList1(p: Parsec): Parsec =
        mark(many1(untilSame(same(p))))

    // Kinds.hs
    private val parseKind = ref()
    private val parseKindPrefixRef = ref()
    private val parseKindAtom = indented(
        choice(
            token("*").`as`(START).`as`(Star),
            token("!").`as`(BANG).`as`(Bang),
            parseQualified(properName).`as`(TypeConstructor),
            parens(parseKind)
        )
    )
    private val parseKindPrefix =
        choice(
            (token("#") + parseKindPrefixRef).`as`(RowKind),
            parseKindAtom
        )

    // Types.hs
    private val type = ref()
    private val parseForAll = ref()
    private val `_` = token("_")
    private val parseTypeVariable: Parsec =
        guard(
            idents,
            { content: String? -> !(content == "∀" || content == "forall") },
            "not `forall`"
        )
            .`as`(GenericIdentifier)
    private val parseTypeConstructor: Parsec =
        parseQualified(properName).`as`(TypeConstructor)

    private fun parseNameAndType(p: Parsec): Parsec =
        indented(lname.or(stringLiteral).`as`(GenericIdentifier)) +
            indented(dcolon) + p

    private val parseRowEnding =
        optional(
            indented(token(PIPE)) +
                indented(
                    attempt(`_`)
                        .or(
                            attempt(
                                optional(
                                    manyOrEmpty(properName).`as`(
                                        TypeConstructor
                                    )
                                ) +
                                    optional(
                                        idents.`as`(
                                            GenericIdentifier
                                        )
                                    ) +
                                    optional(
                                        indented(
                                            lname.or(
                                                stringLiteral
                                            )
                                        )
                                    ) +
                                    optional(indented(dcolon)) +
                                    optional(type)
                            ).`as`(PSElements.TypeVar)
                        )
                )
        )
    private val parseRow: Parsec =
        commaSep(parseNameAndType(type)).then(parseRowEnding).`as`(Row)
    private val arrow = token(ARROW)

    private val typeAtom: Parsec =
        indented(
            choice(
                attempt(squares(optional(type))),
                attempt(parens(arrow)),
                attempt(braces(parseRow).`as`(PSElements.ObjectType)),
                attempt(`_`),
                attempt(parseForAll),
                attempt(parseTypeVariable),
                attempt(parseTypeConstructor),
                attempt(parens(parseRow)),
                attempt(parens(type))
            )
        ).`as`(PSElements.TypeAtom)
    private val parseConstrainedType: Parsec =
        optional(
            attempt(
                parens(
                    commaSep1(
                        parseQualified(properName).`as`(TypeConstructor) +
                            indented(manyOrEmpty(typeAtom))
                    )
                ) + token(DARROW)
            )
        ).then(indented(type)).`as`(ConstrainedType)
    private val forall = token(FORALL)

    private val ident =
        idents.`as`(Identifier)
            .or(attempt(parens(operator.`as`(Identifier))))

    // Declarations.hs
    private val typeVarBinding =
        idents.`as`(GenericIdentifier)
            .or(
                parens(
                    idents.`as`(GenericIdentifier)
                        .then(indented(dcolon))
                        .then(indented(parseKind))
                )
            )
    private val binderAtom = ref()
    private val binder = ref()
    private val expr = ref()
    private val parseLocalDeclarationRef = ref()
    private val parseGuard =
        (token(PIPE) + indented(commaSep(expr))).`as`(Guard)
    private val dataHead =
        token(DATA) +
            indented(properName).`as`(TypeConstructor) +
            manyOrEmpty(indented(typeVarBinding)).`as`(TypeArgs)

    val dataCtor =
        properName.`as`(TypeConstructor) + manyOrEmpty(indented(typeAtom))
    private val parseTypeDeclaration =
        (ident.`as`(PSElements.TypeAnnotationName) + dcolon + type)
            .`as`(PSElements.TypeDeclaration)
    private val newtypeHead =
        token(NEWTYPE) +
            indented(properName).`as`(TypeConstructor) +
            manyOrEmpty(indented(typeVarBinding))
                .`as`(TypeArgs)
    private val parseTypeSynonymDeclaration =
        token(TYPE)
            .then(token(PROPER_NAME).`as`(TypeConstructor))
            .then(manyOrEmpty(indented(typeVarBinding)))
            .then(indented(eq) + (type))
            .`as`(PSElements.TypeSynonymDeclaration)
    private val exprWhere =
        expr + optional(where + indentedList1(parseLocalDeclarationRef))

    private val parsePatternMatchObject =
        indented(
            braces(
                commaSep(
                    idents.or(lname).or(stringLiteral)
                        .then(optional(indented(eq.or(token(OPERATOR)))))
                        .then(optional(indented(binder)))
                )
            )
        ).`as`(Binder)
    private val parseRowPatternBinder =
        indented(token(OPERATOR)).then(indented(binder))
    private val guardedDeclExpr = parseGuard + eq + exprWhere
    private val guardedDecl =
        choice(attempt(eq) + exprWhere, indented(many1(guardedDeclExpr)))


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
        ).then(indented(token(DARROW)))
    private val parseExternDeclaration =
        token(FOREIGN)
            .then(indented(token(IMPORT)))
            .then(
                indented(
                    choice(
                        token(DATA)
                            .then(
                                indented(
                                    token(PROPER_NAME).`as`(
                                        TypeConstructor
                                    )
                                )
                            )
                            .then(dcolon).then(parseKind)
                            .`as`(ExternDataDeclaration),
                        token(INSTANCE)
                            .then(ident).then(indented(dcolon))
                            .then(optional(parseDeps))
                            .then(parseQualified(properName).`as`(pClassName))
                            .then(manyOrEmpty(indented(typeAtom)))
                            .`as`(PSElements.ExternInstanceDeclaration),
                        attempt(ident)
                            .then(optional(stringLiteral.`as`(PSElements.JSRaw)))
                            .then(indented(token(DCOLON)))
                            .then(type)
                            .`as`(PSElements.ForeignValueDeclaration)
                    )
                )
            )
    private val parseAssociativity = choice(
        token(PSTokens.INFIXL),
        token(PSTokens.INFIXR),
        token(PSTokens.INFIX)
    )
    private val parseFixity =
        parseAssociativity.then(indented(token(NATURAL))).`as`(
            PSElements.Fixity
        )
    private val parseFixityDeclaration = parseFixity
        .then(optional(token(TYPE)))
        .then(
            parseQualified(properName).`as`(PSElements.pModuleName)
                .or(ident.`as`(ProperName))
        )
        .then(token(AS))
        .then(operator)
        .`as`(PSElements.FixityDeclaration)
    private val parseDeclarationRef =
        choice(
            token("kind").then(parseQualified(properName).`as`(pClassName)),
            ident.`as`(PSElements.ValueRef),
            token(TYPE).then(optional(parens(operator))),
            token(MODULE).then(moduleName).`as`(importModuleName),
            token(CLASS).then(parseQualified(properName).`as`(pClassName)),
            properName.`as`(ProperName)
                .then(
                    optional(
                        parens(
                            optional(
                                choice(
                                    token(DDOT),
                                    commaSep1(properName.`as`(TypeConstructor))
                                )
                            )
                        )
                    )
                )
        ).`as`(PSElements.PositionedDeclarationRef)
    private val parseTypeClassDeclaration =
        token(CLASS)
            .then(
                optional(
                    indented(
                        choice(
                            parens(
                                commaSep1(
                                    parseQualified(properName)
                                        .`as`(TypeConstructor)
                                        .then(manyOrEmpty(typeAtom))
                                )
                            ),
                            commaSep1(
                                parseQualified(properName).`as`(TypeConstructor)
                                    .then(manyOrEmpty(typeAtom))
                            )
                        )
                    )
                        .then(optional(token(LDARROW)).`as`(pImplies))
                )
            ).then(optional(indented(properName.`as`(pClassName))))
            .then(optional(manyOrEmpty(indented(typeVarBinding))))
            .then(optional(token(PIPE).then(indented(commaSep1(type)))))
            .then(
                optional(
                    attempt(
                        indented(token(WHERE)).then(
                            indentedList(parseTypeDeclaration)
                        )
                    )
                )
            ).`as`(TypeClassDeclaration)
    private val parseTypeInstanceDeclaration =
        optional(token(DERIVE))
            .then(optional(token(NEWTYPE)))
            .then(
                token(INSTANCE)
                    .then(ident.`as`(GenericIdentifier).then(indented(dcolon)))
                    .then(
                        optional(
                            optional(token(LPAREN))
                                .then(
                                    commaSep1(
                                        parseQualified(properName)
                                            .`as`(TypeConstructor)
                                            .then(manyOrEmpty(typeAtom))
                                    )
                                )
                                .then(optional(token(RPAREN)))
                                .then(optional(indented(token(DARROW))))
                        )
                    )
                    .then(
                        optional(
                            indented(parseQualified(properName)).`as`(
                                pClassName
                            )
                        )
                    )
                    .then(manyOrEmpty(indented(typeAtom).or(token(STRING))))
                    .then(
                        optional(
                            indented(token(DARROW))
                                .then(optional(token(LPAREN)))
                                .then(
                                    parseQualified(properName).`as`(
                                        TypeConstructor
                                    )
                                )
                                .then(manyOrEmpty(typeAtom))
                                .then(optional(token(RPAREN)))
                        )
                    )
                    .then(
                        optional(
                            attempt(
                                indented(token(WHERE))
                                    .then(
                                        indented(
                                            indentedList(
                                                parseValueDeclaration
                                            )
                                        )
                                    )
                            )
                        )
                    )
            ).`as`(TypeInstanceDeclaration)
    private val importDeclarationType =
        optional(indented(parens(commaSep(parseDeclarationRef))))
    private val parseImportDeclaration =
        token(IMPORT)
            .then(indented(moduleName).`as`(importModuleName))
            .then(optional(token(HIDING)).then(importDeclarationType))
            .then(
                optional(
                    token(AS)
                        .then(moduleName)
                        .`as`(importModuleName)
                )
            )
            .`as`(PSElements.ImportDeclaration)
    private val decl = choice(
        (dataHead + optional(eq + sepBy1(dataCtor, PIPE)))
            .`as`(PSElements.DataDeclaration),
        (newtypeHead + eq + properName.`as`(TypeConstructor) + typeAtom)
            .`as`(PSElements.NewtypeDeclaration),
        attempt(parseTypeDeclaration),
        parseTypeSynonymDeclaration,
        attempt(ident)
            .then(manyOrEmpty(binderAtom))
            .then(guardedDecl).`as`(ValueDeclaration),
        parseExternDeclaration,
        parseFixityDeclaration,
        parseTypeClassDeclaration,
        parseTypeInstanceDeclaration
    )
    private val parseLocalDeclaration = choice(
        attempt(parseTypeDeclaration),
        // this is for when used with LET
        optional(attempt(token(LPAREN)))
            .then(
                optional(
                    attempt(properName).`as`(Constructor)
                )
            )
            .then(
                optional(
                    attempt(
                        many1(
                            ident
                        )
                    )
                )
            )
            .then(optional(
                attempt(
                    squares(commaSep(binder))
                        .`as`(ObjectBinder)
                )))
            .then(
                optional(
                    attempt(
                        indented(`@`)
                            .then(
                                indented(
                                    braces(
                                        commaSep(
                                            idents
                                        )
                                    )
                                )
                            )
                    )
                ).`as`(NamedBinder)
            ).then(
                optional(
                    attempt(parsePatternMatchObject)
                )
            )
            .then(optional(attempt(parseRowPatternBinder)))
            .then(optional(attempt(token(RPAREN))))
            // ---------- end of LET stuff -----------
            .then(
                attempt(
                    manyOrEmpty(
                        binderAtom
                    )
                )
            )
            .then(guardedDecl).`as`(ValueDeclaration)
    )
    private val exportedClass =
        token(CLASS)
            .then(properName)
            .`as`(PSElements.ExportedClass)
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
        token(KIND)
            .then(properName)
            .`as`(PSElements.ExportedKind)
    private val exportedModule =
        token(MODULE)
            .then(parseQualified(properName))
            .`as`(PSElements.ExportedModule)
    private val exportedOperator =
        parens(operator)
            .`as`(PSElements.ExportedOperator)
    private val exportedType =
        token(TYPE)
            .then(parens(operator))
            .`as`(PSElements.ExportedType)
    private val exportedValue =
        ident.`as`(PSElements.ExportedValue)
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

    private val elseDecl = token("else")
    private val moduleDecl =
        choice(
            parseImportDeclaration,
            sepBy(decl, elseDecl)
        )
    private val moduleDecls = indentedList(moduleDecl)

    val parseModule = token(MODULE)
        .then(indented(moduleName.`as`(PSElements.pModuleName)))
        .then(optional(exportList))
        .then(token(WHERE))
        .then(moduleDecls)
        .`as`(PSElements.Module)

    // Literals
    private val parseBooleanLiteral =
        token(TRUE).or(token(FALSE)).`as`(BooleanLiteral)
    private val parseNumericLiteral =
        token(NATURAL).or(token(FLOAT)).`as`(NumericLiteral)

    private val parseCharLiteral = char.`as`(StringLiteral)
    private val parseArrayLiteral = squares(commaSep(expr)).`as`(ArrayLiteral)
    private val parseTypeHole = token("?").`as`(TypeHole)
    private val parseIdentifierAndValue =
        indented(lname.or(stringLiteral))
            .then(optional(indented(token(OPERATOR).or(token(COMMA)))))
            .then(optional(indented(expr)))
            .`as`(ObjectBinderField)
    private val parseObjectLiteral =
        braces(commaSep(parseIdentifierAndValue)).`as`(PSElements.ObjectLiteral)
    private val typedIdent =
        optional(token(LPAREN))
            .then(
                many1(
                    idents.`as`(GenericIdentifier)
                        .or(parseQualified(properName).`as`(TypeConstructor))
                )
            )
            .then(optional(indented(dcolon).then(indented(type))))
            .then(optional(parseObjectLiteral))
            .then(optional(token(RPAREN)))
    private val parseAbs =
        token(PSTokens.BACKSLASH)
            .then(
                choice(
                    many1(typedIdent).`as`(Abs),
                    many1(indented(ident.or(binderAtom).`as`(Abs)))
                )
            )
            .then(indented(arrow))
            .then(expr)
    private val parseVar =
        attempt(
            manyOrEmpty(
                attempt(
                    token(PROPER_NAME)
                        .`as`(qualifiedModuleName)
                        .then(token(DOT))
                )
            ).then(ident).`as`(Qualified)
        ).`as`(PSElements.Var)
    private val parseConstructor =
        parseQualified(properName).`as`(Constructor)

    private val binder1 = expr.or(`_`)

    private val guardedCaseExpr = parseGuard + indented(arrow + exprWhere)
    private val guardedCase =
        indented(choice(arrow + exprWhere, many1(guardedCaseExpr)))
    private val caseBranch =
        (commaSep1(binder1) + guardedCase).`as`(CaseAlternative)

    private val parseIfThenElse = token(PSTokens.IF)
        .then(indented(expr))
        .then(indented(token(PSTokens.THEN)))
        .then(indented(expr))
        .then(indented(token(PSTokens.ELSE)))
        .then(indented(expr))
        .`as`(PSElements.IfThenElse)
    private val parseLet = token(LET)
        .then(indented(indentedList1(parseLocalDeclaration)))
        .then(indented(token(PSTokens.IN)))
        .then(expr)
        .`as`(PSElements.Let)
    private val letBinding =
        choice(
            attempt(parseTypeDeclaration),
            optional(attempt(token(LPAREN)))
                .then(optional(attempt(properName).`as`(Constructor)))
                .then(optional(attempt(many1(ident))))
                .then(optional(
                    attempt(
                        squares(commaSep(binder))
                            .`as`(ObjectBinder)
                    )))
                .then(
                    optional(
                        attempt(
                            indented(`@`)
                                .then(indented(braces(commaSep(idents))))
                        )
                    ).`as`(NamedBinder)
                )
                .then(optional(attempt(parsePatternMatchObject)))
                .then(optional(attempt(parseRowPatternBinder)))
                .then(optional(attempt(token(RPAREN))))
                .then(attempt(manyOrEmpty(binderAtom)))
                .then(
                    choice(
                        attempt(
                            indented(
                                many1(
                                    parseGuard + indented(eq + exprWhere)
                                )
                            )
                        ),
                        attempt(indented(eq + (exprWhere)))
                    )
                ).`as`(ValueDeclaration)
        )
    private val parseDoNotationBind: Parsec =
        binder
            .then(indented(token(PSTokens.LARROW)).then(expr))
            .`as`(PSElements.DoNotationBind)
    private val doExpr = expr.`as`(PSElements.DoNotationValue)
    private val doStatement =
        choice(
            token(LET)
                .then(indented(indentedList1(letBinding)))
                .`as`(DoNotationLet),
            attempt(parseDoNotationBind),
            attempt(doExpr)
        )
    private val doBlock =
        token(PSTokens.DO)
            .then(indented(indentedList(mark(doStatement))))
    private val parsePropertyUpdate =
        lname.or(stringLiteral)
            .then(optional(indented(eq)))
            .then(indented(expr))
    private val parseValueAtom = choice(
        attempt(parseTypeHole),
        attempt(parseNumericLiteral),
        attempt(token(STRING).`as`(StringLiteral)),
        attempt(parseBooleanLiteral),
        attempt(
            token(PSTokens.TICK) +
                properName.`as`(ProperName)
                    .or(many1(idents.`as`(ProperName))) +
                token(PSTokens.TICK)
        ),
        parseArrayLiteral,
        parseCharLiteral,
        attempt(indented(braces(commaSep1(indented(parsePropertyUpdate))))),
        attempt(parseObjectLiteral),
        parseAbs,
        attempt(parseConstructor),
        attempt(parseVar),
        (case + commaSep1(expr) + of + indentedList(caseBranch))
            .`as`(PSElements.Case),
        parseIfThenElse,
        doBlock,
        parseLet,
        parens(expr).`as`(PSElements.Parens)
    )
    private val parseAccessor: Parsec =
        attempt(indented(token(DOT)).then(indented(lname.or(stringLiteral))))
            .`as`(PSElements.Accessor)
    private val parseIdentInfix: Parsec =
        choice(
            (token(PSTokens.TICK) + parseQualified(idents))
                .lexeme(PSTokens.TICK),
            parseQualified(operator)
        ).`as`(PSElements.IdentInfix)
    private val indexersAndAccessors =
        parseValueAtom +
            manyOrEmpty(
                choice(
                    parseAccessor,
                    attempt(
                        indented(
                            braces(commaSep1(indented(parsePropertyUpdate)))
                        )
                    ),
                    indented(dcolon + type)
                )
            )
    private val parseValuePostFix =
        indexersAndAccessors +
            manyOrEmpty(
                indented(indexersAndAccessors)
                    .or(attempt(indented(dcolon) + type))
            )
    private val parsePrefix = ref()

    // Binder
    private val parseIdentifierAndBinder =
        lname.or(stringLiteral)
            .then(indented(eq.or(token(OPERATOR))))
            .then(indented(binder))

    private val type0 = ref()
    private val type1 = ref()
    private val type2 = ref()
    private val type3 = ref()
    private val type4 = ref()
    private val type5 = ref()
    private val darrow = token(DARROW)
    private val qualOp = choice(
        operator,
        token("<="),
        token("-"),
        token("#"),
        token(":"),
    )

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
                    arrow.or(
                        optional(
                            parseQualified(properName).`as`(TypeConstructor)
                        )
                    ) + optional(parseKind)
                )).`as`(PSElements.FunKind)
        )
        type.setRef(
            many1(typeAtom.or(string) + optional(dcolon + parseKind))
                .then(
                    optional(
                        choice(
                            arrow,
                            token(DARROW),
                            token(PSTokens.OPTIMISTIC),
                            token(OPERATOR)
                        )
                            .then(type)
                    )
                ).`as`(PSElements.Type)
        )
        parseForAll.setRef(
            forall
                .then(many1(indented(idents.`as`(GenericIdentifier))))
                .then(indented(dot))
                .then(parseConstrainedType).`as`(PSElements.ForAll)
        )
        parseLocalDeclarationRef.setRef(parseLocalDeclaration)
        parsePrefix.setRef(
            choice(
                parseValuePostFix,
                indented(token("-")).then(parsePrefix).`as`(UnaryMinus)
            ).`as`(PrefixValue)
        )
        expr.setRef(
            (parsePrefix + optional(attempt(indented(parseIdentInfix)) + expr))
                .`as`(PSElements.Value)
        )
        binder.setRef(
            choice(
                `_`.`as`(PSElements.NullBinder),
                attempt(token(STRING).`as`(StringBinder)),
                attempt(token("true").or(token("false")).`as`(BooleanBinder)),
                attempt(
                    optional(token("+").or(token("-")))
                        .then(token(NATURAL).or(token(FLOAT)))
                        .`as`(NumberBinder)
                ),
                attempt(
                    ident.then(indented(`@`).then(indented(binder)))
                        .`as`(NamedBinder)
                ),
                attempt(ident.`as`(VarBinder)),
                attempt(
                    parseQualified(properName).`as`(GenericIdentifier)
                        .then(manyOrEmpty(indented(binderAtom)))
                        .`as`(ConstructorBinder)
                ),
                attempt(
                    braces(commaSep(parseIdentifierAndBinder))
                        .`as`(ObjectBinder)
                ),
                attempt(
                    squares(commaSep(binder))
                        .`as`(ObjectBinder)
                ),
                attempt(indented(braces(commaSep(idents))).`as`(Binder)),
                attempt(char.`as`(StringBinder)),
                attempt(parens(binder))
            ).then(optional(token(OPERATOR).then(binder))).`as`(Binder)
        )
        val boolean = token("true").or(token("false"))
        val qualPropName = parseQualified(properName.`as`(ProperName))
        val recordBinder =
            idents +
                optional(token("=").or(token(":") + binder))
        binderAtom.setRef(
            choice(
                attempt(`_`.`as`(PSElements.NullBinder)),
                attempt(ident + `@` + binderAtom).`as`(NamedBinder),
                attempt(ident.`as`(VarBinder)),
                attempt(qualPropName.`as`(ConstructorBinder)),
                attempt(boolean.`as`(BooleanBinder)),
                attempt(char.`as`(StringBinder)),
                attempt(string.`as`(StringBinder)),
                attempt(number.`as`(NumberBinder)),
                attempt(squares(commaSep(expr)).`as`(ObjectBinder)),
                attempt(braces(commaSep(recordBinder))),
                attempt(parens(binder))
            )
        )
    }
}
