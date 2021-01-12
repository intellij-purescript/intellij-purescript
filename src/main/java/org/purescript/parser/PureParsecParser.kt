package org.purescript.parser

import org.purescript.psi.PSElements
import org.purescript.psi.PSTokens

class PureParsecParser {
    private fun parseQualified(p: Parsec): Parsec =
        Combinators.attempt(
            Combinators.manyOrEmpty(
                Combinators.attempt(
                    Combinators.token(PSTokens.PROPER_NAME)
                        .`as`(PSElements.ProperName) + Combinators.token(
                        PSTokens.DOT
                    )
                )
            ) + p
        ).`as`(PSElements.Qualified)

    // tokens
    private val dcolon = Combinators.lexeme(PSTokens.DCOLON)
    private val dot = Combinators.lexeme(PSTokens.DOT)
    private val eq = Combinators.lexeme(PSTokens.EQ)
    private val where = Combinators.lexeme(PSTokens.WHERE)

    private val idents =
        Combinators.choice(
            Combinators.token(PSTokens.IDENT),
            Combinators.token(PSTokens.AS),
            Combinators.token(PSTokens.HIDING),
            Combinators.token(PSTokens.FORALL),
            Combinators.token(PSTokens.QUALIFIED),
        )
    private val lname = Combinators.lexeme(
        Combinators.choice(
            Combinators.token(PSTokens.IDENT),
            Combinators.token(PSTokens.DATA),
            Combinators.token(PSTokens.NEWTYPE),
            Combinators.token(PSTokens.TYPE),
            Combinators.token(PSTokens.FOREIGN),
            Combinators.token(PSTokens.IMPORT),
            Combinators.token(PSTokens.INFIXL),
            Combinators.token(PSTokens.INFIXR),
            Combinators.token(PSTokens.INFIX),
            Combinators.token(PSTokens.CLASS),
            Combinators.token(PSTokens.DERIVE),
            Combinators.token(PSTokens.INSTANCE),
            Combinators.token(PSTokens.MODULE),
            Combinators.token(PSTokens.CASE),
            Combinators.token(PSTokens.OF),
            Combinators.token(PSTokens.IF),
            Combinators.token(PSTokens.THEN),
            Combinators.token(PSTokens.ELSE),
            Combinators.token(PSTokens.DO),
            Combinators.token(PSTokens.LET),
            Combinators.token(PSTokens.TRUE),
            Combinators.token(PSTokens.FALSE),
            Combinators.token(PSTokens.IN),
            Combinators.token(PSTokens.WHERE),
            Combinators.token(PSTokens.FORALL),
            Combinators.token(PSTokens.QUALIFIED),
            Combinators.token(PSTokens.HIDING),
            Combinators.token(PSTokens.AS)
        ).`as`(PSElements.Identifier)
    )
    private val operator =
        Combinators.choice(
            Combinators.token(PSTokens.OPERATOR),
            Combinators.token(PSTokens.DOT),
            Combinators.token(PSTokens.DDOT),
            Combinators.token(PSTokens.LARROW),
            Combinators.token(PSTokens.LDARROW),
            Combinators.token(PSTokens.OPTIMISTIC)
        )
    private val properName: Parsec = Combinators.lexeme(PSTokens.PROPER_NAME)
        .`as`(PSElements.ProperName)
    private val moduleName =
        Combinators.lexeme(parseQualified(Combinators.token(PSTokens.PROPER_NAME)))
    private val stringLiteral =
        Combinators.attempt(Combinators.lexeme(PSTokens.STRING))
    private fun indentedList(p: Parsec): Parsec =
        Combinators.mark(
            Combinators.manyOrEmpty(
                Combinators.untilSame(
                    Combinators.same(p)
                )
            )
        )

    private fun indentedList1(p: Parsec): Parsec =
        Combinators.mark(
            Combinators.many1(
                Combinators.untilSame(
                    Combinators.same(
                        p
                    )
                )
            )
        )

    // Kinds.hs
    private val parseKind = Combinators.ref()
    private val parseKindPrefixRef = Combinators.ref()
    private val parseKindAtom = Combinators.indented(
        Combinators.choice(
            Combinators.reserved("*").`as`(PSTokens.START)
                .`as`(PSElements.Star),
            Combinators.reserved("!").`as`(PSTokens.BANG).`as`(PSElements.Bang),
            parseQualified(properName).`as`(PSElements.TypeConstructor),
            Combinators.parens(parseKind)
        )
    )
    private val parseKindPrefix =
        Combinators.choice(
            (Combinators.lexeme("#") + parseKindPrefixRef).`as`(PSElements.RowKind),
            parseKindAtom
        )

    // Types.hs
    private val type = Combinators.ref()
    private val parseForAllRef = Combinators.ref()
    private val parseTypeWildcard = Combinators.reserved("_")
    private val parseFunction =
        Combinators.parens(Combinators.reserved(PSTokens.ARROW))
    private val parseTypeVariable: Parsec = Combinators.lexeme(
        Combinators.guard(
            idents,
            { content: String? -> !(content == "∀" || content == "forall") },
            "not `forall`"
        )
    ).`as`(PSElements.GenericIdentifier)
    private val parseTypeConstructor: Parsec =
        parseQualified(properName).`as`(PSElements.TypeConstructor)

    private fun parseNameAndType(p: Parsec): Parsec =
        Combinators.indented(
            Combinators.lexeme(
                lname.or(stringLiteral).`as`(PSElements.GenericIdentifier)
            )
        ) + Combinators.indented(dcolon) + p

    private val parseRowEnding =
        Combinators.optional(
            Combinators.indented(Combinators.lexeme(PSTokens.PIPE)) +
                Combinators.indented(
                    Combinators.attempt(parseTypeWildcard)
                        .or(
                            Combinators.attempt(
                                Combinators.optional(
                                    Combinators.lexeme(
                                        Combinators.manyOrEmpty(properName)
                                            .`as`(PSElements.TypeConstructor)
                                    )
                                ) +
                                    Combinators.optional(
                                        Combinators.lexeme(idents)
                                            .`as`(PSElements.GenericIdentifier)
                                    ) +
                                    Combinators.optional(
                                        Combinators.indented(
                                            Combinators.lexeme(
                                                lname.or(stringLiteral)
                                            )
                                        )
                                    ) +
                                    Combinators.optional(
                                        Combinators.indented(
                                            dcolon
                                        )
                                    ) +
                                    Combinators.optional(type)
                            ).`as`(PSElements.TypeVar)
                        )
                )
        )
    private val parseRow: Parsec =
        Combinators.commaSep(parseNameAndType(type))
            .then(parseRowEnding)
            .`as`(PSElements.Row)
    private val parseObject: Parsec = Combinators.braces(parseRow)
        .`as`(PSElements.ObjectType)
    private val typeAtom: Parsec = Combinators.indented(
        Combinators.choice(
            Combinators.attempt(Combinators.squares(Combinators.optional(type))),
            Combinators.attempt(parseFunction),
            Combinators.attempt(parseObject),
            Combinators.attempt(parseTypeWildcard),
            Combinators.attempt(parseTypeVariable),
            Combinators.attempt(parseTypeConstructor),
            Combinators.attempt(parseForAllRef),
            Combinators.attempt(Combinators.parens(parseRow)),
            Combinators.attempt(Combinators.parens(type))
        )
    ).`as`(PSElements.TypeAtom)
    private val parseConstrainedType: Parsec =
        Combinators.optional(
            Combinators.attempt(
                Combinators.parens(
                    Combinators.commaSep1(
                        parseQualified(properName).`as`(PSElements.TypeConstructor) +
                            Combinators.indented(
                                Combinators.manyOrEmpty(
                                    typeAtom
                                )
                            )
                    )
                ) + Combinators.lexeme(PSTokens.DARROW)
            )
        ).then(Combinators.indented(type)).`as`(PSElements.ConstrainedType)
    private val forlall = Combinators.reserved(PSTokens.FORALL)

    private val parseForAll = forlall
        .then(
            Combinators.many1(
                Combinators.indented(
                    Combinators.lexeme(idents)
                        .`as`(PSElements.GenericIdentifier)
                )
            )
        )
        .then(Combinators.indented(dot))
        .then(parseConstrainedType).`as`(PSElements.ForAll)
    private val ident =
        Combinators.lexeme(idents.`as`(PSElements.Identifier))
        .or(
            Combinators.attempt(
                Combinators.parens(
                    Combinators.lexeme(
                        operator.`as`(
                            PSElements.Identifier
                        )
                    )
                )
            )
        )

    // Declarations.hs
    private val typeVarBinding =
        Combinators.lexeme(idents).`as`(PSElements.GenericIdentifier)
        .or(
            Combinators.parens(
                Combinators.lexeme(idents).`as`(PSElements.GenericIdentifier)
                    .then(Combinators.indented(dcolon))
                    .then(Combinators.indented(parseKind))
            )
        )
    private val parseBinderNoParensRef = Combinators.ref()
    private val parseBinderRef = Combinators.ref()
    private val expr = Combinators.ref()
    private val parseLocalDeclarationRef = Combinators.ref()
    private val parseGuard =
        (Combinators.lexeme(PSTokens.PIPE) + Combinators.indented(
            Combinators.commaSep(
                expr
            )
        )).`as`(PSElements.Guard)
    private val dataHead =
        Combinators.reserved(PSTokens.DATA) +
            Combinators.indented(properName).`as`(PSElements.TypeConstructor) +
            Combinators.manyOrEmpty(Combinators.indented(typeVarBinding))
                .`as`(PSElements.TypeArgs)

    val dataCtor =
        properName.`as`(PSElements.TypeConstructor) +
            Combinators.manyOrEmpty(Combinators.indented(typeAtom))
    private val parseTypeDeclaration =
        (ident.`as`(PSElements.TypeAnnotationName) + dcolon + type)
            .`as`(PSElements.TypeDeclaration)

    private val newtypeHead =
        Combinators.reserved(PSTokens.NEWTYPE) +
            Combinators.indented(properName).`as`(PSElements.TypeConstructor) +
            Combinators.manyOrEmpty(Combinators.indented(typeVarBinding))
                .`as`(PSElements.TypeArgs)
    private val parseTypeSynonymDeclaration =
        Combinators.reserved(PSTokens.TYPE)
            .then(
                Combinators.reserved(PSTokens.PROPER_NAME)
                    .`as`(PSElements.TypeConstructor))
            .then(
                Combinators.manyOrEmpty(
                    Combinators.indented(
                        Combinators.lexeme(
                            typeVarBinding
                        )
                    )
                )
            )
            .then(Combinators.indented(eq) + (type))
            .`as`(PSElements.TypeSynonymDeclaration)
    private val exprWhere =
        expr + Combinators.optional(
            where + indentedList1(
                parseLocalDeclarationRef
            )
        )

    // Some Binders - rest at the bottom
    private val parseArrayBinder =
        Combinators.squares(Combinators.commaSep(parseBinderRef))
            .`as`(PSElements.ObjectBinder)
    private val parsePatternMatchObject =
        Combinators.indented(
            Combinators.braces(
                Combinators.commaSep(
                    Combinators.lexeme(idents).or(lname).or(stringLiteral)
                        .then(
                            Combinators.optional(
                                Combinators.indented(
                                    eq.or(
                                        Combinators.lexeme(PSTokens.OPERATOR)
                                    )
                                )
                            )
                        )
                        .then(
                            Combinators.optional(
                                Combinators.indented(
                                    parseBinderRef
                                )
                            )
                        )
                )
            )
        ).`as`(PSElements.Binder)
    private val parseRowPatternBinder =
        Combinators.indented(Combinators.lexeme(PSTokens.OPERATOR))
            .then(Combinators.indented(parseBinderRef))
    private val guardedDeclExpr = parseGuard + eq + exprWhere
    private val guardedDecl =
        Combinators.choice(
            Combinators.attempt(eq) + exprWhere,
            Combinators.indented(Combinators.many1(guardedDeclExpr)),
        )

    private val parseValueDeclaration // this is for when used with LET
        = Combinators.optional(Combinators.attempt(Combinators.reserved(PSTokens.LPAREN)))
        .then(
            Combinators.optional(
                Combinators.attempt(properName).`as`(PSElements.Constructor)
            )
        )
        .then(Combinators.optional(Combinators.attempt(Combinators.many1(ident))))
        .then(Combinators.optional(Combinators.attempt(parseArrayBinder)))
        .then(
            Combinators.optional(
                Combinators.attempt(
                    Combinators.indented(Combinators.lexeme("@"))
                        .then(
                            Combinators.indented(
                                Combinators.braces(
                                    Combinators.commaSep(
                                        Combinators.lexeme(
                                            idents
                                        )
                                    )
                                )
                            )
                        )
                )
            ).`as`(PSElements.NamedBinder)
        ).then(Combinators.optional(Combinators.attempt(parsePatternMatchObject)))
        .then(Combinators.optional(Combinators.attempt(parseRowPatternBinder)))
        .then(
            Combinators.optional(
                Combinators.attempt(
                    Combinators.reserved(
                        PSTokens.RPAREN
                    )
                )
            )
        )
        // ---------- end of LET stuff -----------
        .then(Combinators.attempt(Combinators.manyOrEmpty(parseBinderNoParensRef)))
        .then(guardedDecl).`as`(PSElements.ValueDeclaration)
    private val parseDeps =
        Combinators.parens(
            Combinators.commaSep1(
                parseQualified(properName).`as`(PSElements.TypeConstructor)
                    .then(Combinators.manyOrEmpty(typeAtom))
            )
        ).then(Combinators.indented(Combinators.reserved(PSTokens.DARROW)))
    private val parseExternDeclaration =
        Combinators.reserved(PSTokens.FOREIGN)
        .then(Combinators.indented(Combinators.reserved(PSTokens.IMPORT)))
        .then(
            Combinators.indented(
                Combinators.choice(
                    Combinators.reserved(PSTokens.DATA)
                        .then(
                            Combinators.indented(
                                Combinators.reserved(PSTokens.PROPER_NAME)
                                    .`as`(PSElements.TypeConstructor)
                            )
                        )
                        .then(dcolon).then(parseKind)
                        .`as`(PSElements.ExternDataDeclaration),
                    Combinators.reserved(PSTokens.INSTANCE)
                        .then(ident).then(Combinators.indented(dcolon))
                        .then(Combinators.optional(parseDeps))
                        .then(parseQualified(properName).`as`(PSElements.pClassName))
                        .then(
                            Combinators.manyOrEmpty(
                                Combinators.indented(
                                    typeAtom
                                )
                            )
                        )
                        .`as`(PSElements.ExternInstanceDeclaration),
                    Combinators.attempt(ident)
                        .then(Combinators.optional(stringLiteral.`as`(PSElements.JSRaw)))
                        .then(Combinators.indented(Combinators.lexeme(PSTokens.DCOLON)))
                        .then(type)
                        .`as`(PSElements.ExternDeclaration)
                )
            )
        )
    private val parseAssociativity = Combinators.choice(
        Combinators.reserved(PSTokens.INFIXL),
        Combinators.reserved(PSTokens.INFIXR),
        Combinators.reserved(PSTokens.INFIX)
    )
    private val parseFixity =
        parseAssociativity.then(Combinators.indented(Combinators.lexeme(PSTokens.NATURAL))).`as`(
            PSElements.Fixity
        )
    private val parseFixityDeclaration = parseFixity
        .then(Combinators.optional(Combinators.reserved(PSTokens.TYPE)))
        .then(
            parseQualified(properName).`as`(PSElements.pModuleName)
            .or(ident.`as`(PSElements.ProperName))
        )
        .then(Combinators.reserved(PSTokens.AS))
        .then(Combinators.lexeme(operator))
        .`as`(PSElements.FixityDeclaration)
    private val parseDeclarationRef =
        Combinators.choice(
            Combinators.reserved("kind")
                .then(parseQualified(properName).`as`(PSElements.pClassName)),
            ident.`as`(PSElements.ValueRef),
            Combinators.reserved(PSTokens.TYPE)
                .then(Combinators.optional(Combinators.parens(operator))),
            Combinators.reserved(PSTokens.MODULE).then(moduleName)
                .`as`(PSElements.importModuleName),
            Combinators.reserved(PSTokens.CLASS)
                .then(parseQualified(properName).`as`(PSElements.pClassName)),
            properName.`as`(PSElements.ProperName)
                .then(
                    Combinators.optional(
                        Combinators.parens(
                            Combinators.optional(
                                Combinators.choice(
                                    Combinators.reserved(PSTokens.DDOT),
                                    Combinators.commaSep1(
                                        properName.`as`(
                                            PSElements.TypeConstructor
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
        ).`as`(PSElements.PositionedDeclarationRef)
    private val parseTypeClassDeclaration =
        Combinators.lexeme(PSTokens.CLASS)
        .then(
            Combinators.optional(
                Combinators.indented(
                    Combinators.choice(
                        Combinators.parens(
                            Combinators.commaSep1(
                                parseQualified(properName).`as`(PSElements.TypeConstructor)
                                    .then(Combinators.manyOrEmpty(typeAtom))
                            )
                        ),
                        Combinators.commaSep1(
                            parseQualified(properName).`as`(PSElements.TypeConstructor)
                                .then(Combinators.manyOrEmpty(typeAtom))
                        )
                    )
                )
                    .then(
                        Combinators.optional(Combinators.reserved(PSTokens.LDARROW))
                            .`as`(PSElements.pImplies)
                    )
            )
        ).then(
                Combinators.optional(
                    Combinators.indented(
                        properName.`as`(
                            PSElements.pClassName
                        )
                    )
                )
            )
        .then(
            Combinators.optional(
                Combinators.manyOrEmpty(
                    Combinators.indented(
                        typeVarBinding
                    )
                )
            )
        )
        .then(
            Combinators.optional(
                Combinators.lexeme(PSTokens.PIPE)
                    .then(Combinators.indented(Combinators.commaSep1(type)))
            )
        )
        .then(
            Combinators.optional(
                Combinators.attempt(
                    Combinators.indented(Combinators.reserved(PSTokens.WHERE))
                        .then(indentedList(parseTypeDeclaration))
                )
            )
        ).`as`(PSElements.TypeClassDeclaration)
    private val parseTypeInstanceDeclaration =
        Combinators.optional(Combinators.reserved(PSTokens.DERIVE))
        .then(Combinators.optional(Combinators.reserved(PSTokens.NEWTYPE)))
        .then(
            Combinators.reserved(PSTokens.INSTANCE)
            .then(ident.`as`(PSElements.GenericIdentifier).then(
                Combinators.indented(
                    dcolon
                )
            ))
            .then(
                Combinators.optional(
                    Combinators.optional(Combinators.reserved(PSTokens.LPAREN))
                        .then(
                            Combinators.commaSep1(
                                parseQualified(properName).`as`(PSElements.TypeConstructor)
                                    .then(Combinators.manyOrEmpty(typeAtom))
                            )
                        )
                        .then(Combinators.optional(Combinators.reserved(PSTokens.RPAREN)))
                        .then(
                            Combinators.optional(
                                Combinators.indented(
                                    Combinators.reserved(PSTokens.DARROW)
                                )
                            )
                        )
                )
            )
            .then(
                Combinators.optional(
                    Combinators.indented(parseQualified(properName))
                        .`as`(PSElements.pClassName)
                )
            ).then(
                    Combinators.manyOrEmpty(
                        Combinators.indented(typeAtom)
                            .or(Combinators.lexeme(PSTokens.STRING))
                    )
                )
            .then(
                Combinators.optional(
                    Combinators.indented(Combinators.reserved(PSTokens.DARROW))
                        .then(Combinators.optional(Combinators.reserved(PSTokens.LPAREN)))
                        .then(parseQualified(properName).`as`(PSElements.TypeConstructor))
                        .then(Combinators.manyOrEmpty(typeAtom))
                        .then(Combinators.optional(Combinators.reserved(PSTokens.RPAREN)))
                )
            )
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        Combinators.indented(Combinators.reserved(PSTokens.WHERE))
                            .then(
                                Combinators.indented(
                                    indentedList(
                                        parseValueDeclaration
                                    )
                                )
                            )
                    )
                )
            )
        ).`as`(PSElements.TypeInstanceDeclaration)
    private val importDeclarationType =
        Combinators.optional(
            Combinators.indented(
                Combinators.parens(
                    Combinators.commaSep(
                        parseDeclarationRef
                    )
                )
            )
        )
    private val parseImportDeclaration =
        Combinators.reserved(PSTokens.IMPORT)
            .then(
                Combinators.indented(moduleName)
                    .`as`(PSElements.importModuleName))
            .then(Combinators.optional(Combinators.reserved(PSTokens.HIDING)).then(importDeclarationType))
            .then(
                Combinators.optional(
                    Combinators.reserved(PSTokens.AS).then(moduleName)
                        .`as`(PSElements.importModuleName)
                )
            ).`as`(PSElements.ImportDeclaration)
    private val decl = Combinators.choice(
        (dataHead + Combinators.optional(
            eq + Combinators.sepBy1(
                dataCtor,
                PSTokens.PIPE
            )
        ))
            .`as`(PSElements.DataDeclaration),
        (newtypeHead + eq + properName.`as`(PSElements.TypeConstructor) + typeAtom)
            .`as`(PSElements.NewtypeDeclaration),
        Combinators.attempt(parseTypeDeclaration),
        parseTypeSynonymDeclaration,
        Combinators.optional(Combinators.attempt(Combinators.reserved(PSTokens.LPAREN)))
            .then(
                Combinators.optional(
                    Combinators.attempt(properName).`as`(PSElements.Constructor)
                )
            )
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        Combinators.many1(
                            ident
                        )
                    )
                )
            )
            .then(Combinators.optional(Combinators.attempt(parseArrayBinder)))
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        Combinators.indented(Combinators.lexeme("@"))
                            .then(
                                Combinators.indented(
                                    Combinators.braces(
                                        Combinators.commaSep(
                                            Combinators.lexeme(
                                                idents
                                            )
                                        )
                                    )
                                )
                            )
                    )
                ).`as`(PSElements.NamedBinder)
            ).then(
                Combinators.optional(
                    Combinators.attempt(parsePatternMatchObject)
                )
            )
            .then(Combinators.optional(Combinators.attempt(parseRowPatternBinder)))
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        Combinators.reserved(
                            PSTokens.RPAREN
                        )
                    )
                )
            )
            .then(
                Combinators.attempt(
                    Combinators.manyOrEmpty(
                        parseBinderNoParensRef
                    )
                )
            )
            .then(guardedDecl).`as`(PSElements.ValueDeclaration),
        parseExternDeclaration,
        parseFixityDeclaration,
        parseImportDeclaration,
        parseTypeClassDeclaration,
        parseTypeInstanceDeclaration
    )
    private val parseLocalDeclaration = Combinators.choice(
        Combinators.attempt(parseTypeDeclaration),
        // this is for when used with LET
        Combinators.optional(Combinators.attempt(Combinators.reserved(PSTokens.LPAREN)))
            .then(
                Combinators.optional(
                    Combinators.attempt(properName).`as`(PSElements.Constructor)
                )
            )
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        Combinators.many1(
                            ident
                        )
                    )
                )
            )
            .then(Combinators.optional(Combinators.attempt(parseArrayBinder)))
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        Combinators.indented(Combinators.lexeme("@"))
                            .then(
                                Combinators.indented(
                                    Combinators.braces(
                                        Combinators.commaSep(
                                            Combinators.lexeme(
                                                idents
                                            )
                                        )
                                    )
                                )
                            )
                    )
                ).`as`(PSElements.NamedBinder)
            ).then(
                Combinators.optional(
                    Combinators.attempt(parsePatternMatchObject)
                )
            )
            .then(Combinators.optional(Combinators.attempt(parseRowPatternBinder)))
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        Combinators.reserved(
                            PSTokens.RPAREN
                        )
                    )
                )
            )
            // ---------- end of LET stuff -----------
            .then(
                Combinators.attempt(
                    Combinators.manyOrEmpty(
                        parseBinderNoParensRef
                    )
                )
            )
            .then(guardedDecl).`as`(PSElements.ValueDeclaration)
    )
    private val parseModule = Combinators.reserved(PSTokens.MODULE)
        .then(Combinators.indented(moduleName).`as`(PSElements.pModuleName))
        .then(
            Combinators.optional(
                Combinators.parens(
                    Combinators.commaSep1(
                        parseDeclarationRef
                    )
                )
            )
        )
        .then(Combinators.reserved(PSTokens.WHERE))
        .then(indentedList(decl))
        .`as`(PSElements.Module)
    val program: Parsec = indentedList(parseModule).`as`(PSElements.Program)

    // Literals
    private val parseBooleanLiteral =
        Combinators.reserved(PSTokens.TRUE)
            .or(Combinators.reserved(PSTokens.FALSE)).`as`(PSElements.BooleanLiteral)
    private val parseNumericLiteral =
        Combinators.reserved(PSTokens.NATURAL)
            .or(Combinators.reserved(PSTokens.FLOAT)).`as`(PSElements.NumericLiteral)
    private val parseStringLiteral =
        Combinators.reserved(PSTokens.STRING).`as`(PSElements.StringLiteral)
    private val parseCharLiteral =
        Combinators.lexeme("'").`as`(PSElements.StringLiteral)
    private val parseArrayLiteral =
        Combinators.squares(Combinators.commaSep(expr))
            .`as`(PSElements.ArrayLiteral)
    private val parseTypeHole =
        Combinators.lexeme("?").`as`(PSElements.TypeHole)
    private val parseIdentifierAndValue =
        Combinators.indented(
            Combinators.lexeme(lname)
                .or(stringLiteral)
        )
        .then(
            Combinators.optional(
                Combinators.indented(
                    Combinators.lexeme(
                        PSTokens.OPERATOR
                    ).or(Combinators.reserved(PSTokens.COMMA))
                )
            )
        )
        .then(Combinators.optional(Combinators.indented(expr)))
        .`as`(PSElements.ObjectBinderField)
    private val parseObjectLiteral =
        Combinators.braces(Combinators.commaSep(parseIdentifierAndValue))
            .`as`(PSElements.ObjectLiteral)
    private val typedIdent =
        Combinators.optional(Combinators.reserved(PSTokens.LPAREN))
        .then(
            Combinators.many1(
                Combinators.lexeme(idents).`as`(PSElements.GenericIdentifier)
                    .or(parseQualified(properName).`as`(PSElements.TypeConstructor))
            )
        )
        .then(
            Combinators.optional(
                Combinators.indented(dcolon).then(Combinators.indented(type))
            )
        )
        .then(Combinators.optional(parseObjectLiteral))
        .then(Combinators.optional(Combinators.reserved(PSTokens.RPAREN)))
    private val parseAbs =
        Combinators.reserved(PSTokens.BACKSLASH)
        .then(
            Combinators.choice(
                Combinators.many1(typedIdent).`as`(PSElements.Abs),
                Combinators.many1(
                    Combinators.indented(
                        ident.or(parseBinderNoParensRef).`as`(PSElements.Abs)
                    )
                )
            )
        )
        .then(Combinators.indented(Combinators.reserved(PSTokens.ARROW)))
        .then(expr)
    private val parseVar =
        Combinators.attempt(
            Combinators.manyOrEmpty(
                Combinators.attempt(
                    Combinators.token(PSTokens.PROPER_NAME)
                        .`as`(PSElements.qualifiedModuleName)
                        .then(Combinators.token(PSTokens.DOT))
                )
            )
                .then(ident).`as`(PSElements.Qualified)
        ).`as`(PSElements.Var)
    private val parseConstructor =
        parseQualified(properName).`as`(PSElements.Constructor)
    private val parseCaseAlternative =
        Combinators.commaSep1(expr.or(parseTypeWildcard))
        .then(
            Combinators.indented(
                Combinators.choice(
                    Combinators.many1(
                        parseGuard + Combinators.indented(
                            Combinators.lexeme(PSTokens.ARROW) + expr
                        )
                    ),
                    Combinators.reserved(PSTokens.ARROW).then(expr)
                )
            )
        ).`as`(PSElements.CaseAlternative)
    private val parseCase = Combinators.reserved(PSTokens.CASE)
        .then(Combinators.commaSep1(expr.or(parseTypeWildcard)))
        .then(Combinators.indented(Combinators.reserved(PSTokens.OF)))
        .then(
            Combinators.indented(
                indentedList(
                    Combinators.mark(
                        parseCaseAlternative
                    )
                )
            )
        )
        .`as`(PSElements.Case)
    private val parseIfThenElse = Combinators.reserved(PSTokens.IF)
        .then(Combinators.indented(expr))
        .then(Combinators.indented(Combinators.reserved(PSTokens.THEN)))
        .then(Combinators.indented(expr))
        .then(Combinators.indented(Combinators.reserved(PSTokens.ELSE)))
        .then(Combinators.indented(expr))
        .`as`(PSElements.IfThenElse)
    private val parseLet = Combinators.reserved(PSTokens.LET)
        .then(Combinators.indented(indentedList1(parseLocalDeclaration)))
        .then(Combinators.indented(Combinators.reserved(PSTokens.IN)))
        .then(expr)
        .`as`(PSElements.Let)
    private val letBinding =
        Combinators.choice(
            Combinators.attempt(parseTypeDeclaration),
            Combinators.optional(
                Combinators.attempt(
                    Combinators.reserved(
                        PSTokens.LPAREN
                    )
                )
            )
                .then(
                    Combinators.optional(
                        Combinators.attempt(properName)
                            .`as`(PSElements.Constructor)
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.attempt(
                            Combinators.many1(
                                ident
                            )
                        )
                    )
                )
                .then(Combinators.optional(Combinators.attempt(parseArrayBinder)))
                .then(
                    Combinators.optional(
                        Combinators.attempt(
                            Combinators.indented(Combinators.lexeme("@"))
                                .then(
                                    Combinators.indented(
                                        Combinators.braces(
                                            Combinators.commaSep(
                                                Combinators.lexeme(idents)
                                            )
                                        )
                                    )
                                )
                        )
                    ).`as`(PSElements.NamedBinder)
                )
                .then(
                    Combinators.optional(
                        Combinators.attempt(
                            parsePatternMatchObject
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.attempt(
                            parseRowPatternBinder
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.attempt(
                            Combinators.reserved(
                                PSTokens.RPAREN
                            )
                        )
                    )
                )
                .then(
                    Combinators.attempt(
                        Combinators.manyOrEmpty(
                            parseBinderNoParensRef
                        )
                    )
                )
                .then(
                    Combinators.choice(
                        Combinators.attempt(
                            Combinators.indented(
                                Combinators.many1(
                                    parseGuard + Combinators.indented(
                                        eq + exprWhere
                                    )
                                )
                            )
                        ),
                        Combinators.attempt(Combinators.indented(eq + (exprWhere)))
                    )
                ).`as`(PSElements.ValueDeclaration)
        )
    private val parseDoNotationBind: Parsec =
        parseBinderRef
        .then(Combinators.indented(Combinators.reserved(PSTokens.LARROW)).then(expr))
        .`as`(PSElements.DoNotationBind)
    private val doExpr = expr.`as`(PSElements.DoNotationValue)
    private val doStatement =
        Combinators.choice(
            Combinators.reserved(PSTokens.LET)
                .then(Combinators.indented(indentedList1(letBinding)))
                .`as`(PSElements.DoNotationLet),
            Combinators.attempt(parseDoNotationBind),
            Combinators.attempt(doExpr)
        )
    private val doBlock =
        Combinators.reserved(PSTokens.DO)
        .then(Combinators.indented(indentedList(Combinators.mark(doStatement))))
    private val parsePropertyUpdate =
        Combinators.reserved(lname.or(stringLiteral))
        .then(Combinators.optional(Combinators.indented(eq)))
        .then(Combinators.indented(expr))
    private val parseValueAtom = Combinators.choice(
        Combinators.attempt(parseTypeHole),
        Combinators.attempt(parseNumericLiteral),
        Combinators.attempt(parseStringLiteral),
        Combinators.attempt(parseBooleanLiteral),
        Combinators.attempt(
            Combinators.reserved(PSTokens.TICK) +
                properName.`as`(PSElements.ProperName)
                    .or(
                        Combinators.many1(
                            Combinators.lexeme(idents)
                                .`as`(PSElements.ProperName)
                        )
                    ) +
                Combinators.reserved(PSTokens.TICK)
        ),
        parseArrayLiteral,
        parseCharLiteral,
        Combinators.attempt(
            Combinators.indented(
                Combinators.braces(
                    Combinators.commaSep1(
                        Combinators.indented(parsePropertyUpdate)
                    )
                )
            )
        ),
        Combinators.attempt(parseObjectLiteral),
        parseAbs,
        Combinators.attempt(parseConstructor),
        Combinators.attempt(parseVar),
        parseCase,
        parseIfThenElse,
        doBlock,
        parseLet,
        Combinators.parens(expr).`as`(PSElements.Parens)
    )
    private val parseAccessor: Parsec =
        Combinators.attempt(
            Combinators.indented(Combinators.token(PSTokens.DOT))
                .then(Combinators.indented(lname.or(stringLiteral)))
        ).`as`(PSElements.Accessor)
    private val parseIdentInfix: Parsec =
        Combinators.choice(
            (Combinators.reserved(PSTokens.TICK) + parseQualified(
                Combinators.lexeme(
                    idents
                )
            )).lexeme(PSTokens.TICK),
            parseQualified(Combinators.lexeme(operator))
        ).`as`(PSElements.IdentInfix)
    private val indexersAndAccessors =
        parseValueAtom +
            Combinators.manyOrEmpty(
                Combinators.choice(
                    parseAccessor,
                    Combinators.attempt(
                        Combinators.indented(
                            Combinators.braces(
                                Combinators.commaSep1(
                                    Combinators.indented(parsePropertyUpdate)
                                )
                            )
                        )
                    ),
                    Combinators.indented(dcolon + type)
                )
            )
    private val parseValuePostFix =
        indexersAndAccessors +
            Combinators.manyOrEmpty(
                Combinators.indented(indexersAndAccessors)
                    .or(Combinators.attempt(Combinators.indented(dcolon) + type))
            )
    private val parsePrefixRef = Combinators.ref()
    private val parsePrefix =
        Combinators.choice(
            parseValuePostFix,
            Combinators.indented(Combinators.lexeme("-"))
                .then(parsePrefixRef)
                .`as`(PSElements.UnaryMinus)
        ).`as`(PSElements.PrefixValue)

    // Binder
    private val parseIdentifierAndBinder =
        Combinators.lexeme(lname.or(stringLiteral))
            .then(Combinators.indented(eq.or(Combinators.lexeme(PSTokens.OPERATOR))))
            .then(Combinators.indented(parseBinderRef))
    private val parseObjectBinder =
        Combinators.braces(Combinators.commaSep(parseIdentifierAndBinder))
            .`as`(PSElements.ObjectBinder)
    private val parseNullBinder = Combinators.reserved("_")
        .`as`(PSElements.NullBinder)
    private val parseStringBinder =
        Combinators.lexeme(PSTokens.STRING).`as`(PSElements.StringBinder)
    private val parseBooleanBinder =
        Combinators.lexeme("true")
            .or(Combinators.lexeme("false")).`as`(PSElements.BooleanBinder)
    private val parseNumberBinder =
        Combinators.optional(
            Combinators.lexeme("+").or(Combinators.lexeme("-"))
        )
            .then(
                Combinators.lexeme(PSTokens.NATURAL)
                    .or(Combinators.lexeme(PSTokens.FLOAT)))
            .`as`(PSElements.NumberBinder)
    private val parseNamedBinder =
        ident
            .then(
                Combinators.indented(Combinators.lexeme("@"))
                    .then(Combinators.indented(parseBinderRef)))
            .`as`(PSElements.NamedBinder)
    private val parseVarBinder = ident.`as`(PSElements.VarBinder)
    private val parseConstructorBinder =
        Combinators.lexeme(
            parseQualified(properName).`as`(PSElements.GenericIdentifier)
                .then(
                    Combinators.manyOrEmpty(
                        Combinators.indented(
                            parseBinderNoParensRef
                        )
                    )
                )
        ).`as`(PSElements.ConstructorBinder)
    private val parseNullaryConstructorBinder =
        Combinators.lexeme(parseQualified(properName.`as`(PSElements.ProperName)))
            .`as`(PSElements.ConstructorBinder)
    private val parsePatternMatch =
        Combinators.indented(
            Combinators.braces(
                Combinators.commaSep(
                    Combinators.lexeme(
                        idents
                    )
                )
            )
        ).`as`(PSElements.Binder)
    private val parseCharBinder =
        Combinators.lexeme("'").`as`(PSElements.StringBinder)
    private val parseBinderAtom = Combinators.choice(
        Combinators.attempt(parseNullBinder),
        Combinators.attempt(parseStringBinder),
        Combinators.attempt(parseBooleanBinder),
        Combinators.attempt(parseNumberBinder),
        Combinators.attempt(parseNamedBinder),
        Combinators.attempt(parseVarBinder),
        Combinators.attempt(parseConstructorBinder),
        Combinators.attempt(parseObjectBinder),
        Combinators.attempt(parseArrayBinder),
        Combinators.attempt(parsePatternMatch),
        Combinators.attempt(parseCharBinder),
        Combinators.attempt(Combinators.parens(parseBinderRef))
    ).`as`(PSElements.BinderAtom)
    private val parseBinder =
        parseBinderAtom
            .then(
                Combinators.optional(
                    Combinators.lexeme(PSTokens.OPERATOR).then(parseBinderRef)
                )
            )
            .`as`(PSElements.Binder)
    private val parseBinderNoParens = Combinators.choice(
        Combinators.attempt(parseNullBinder),
        Combinators.attempt(parseStringBinder),
        Combinators.attempt(parseBooleanBinder),
        Combinators.attempt(parseNumberBinder),
        Combinators.attempt(parseNamedBinder),
        Combinators.attempt(parseVarBinder),
        Combinators.attempt(parseNullaryConstructorBinder),
        Combinators.attempt(parseObjectBinder),
        Combinators.attempt(parseArrayBinder),
        Combinators.attempt(parsePatternMatch),
        Combinators.attempt(parseCharBinder),
        Combinators.attempt(Combinators.parens(parseBinderRef))
    ).`as`(PSElements.Binder)

    private val type0 = Combinators.ref()
    private val type1 = Combinators.ref()
    private val type2 = Combinators.ref()
    private val type3 = Combinators.ref()
    private val type4 = Combinators.ref()
    private val type5 = Combinators.ref()
    private val arrow = Combinators.reserved(PSTokens.ARROW)
    private val darrow = Combinators.reserved(PSTokens.DARROW)
    private val qualOp = Combinators.choice(
        operator,
        Combinators.lexeme("<="),
        Combinators.lexeme("-"),
        Combinators.lexeme("#"),
        Combinators.lexeme(":"),
    )

    init {
        type0.setRef(type1 + Combinators.optional(dcolon + type0))
        type1.setRef(type2.or(forlall + Combinators.many1(typeVarBinding) + dot + type1))
        type2.setRef(type3 + Combinators.optional(arrow.or(darrow) + type1))
        type3.setRef(type4 + Combinators.optional(qualOp + type4))
        type4.setRef(type5.or(Combinators.lexeme("#") + type4))
        type5.setRef(Combinators.many1(typeAtom))
        parseKindPrefixRef.setRef(parseKindPrefix)
        parseKind.setRef(
            (parseKindPrefix +
                Combinators.optional(
                    arrow
                        .or(
                            Combinators.optional(
                                parseQualified(properName).`as`(
                                    PSElements.TypeConstructor
                                )
                            )
                        ) +
                        Combinators.optional(parseKind)
                )).`as`(PSElements.FunKind)
        )
        type.setRef(
            Combinators.many1(
                typeAtom.or(Combinators.lexeme(PSTokens.STRING)) + Combinators.optional(
                    dcolon + parseKind
                )
            )
                .then(
                    Combinators.optional(
                        Combinators.choice(
                            Combinators.reserved(PSTokens.ARROW),
                            Combinators.reserved(PSTokens.DARROW),
                            Combinators.reserved(PSTokens.OPTIMISTIC),
                            Combinators.reserved(PSTokens.OPERATOR)
                        ).then(type)
                    )
                ).`as`(PSElements.Type)
        )
        parseForAllRef.setRef(parseForAll)
        parseLocalDeclarationRef.setRef(parseLocalDeclaration)
        parsePrefixRef.setRef(parsePrefix)
        expr.setRef(
            (
                parsePrefix + Combinators.optional(
                    Combinators.attempt(
                        Combinators.indented(parseIdentInfix)
                    ) + expr
                )
                ).`as`(PSElements.Value)
        )
        parseBinderRef.setRef(parseBinder)
        parseBinderNoParensRef.setRef(parseBinderNoParens)
    }
}