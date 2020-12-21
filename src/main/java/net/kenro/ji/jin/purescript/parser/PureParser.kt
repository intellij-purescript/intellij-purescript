package net.kenro.ji.jin.purescript.parser

import com.intellij.lang.*
import com.intellij.psi.tree.IElementType
import net.kenro.ji.jin.purescript.psi.PSTokens
import net.kenro.ji.jin.purescript.psi.PSElements

class PureParser : PsiParser, PSTokens, PSElements {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        // builder.setDebugMode(true);
        val context = ParserContext(builder)
        val mark = context.start()
        context.whiteSpace()
        // Creating a new instance here allows hot swapping while debugging.
        val info = PureParsecParser().program.parse(context)
        var nextType: IElementType? = null
        if (!context.eof()) {
            var errorMarker: PsiBuilder.Marker? = null
            while (!context.eof()) {
                if (context.position >= info.position && errorMarker == null) {
                    errorMarker = context.start()
                    nextType = builder.tokenType
                }
                context.advance()
            }
            if (errorMarker != null) {
                if (nextType != null) errorMarker.error("Unexpected $nextType. $info") else errorMarker.error(
                    info.toString()
                )
            }
        }
        mark.done(root)
        return builder.treeBuilt
    }

    class PureParsecParser {
        private fun parseQualified(p: Parsec): Parsec {
            return Combinators.attempt(
                Combinators.many(
                    Combinators.attempt(
                        Combinators.token(
                            PSTokens.PROPER_NAME
                        ).`as`(PSElements.ProperName).then(
                            Combinators.token(
                                PSTokens.DOT
                            )
                        )
                    )
                ).then(p).`as`(PSElements.Qualified)
            )
        }

        private val idents = Combinators.choice(
            Combinators.token(PSTokens.IDENT), Combinators.choice(
                Combinators.token(
                    PSTokens.FORALL
                ), Combinators.token(PSTokens.QUALIFIED), Combinators.token(
                    PSTokens.HIDING
                ), Combinators.token(PSTokens.AS)
            ).`as`(PSElements.Identifier)
        )
        private val identifier = idents
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
        private val operator = Combinators.choice(
            Combinators.token(PSTokens.OPERATOR), Combinators.token(
                PSTokens.DOT
            ), Combinators.token(PSTokens.DDOT), Combinators.token(
                PSTokens.LARROW
            ), Combinators.token(PSTokens.LDARROW), Combinators.token(
                PSTokens.OPTIMISTIC
            )
        )
        private val properName: Parsec =
            Combinators.lexeme(PSTokens.PROPER_NAME).`as`(
                PSElements.ProperName
            )
        private val moduleName = Combinators.lexeme(
            parseQualified(
                Combinators.token(
                    PSTokens.PROPER_NAME
                )
            )
        )
        private val stringLiteral = Combinators.attempt(
            Combinators.lexeme(
                PSTokens.STRING
            )
        )

        private fun positioned(p: Parsec): Parsec {
            return p
        }

        private fun indentedList(p: Parsec): Parsec {
            return Combinators.mark(
                Combinators.many(
                    Combinators.untilSame(
                        Combinators.same(p)
                    )
                )
            )
        }

        private fun indentedList1(p: Parsec): Parsec {
            return Combinators.mark(
                Combinators.many1(
                    Combinators.untilSame(
                        Combinators.same(p)
                    )
                )
            )
        }

        // Kinds.hs
        private val parseKindRef = Combinators.ref()
        private val parseKindPrefixRef = Combinators.ref()
        private val parseStar = Combinators.keyword(PSTokens.START, "*").`as`(
            PSElements.Star
        )
        private val parseBang = Combinators.keyword(PSTokens.BANG, "!").`as`(
            PSElements.Bang
        )
        private val parseKindAtom = Combinators.indented(
            Combinators.choice(
                parseStar, parseBang, parseQualified(properName).`as`(
                    PSElements.TypeConstructor
                ), Combinators.parens(parseKindRef)
            )
        )
        private val parseKindPrefix = Combinators.choice(
            Combinators.lexeme("#").then(parseKindPrefixRef)
                .`as`(PSElements.RowKind),
            parseKindAtom
        )
        private val parseKind = parseKindPrefix.then(
            Combinators.optional(
                Combinators.reserved(
                    PSTokens.ARROW
                ).or(
                    Combinators.optional(
                        parseQualified(properName).`as`(
                            PSElements.TypeConstructor
                        )
                    )
                ).then(Combinators.optional(parseKindRef))
            )
        ).`as`(
            PSElements.FunKind
        )

        // Types.hs
        private val parsePolyTypeRef = Combinators.ref()
        private val parseTypeRef = Combinators.ref()
        private val parseForAllRef = Combinators.ref()
        private val parseTypeWildcard = Combinators.reserved("_")
        private val parseFunction = Combinators.parens(
            Combinators.reserved(
                PSTokens.ARROW
            )
        )
        private val parseTypeVariable: Parsec = Combinators.lexeme(
            Combinators.guard(
                idents,
                { content:String? -> !(content == "∀" || content == "forall") },
                "not `forall`"
            )
        ).`as`(PSElements.GenericIdentifier)
        private val parseTypeConstructor: Parsec =
            parseQualified(properName).`as`(
                PSElements.TypeConstructor
            )

        private fun parseNameAndType(p: Parsec): Parsec {
            return Combinators.indented(
                Combinators.lexeme(
                    Combinators.choice(lname, stringLiteral).`as`(
                        PSElements.GenericIdentifier
                    )
                )
            ).then(
                Combinators.indented(
                    Combinators.lexeme(
                        PSTokens.DCOLON
                    )
                )
            ).then(p)
        }

        private val parseRowEnding = Combinators.optional(
            Combinators.indented(Combinators.lexeme(PSTokens.PIPE)).then(
                Combinators.indented(
                    Combinators.choice(
                        Combinators.attempt(parseTypeWildcard),
                        Combinators.attempt(
                            Combinators.optional(
                                Combinators.lexeme(
                                    Combinators.many(properName).`as`(
                                        PSElements.TypeConstructor
                                    )
                                )
                            )
                                .then(
                                    Combinators.optional(
                                        Combinators.lexeme(identifier).`as`(
                                            PSElements.GenericIdentifier
                                        )
                                    )
                                )
                                .then(
                                    Combinators.optional(
                                        Combinators.indented(
                                            Combinators.lexeme(
                                                Combinators.choice(
                                                    lname,
                                                    stringLiteral
                                                )
                                            )
                                        )
                                    )
                                )
                                .then(
                                    Combinators.optional(
                                        Combinators.indented(
                                            Combinators.lexeme(
                                                PSTokens.DCOLON
                                            )
                                        )
                                    ).then(
                                        Combinators.optional(
                                            parsePolyTypeRef
                                        )
                                    )
                                )
                                .`as`(PSElements.TypeVar)
                        )
                    )
                )
            )
        )
        private val parseRow: Parsec =
            Combinators.commaSep(parseNameAndType(parsePolyTypeRef))
                .then(parseRowEnding)
                .`as`(PSElements.Row)
        private val parseObject: Parsec = Combinators.braces(parseRow).`as`(
            PSElements.ObjectType
        )
        private val parseTypeAtom: Parsec = Combinators.indented(
            Combinators.choice(
                Combinators.attempt(
                    Combinators.squares(
                        Combinators.optional(
                            parseTypeRef
                        )
                    )
                ),
                Combinators.attempt(parseFunction),
                Combinators.attempt(parseObject),
                Combinators.attempt(parseTypeWildcard),
                Combinators.attempt(parseTypeVariable),
                Combinators.attempt(parseTypeConstructor),
                Combinators.attempt(parseForAllRef),
                Combinators.attempt(Combinators.parens(parseRow)),
                Combinators.attempt(Combinators.parens(parsePolyTypeRef))
            )
        ).`as`(PSElements.TypeAtom)
        private val parseConstrainedType: Parsec = Combinators.optional(
            Combinators.attempt(
                Combinators.parens(
                    Combinators.commaSep1(
                        parseQualified(properName).`as`(
                            PSElements.TypeConstructor
                        ).then(
                            Combinators.indented(
                                Combinators.many(parseTypeAtom)
                            )
                        )
                    )
                )
                    .then(Combinators.lexeme(PSTokens.DARROW))
            )
        ).then(Combinators.indented(parseTypeRef))
            .`as`(PSElements.ConstrainedType)
        private val parseForAll = Combinators.reserved(PSTokens.FORALL)
            .then(
                Combinators.many1(
                    Combinators.indented(
                        Combinators.lexeme(identifier).`as`(
                            PSElements.GenericIdentifier
                        )
                    )
                )
            )
            .then(Combinators.indented(Combinators.lexeme(PSTokens.DOT)))
            .then(parseConstrainedType).`as`(PSElements.ForAll)
        private val parseIdent = Combinators.choice(
            Combinators.lexeme(identifier.`as`(PSElements.Identifier)),
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
        private val parseTypePostfix = Combinators.choice(
            parseTypeAtom, Combinators.lexeme(
                PSTokens.STRING
            )
        )
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        Combinators.indented(
                            Combinators.lexeme(
                                PSTokens.DCOLON
                            ).then(parseKind)
                        )
                    )
                )
            )
        private val parseType = Combinators.many1(parseTypePostfix)
            .then(
                Combinators.optional(
                    Combinators.choice(
                        Combinators.reserved(PSTokens.ARROW),
                        Combinators.reserved(
                            PSTokens.DARROW
                        ),
                        Combinators.reserved(PSTokens.OPTIMISTIC),
                        Combinators.reserved(
                            PSTokens.OPERATOR
                        )
                    ).then(parseTypeRef)
                )
            ).`as`(PSElements.Type)

        // Declarations.hs
        private val kindedIdent = Combinators.lexeme(identifier).`as`(
            PSElements.GenericIdentifier
        )
            .or(
                Combinators.parens(
                    Combinators.lexeme(identifier)
                        .`as`(PSElements.GenericIdentifier).then(
                        Combinators.indented(
                            Combinators.lexeme(
                                PSTokens.DCOLON
                            )
                        )
                    ).then(Combinators.indented(parseKindRef))
                )
            )
        private val parseBinderNoParensRef = Combinators.ref()
        private val parseBinderRef = Combinators.ref()
        private val parseValueRef = Combinators.ref()
        private val parseLocalDeclarationRef = Combinators.ref()
        private val parseGuard = Combinators.lexeme(PSTokens.PIPE)
            .then(Combinators.indented(Combinators.commaSep(parseValueRef)))
            .`as`(
                PSElements.Guard
            )
        private val parseDataDeclaration = Combinators.reserved(PSTokens.DATA)
            .then(
                Combinators.indented(properName)
                    .`as`(PSElements.TypeConstructor)
            )
            .then(
                Combinators.many(Combinators.indented(kindedIdent)).`as`(
                    PSElements.TypeArgs
                )
            )
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        Combinators.lexeme(
                            PSTokens.EQ
                        )
                    )
                        .then(
                            Combinators.sepBy1(
                                properName.`as`(PSElements.TypeConstructor)
                                    .then(
                                        Combinators.many(
                                            Combinators.indented(parseTypeAtom)
                                        )
                                    ), PSTokens.PIPE
                            )
                        )
                )
            )
            .`as`(PSElements.DataDeclaration)
        private val parseTypeDeclaration = Combinators.attempt(
            parseIdent.`as`(
                PSElements.TypeAnnotationName
            ).then(
                Combinators.indented(
                    Combinators.lexeme(
                        PSTokens.DCOLON
                    )
                )
            )
        )
            .then(Combinators.attempt(parsePolyTypeRef))
            .`as`(PSElements.TypeDeclaration)
        private val parseNewtypeDeclaration =
            Combinators.reserved(PSTokens.NEWTYPE)
                .then(
                    Combinators.indented(properName)
                        .`as`(PSElements.TypeConstructor)
                )
                .then(
                    Combinators.many(Combinators.indented(kindedIdent)).`as`(
                        PSElements.TypeArgs
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.lexeme(PSTokens.EQ)
                            .then(
                                properName.`as`(PSElements.TypeConstructor)
                                    .then(
                                        Combinators.optional(
                                            Combinators.many(
                                                Combinators.indented(
                                                    Combinators.lexeme(
                                                        identifier
                                                    )
                                                )
                                            )
                                        )
                                    )
                                    .then(
                                        Combinators.optional(
                                            Combinators.indented(
                                                parseTypeAtom
                                            )
                                        )
                                    )
                            )
                    )
                )
                .`as`(PSElements.NewtypeDeclaration)
        private val parseTypeSynonymDeclaration = Combinators.reserved(
            PSTokens.TYPE
        )
            .then(
                Combinators.reserved(PSTokens.PROPER_NAME)
                    .`as`(PSElements.TypeConstructor)
            )
            .then(
                Combinators.many(
                    Combinators.indented(
                        Combinators.lexeme(
                            kindedIdent
                        )
                    )
                )
            )
            .then(
                Combinators.indented(Combinators.lexeme(PSTokens.EQ))
                    .then(parsePolyTypeRef)
            )
            .`as`(PSElements.TypeSynonymDeclaration)
        private val parseValueWithWhereClause = parseValueRef
            .then(
                Combinators.optional(
                    Combinators.indented(Combinators.lexeme(PSTokens.WHERE))
                        .then(
                            Combinators.indented(
                                Combinators.mark(
                                    Combinators.many1(
                                        Combinators.same(
                                            parseLocalDeclarationRef
                                        )
                                    )
                                )
                            )
                        )
                )
            )

        // Some Binders - rest at the bottom
        private val parseArrayBinder =
            Combinators.squares(Combinators.commaSep(parseBinderRef)).`as`(
                PSElements.ObjectBinder
            )
        private val parsePatternMatchObject = Combinators.indented(
            Combinators.braces(
                Combinators.commaSep(
                    Combinators.lexeme(identifier).or(lname).or(stringLiteral)
                        .then(
                            Combinators.optional(
                                Combinators.indented(
                                    Combinators.lexeme(
                                        PSTokens.EQ
                                    ).or(Combinators.lexeme(PSTokens.OPERATOR))
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
        private val parseRowPatternBinder = Combinators.indented(
            Combinators.lexeme(
                PSTokens.OPERATOR
            )
        )
            .then(Combinators.indented(parseBinderRef))
        private val parseValueDeclaration // this is for when used with LET
                = Combinators.optional(
            Combinators.attempt(
                Combinators.reserved(PSTokens.LPAREN)
            )
        )
            .then(
                Combinators.optional(
                    Combinators.attempt(properName).`as`(
                        PSElements.Constructor
                    )
                )
            )
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        Combinators.many1(
                            parseIdent
                        )
                    )
                )
            )
            .then(Combinators.optional(Combinators.attempt(parseArrayBinder)))
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        Combinators.indented(
                            Combinators.lexeme("@")
                        ).then(
                            Combinators.indented(
                                Combinators.braces(
                                    Combinators.commaSep(
                                        Combinators.lexeme(
                                            identifier
                                        )
                                    )
                                )
                            )
                        )
                    )
                ).`as`(
                    PSElements.NamedBinder
                )
            )
            .then(
                Combinators.optional(
                    Combinators.attempt(
                        parsePatternMatchObject
                    )
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
            ) // ---------- end of LET stuff -----------
            .then(Combinators.attempt(Combinators.many(parseBinderNoParensRef)))
            .then(
                Combinators.choice(
                    Combinators.attempt(
                        Combinators.indented(
                            Combinators.many1(
                                parseGuard.then(
                                    Combinators.indented(
                                        Combinators.lexeme(
                                            PSTokens.EQ
                                        ).then(parseValueWithWhereClause)
                                    )
                                )
                            )
                        )
                    ),
                    Combinators.attempt(
                        Combinators.indented(
                            Combinators.lexeme(
                                PSTokens.EQ
                            ).then(parseValueWithWhereClause)
                        )
                    )
                )
            ).`as`(
                PSElements.ValueDeclaration
            )
        private val parseDeps = Combinators.parens(
            Combinators.commaSep1(
                parseQualified(properName).`as`(
                    PSElements.TypeConstructor
                ).then(Combinators.many(parseTypeAtom))
            )
        )
            .then(Combinators.indented(Combinators.reserved(PSTokens.DARROW)))
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
                                            .`as`(
                                                PSElements.TypeConstructor
                                            )
                                    )
                                )
                                .then(Combinators.lexeme(PSTokens.DCOLON))
                                .then(parseKind)
                                .`as`(PSElements.ExternDataDeclaration),
                            Combinators.reserved(PSTokens.INSTANCE)
                                .then(parseIdent)
                                .then(
                                    Combinators.indented(
                                        Combinators.lexeme(
                                            PSTokens.DCOLON
                                        )
                                    )
                                )
                                .then(Combinators.optional(parseDeps))
                                .then(parseQualified(properName).`as`(PSElements.pClassName))
                                .then(
                                    Combinators.many(
                                        Combinators.indented(
                                            parseTypeAtom
                                        )
                                    )
                                )
                                .`as`(PSElements.ExternInstanceDeclaration),
                            Combinators.attempt(parseIdent)
                                .then(
                                    Combinators.optional(
                                        stringLiteral.`as`(
                                            PSElements.JSRaw
                                        )
                                    )
                                )
                                .then(
                                    Combinators.indented(
                                        Combinators.lexeme(
                                            PSTokens.DCOLON
                                        )
                                    )
                                )
                                .then(parsePolyTypeRef)
                                .`as`(PSElements.ExternDeclaration)
                        )
                    )
                )
        private val parseAssociativity = Combinators.choice(
            Combinators.reserved(PSTokens.INFIXL),
            Combinators.reserved(PSTokens.INFIXR),
            Combinators.reserved(PSTokens.INFIX)
        )
        private val parseFixity = parseAssociativity.then(
            Combinators.indented(
                Combinators.lexeme(
                    PSTokens.NATURAL
                )
            )
        ).`as`(PSElements.Fixity)
        private val parseFixityDeclaration = parseFixity
            .then(Combinators.optional(Combinators.reserved(PSTokens.TYPE)))
            .then(
                parseQualified(properName).`as`(PSElements.pModuleName).or(
                    parseIdent.`as`(
                        PSElements.ProperName
                    )
                )
            )
            .then(Combinators.reserved(PSTokens.AS))
            .then(Combinators.lexeme(operator))
            .`as`(PSElements.FixityDeclaration)
        private val parseDeclarationRef = Combinators.choice(
            Combinators.reserved("kind").then(
                parseQualified(properName).`as`(
                    PSElements.pClassName
                )
            ),
            parseIdent.`as`(PSElements.ValueRef),
            Combinators.reserved(PSTokens.TYPE)
                .then(Combinators.optional(Combinators.parens(operator))),
            Combinators.reserved(PSTokens.MODULE).then(moduleName)
                .`as`(PSElements.importModuleName),
            Combinators.reserved(PSTokens.CLASS).then(
                parseQualified(properName).`as`(
                    PSElements.pClassName
                )
            ),
            properName.`as`(PSElements.ProperName).then(
                Combinators.optional(
                    Combinators.parens(
                        Combinators.optional(
                            Combinators.choice(
                                Combinators.reserved(PSTokens.DDOT),
                                Combinators.commaSep1(properName.`as`(PSElements.TypeConstructor))
                            )
                        )
                    )
                )
            )
        ).`as`(
            PSElements.PositionedDeclarationRef
        )
        private val parseTypeClassDeclaration =
            Combinators.lexeme(PSTokens.CLASS)
                .then(
                    Combinators.optional(
                        Combinators.indented(
                            Combinators.choice(
                                Combinators.parens(
                                    Combinators.commaSep1(
                                        parseQualified(properName).`as`(
                                            PSElements.TypeConstructor
                                        ).then(Combinators.many(parseTypeAtom))
                                    )
                                ),
                                Combinators.commaSep1(
                                    parseQualified(properName).`as`(
                                        PSElements.TypeConstructor
                                    ).then(Combinators.many(parseTypeAtom))
                                )
                            )
                        ).then(
                            Combinators.optional(Combinators.reserved(PSTokens.LDARROW))
                                .`as`(
                                    PSElements.pImplies
                                )
                        )
                    )
                )
                .then(
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
                        Combinators.many(
                            Combinators.indented(
                                kindedIdent
                            )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.lexeme(PSTokens.PIPE).then(
                            Combinators.indented(
                                Combinators.commaSep1(parsePolyTypeRef)
                            )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.attempt(
                            Combinators.indented(Combinators.reserved(PSTokens.WHERE))
                                .then(
                                    indentedList(positioned(parseTypeDeclaration))
                                )
                        )
                    )
                )
                .`as`(PSElements.TypeClassDeclaration)
        private val parseTypeInstanceDeclaration = Combinators.optional(
            Combinators.reserved(
                PSTokens.DERIVE
            )
        ).then(
            Combinators.optional(
                Combinators.reserved(
                    PSTokens.NEWTYPE
                )
            )
        ).then(
            Combinators.reserved(PSTokens.INSTANCE)
                .then(
                    parseIdent.`as`(PSElements.GenericIdentifier).then(
                        Combinators.indented(
                            Combinators.lexeme(
                                PSTokens.DCOLON
                            )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.optional(Combinators.reserved(PSTokens.LPAREN))
                            .then(
                                Combinators.commaSep1(
                                    parseQualified(properName).`as`(
                                        PSElements.TypeConstructor
                                    ).then(Combinators.many(parseTypeAtom))
                                )
                            ).then(
                            Combinators.optional(
                                Combinators.reserved(
                                    PSTokens.RPAREN
                                )
                            )
                        )
                            .then(
                                Combinators.optional(
                                    Combinators.indented(
                                        Combinators.reserved(
                                            PSTokens.DARROW
                                        )
                                    )
                                )
                            )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.indented(parseQualified(properName)).`as`(
                            PSElements.pClassName
                        )
                    )
                )
                .then(
                    Combinators.many(
                        Combinators.indented(parseTypeAtom).or(
                            Combinators.lexeme(
                                PSTokens.STRING
                            )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.indented(
                            Combinators.reserved(
                                PSTokens.DARROW
                            )
                        ).then(
                            Combinators.optional(
                                Combinators.reserved(
                                    PSTokens.LPAREN
                                )
                            )
                        )
                            .then(parseQualified(properName).`as`(PSElements.TypeConstructor))
                            .then(Combinators.many(parseTypeAtom)).then(
                            Combinators.optional(
                                Combinators.reserved(
                                    PSTokens.RPAREN
                                )
                            )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.attempt(
                            Combinators.indented(Combinators.reserved(PSTokens.WHERE))
                                .then(
                                    Combinators.indented(
                                        indentedList(
                                            positioned(parseValueDeclaration)
                                        )
                                    )
                                )
                        )
                    )
                )
        )
            .`as`(PSElements.TypeInstanceDeclaration)
        private val importDeclarationType = Combinators.optional(
            Combinators.indented(
                Combinators.parens(Combinators.commaSep(parseDeclarationRef))
            )
        )
        private val parseImportDeclaration =
            Combinators.reserved(PSTokens.IMPORT)
                .then(
                    Combinators.indented(moduleName)
                        .`as`(PSElements.importModuleName)
                )
                .then(
                    Combinators.optional(Combinators.reserved(PSTokens.HIDING))
                        .then(importDeclarationType)
                )
                .then(
                    Combinators.optional(
                        Combinators.reserved(PSTokens.AS).then(moduleName).`as`(
                            PSElements.importModuleName
                        )
                    )
                )
                .`as`(PSElements.ImportDeclaration)
        private val parseDeclaration = positioned(
            Combinators.choice(
                parseDataDeclaration,
                parseNewtypeDeclaration,
                parseTypeDeclaration,
                parseTypeSynonymDeclaration,
                parseValueDeclaration,
                parseExternDeclaration,
                parseFixityDeclaration,
                parseImportDeclaration,
                parseTypeClassDeclaration,
                parseTypeInstanceDeclaration
            )
        )
        private val parseLocalDeclaration = positioned(
            Combinators.choice(
                parseTypeDeclaration,
                parseValueDeclaration
            )
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
            .then(indentedList(parseDeclaration))
            .`as`(PSElements.Module)
        val program: Parsec = indentedList(parseModule).`as`(PSElements.Program)

        // Literals
        private val parseBooleanLiteral = Combinators.reserved(PSTokens.TRUE)
            .or(Combinators.reserved(PSTokens.FALSE)).`as`(
            PSElements.BooleanLiteral
        )
        private val parseNumericLiteral =
            Combinators.reserved(PSTokens.NATURAL).or(
                Combinators.reserved(
                    PSTokens.FLOAT
                )
            ).`as`(PSElements.NumericLiteral)
        private val parseStringLiteral =
            Combinators.reserved(PSTokens.STRING).`as`(
                PSElements.StringLiteral
            )
        private val parseCharLiteral =
            Combinators.lexeme("'").`as`(PSElements.StringLiteral)
        private val parseArrayLiteral =
            Combinators.squares(Combinators.commaSep(parseValueRef)).`as`(
                PSElements.ArrayLiteral
            )
        private val parseTypeHole =
            Combinators.lexeme("?").`as`(PSElements.TypeHole)
        private val parseIdentifierAndValue =
            Combinators.indented(Combinators.lexeme(lname).or(stringLiteral))
                .then(
                    Combinators.optional(
                        Combinators.indented(
                            Combinators.lexeme(
                                PSTokens.OPERATOR
                            ).or(Combinators.reserved(PSTokens.COMMA))
                        )
                    )
                )
                .then(Combinators.optional(Combinators.indented(parseValueRef)))
                .`as`(PSElements.ObjectBinderField)
        private val parseObjectLiteral =
            Combinators.braces(Combinators.commaSep(parseIdentifierAndValue))
                .`as`(
                    PSElements.ObjectLiteral
                )
        private val typedIdent = Combinators.optional(
            Combinators.reserved(
                PSTokens.LPAREN
            )
        )
            .then(
                Combinators.many1(
                    Combinators.lexeme(identifier)
                        .`as`(PSElements.GenericIdentifier).or(
                        parseQualified(properName).`as`(
                            PSElements.TypeConstructor
                        )
                    )
                )
            )
            .then(
                Combinators.optional(
                    Combinators.indented(
                        Combinators.lexeme(
                            PSTokens.DCOLON
                        )
                    ).then(Combinators.indented(parsePolyTypeRef))
                )
            )
            .then(Combinators.optional(parseObjectLiteral))
            .then(Combinators.optional(Combinators.reserved(PSTokens.RPAREN)))
        private val parseAbs = Combinators.reserved(PSTokens.BACKSLASH)
            .then(
                Combinators.choice(
                    Combinators.many1(typedIdent).`as`(PSElements.Abs),
                    Combinators.many1(
                        Combinators.indented(
                            parseIdent.or(parseBinderNoParensRef).`as`(
                                PSElements.Abs
                            )
                        )
                    )
                )
            )
            .then(Combinators.indented(Combinators.reserved(PSTokens.ARROW)))
            .then(parseValueRef)
        private val parseVar = Combinators.attempt(
            Combinators.many(
                Combinators.attempt(
                    Combinators.token(
                        PSTokens.PROPER_NAME
                    ).`as`(PSElements.qualifiedModuleName).then(
                        Combinators.token(
                            PSTokens.DOT
                        )
                    )
                )
            ).then(parseIdent).`as`(PSElements.Qualified)
        ).`as`(
            PSElements.Var
        )
        private val parseConstructor = parseQualified(properName).`as`(
            PSElements.Constructor
        )
        private val parseCaseAlternative = Combinators.commaSep1(
            parseValueRef.or(
                parseTypeWildcard
            )
        )
            .then(
                Combinators.indented(
                    Combinators.choice(
                        Combinators.many1(
                            parseGuard.then(
                                Combinators.indented(
                                    Combinators.lexeme(
                                        PSTokens.ARROW
                                    ).then(parseValueRef)
                                )
                            )
                        ),
                        Combinators.reserved(PSTokens.ARROW).then(parseValueRef)
                    )
                )
            )
            .`as`(PSElements.CaseAlternative)
        private val parseCase = Combinators.reserved(PSTokens.CASE)
            .then(Combinators.commaSep1(parseValueRef.or(parseTypeWildcard)))
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
            .then(Combinators.indented(parseValueRef))
            .then(Combinators.indented(Combinators.reserved(PSTokens.THEN)))
            .then(Combinators.indented(parseValueRef))
            .then(Combinators.indented(Combinators.reserved(PSTokens.ELSE)))
            .then(Combinators.indented(parseValueRef))
            .`as`(PSElements.IfThenElse)
        private val parseLet = Combinators.reserved(PSTokens.LET)
            .then(Combinators.indented(indentedList1(parseLocalDeclaration)))
            .then(Combinators.indented(Combinators.reserved(PSTokens.IN)))
            .then(parseValueRef)
            .`as`(PSElements.Let)
        private val parseDoNotationLet: Parsec =
            Combinators.reserved(PSTokens.LET)
                .then(Combinators.indented(indentedList1(parseLocalDeclaration)))
                .`as`(PSElements.DoNotationLet)
        private val parseDoNotationBind: Parsec = parseBinderRef
            .then(
                Combinators.indented(Combinators.reserved(PSTokens.LARROW))
                    .then(parseValueRef)
            )
            .`as`(PSElements.DoNotationBind)
        private val parseDoNotationElement = Combinators.choice(
            Combinators.attempt(parseDoNotationBind),
            parseDoNotationLet,
            Combinators.attempt(parseValueRef.`as`(PSElements.DoNotationValue))
        )
        private val parseDo = Combinators.reserved(PSTokens.DO)
            .then(
                Combinators.indented(
                    indentedList(
                        Combinators.mark(
                            parseDoNotationElement
                        )
                    )
                )
            )
        private val parsePropertyUpdate =
            Combinators.reserved(lname.or(stringLiteral))
                .then(
                    Combinators.optional(
                        Combinators.indented(
                            Combinators.lexeme(
                                PSTokens.EQ
                            )
                        )
                    )
                )
                .then(Combinators.indented(parseValueRef))
        private val parseValueAtom = Combinators.choice(
            Combinators.attempt(parseTypeHole),
            Combinators.attempt(parseNumericLiteral),
            Combinators.attempt(parseStringLiteral),
            Combinators.attempt(parseBooleanLiteral),
            Combinators.attempt(
                Combinators.reserved(PSTokens.TICK).then(
                    Combinators.choice(
                        properName.`as`(
                            PSElements.ProperName
                        ),
                        Combinators.many1(
                            Combinators.lexeme(identifier)
                                .`as`(PSElements.ProperName)
                        )
                    )
                ).then(
                    Combinators.reserved(
                        PSTokens.TICK
                    )
                )
            ),
            parseArrayLiteral,
            parseCharLiteral,
            Combinators.attempt(
                Combinators.indented(
                    Combinators.braces(
                        Combinators.commaSep1(
                            Combinators.indented(
                                parsePropertyUpdate
                            )
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
            parseDo,
            parseLet,
            Combinators.parens(parseValueRef).`as`(PSElements.Parens)
        )
        private val parseAccessor: Parsec = Combinators.attempt(
            Combinators.indented(
                Combinators.token(
                    PSTokens.DOT
                )
            ).then(Combinators.indented(lname.or(stringLiteral)))
        ).`as`(
            PSElements.Accessor
        )
        private val parseIdentInfix: Parsec = Combinators.choice(
            Combinators.reserved(PSTokens.TICK)
                .then(parseQualified(Combinators.lexeme(identifier))).lexeme(
                PSTokens.TICK
            ),
            parseQualified(Combinators.lexeme(operator))
        ).`as`(PSElements.IdentInfix)
        private val indexersAndAccessors = parseValueAtom
            .then(
                Combinators.many(
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
                        Combinators.indented(
                            Combinators.reserved(PSTokens.DCOLON)
                                .then(parseType)
                        )
                    )
                )
            )
        private val parseValuePostFix = indexersAndAccessors
            .then(
                Combinators.many(
                    Combinators.choice(
                        Combinators.indented(indexersAndAccessors),
                        Combinators.attempt(
                            Combinators.indented(
                                Combinators.lexeme(
                                    PSTokens.DCOLON
                                )
                            ).then(parsePolyTypeRef)
                        )
                    )
                )
            )
        private val parsePrefixRef = Combinators.ref()
        private val parsePrefix = Combinators.choice(
            parseValuePostFix,
            Combinators.indented(Combinators.lexeme("-")).then(parsePrefixRef)
                .`as`(
                    PSElements.UnaryMinus
                )
        ).`as`(PSElements.PrefixValue)
        private val parseValue = parsePrefix
            .then(
                Combinators.optional(
                    Combinators.attempt(Combinators.indented(parseIdentInfix))
                        .then(parseValueRef)
                )
            )
            .`as`(PSElements.Value)

        // Binder
        private val parseIdentifierAndBinder =
            Combinators.lexeme(lname.or(stringLiteral))
                .then(
                    Combinators.indented(
                        Combinators.lexeme(PSTokens.EQ).or(
                            Combinators.lexeme(
                                PSTokens.OPERATOR
                            )
                        )
                    )
                )
                .then(Combinators.indented(parseBinderRef))
        private val parseObjectBinder =
            Combinators.braces(Combinators.commaSep(parseIdentifierAndBinder))
                .`as`(
                    PSElements.ObjectBinder
                )
        private val parseNullBinder = Combinators.reserved("_").`as`(
            PSElements.NullBinder
        )
        private val parseStringBinder =
            Combinators.lexeme(PSTokens.STRING).`as`(
                PSElements.StringBinder
            )
        private val parseBooleanBinder =
            Combinators.lexeme("true").or(Combinators.lexeme("false")).`as`(
                PSElements.BooleanBinder
            )
        private val parseNumberBinder = Combinators.optional(
            Combinators.choice(
                Combinators.lexeme("+"),
                Combinators.lexeme("-")
            )
        )
            .then(
                Combinators.lexeme(PSTokens.NATURAL).or(
                    Combinators.lexeme(
                        PSTokens.FLOAT
                    )
                )
            ).`as`(PSElements.NumberBinder)
        private val parseNamedBinder = parseIdent.then(
            Combinators.indented(Combinators.lexeme("@"))
                .then(Combinators.indented(parseBinderRef))
        ).`as`(
            PSElements.NamedBinder
        )
        private val parseVarBinder = parseIdent.`as`(PSElements.VarBinder)
        private val parseConstructorBinder = Combinators.lexeme(
            parseQualified(properName).`as`(
                PSElements.GenericIdentifier
            ).then(
                Combinators.many(
                    Combinators.indented(
                        parseBinderNoParensRef
                    )
                )
            )
        ).`as`(
            PSElements.ConstructorBinder
        )
        private val parseNullaryConstructorBinder = Combinators.lexeme(
            parseQualified(
                properName.`as`(
                    PSElements.ProperName
                )
            )
        ).`as`(PSElements.ConstructorBinder)
        private val parsePatternMatch = Combinators.indented(
            Combinators.braces(
                Combinators.commaSep(Combinators.lexeme(identifier))
            )
        ).`as`(
            PSElements.Binder
        )
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
        private val parseBinder = parseBinderAtom
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

        init {
            parseKindPrefixRef.setRef(parseKindPrefix)
            parseKindRef.setRef(parseKind)
        }

        init {
            parsePolyTypeRef.setRef(parseType)
            parseTypeRef.setRef(parseType)
            parseForAllRef.setRef(parseForAll)
        }

        init {
            parseLocalDeclarationRef.setRef(parseLocalDeclaration)
        }

        init {
            parsePrefixRef.setRef(parsePrefix)
        }

        init {
            parseValueRef.setRef(parseValue)
            parseBinderRef.setRef(parseBinder)
            parseBinderNoParensRef.setRef(parseBinderNoParens)
        }
    }
}