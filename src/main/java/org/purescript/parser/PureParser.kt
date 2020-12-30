package org.purescript.parser

import com.intellij.lang.*
import com.intellij.psi.tree.IElementType
import org.purescript.parser.Combinators.indented
import org.purescript.parser.Combinators.many
import org.purescript.parser.Combinators.reserved
import org.purescript.psi.PSTokens
import org.purescript.psi.PSElements

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
                many(
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
                many(
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
        private val parseKindAtom = indented(
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
                reserved(
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
        private val parseTypeWildcard = reserved("_")
        private val parseFunction = Combinators.parens(
            reserved(
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
            return indented(
                Combinators.lexeme(
                    Combinators.choice(lname, stringLiteral).`as`(
                        PSElements.GenericIdentifier
                    )
                )
            ).then(
                indented(
                    Combinators.lexeme(
                        PSTokens.DCOLON
                    )
                )
            ).then(p)
        }

        private val parseRowEnding = Combinators.optional(
            indented(Combinators.lexeme(PSTokens.PIPE)).then(
                indented(
                    Combinators.choice(
                        Combinators.attempt(parseTypeWildcard),
                        Combinators.attempt(
                            Combinators.optional(
                                Combinators.lexeme(
                                    many(properName).`as`(
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
                                        indented(
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
                                        indented(
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
        private val parseTypeAtom: Parsec = indented(
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
                            indented(
                                many(parseTypeAtom)
                            )
                        )
                    )
                )
                    .then(Combinators.lexeme(PSTokens.DARROW))
            )
        ).then(indented(parseTypeRef))
            .`as`(PSElements.ConstrainedType)
        private val parseForAll = reserved(PSTokens.FORALL)
            .then(
                Combinators.many1(
                    indented(
                        Combinators.lexeme(identifier).`as`(
                            PSElements.GenericIdentifier
                        )
                    )
                )
            )
            .then(indented(Combinators.lexeme(PSTokens.DOT)))
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
                        indented(
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
                        reserved(PSTokens.ARROW),
                        reserved(
                            PSTokens.DARROW
                        ),
                        reserved(PSTokens.OPTIMISTIC),
                        reserved(
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
                        indented(
                            Combinators.lexeme(
                                PSTokens.DCOLON
                            )
                        )
                    ).then(indented(parseKindRef))
                )
            )
        private val parseBinderNoParensRef = Combinators.ref()
        private val parseBinderRef = Combinators.ref()
        private val parseValueRef = Combinators.ref()
        private val parseLocalDeclarationRef = Combinators.ref()
        private val parseGuard = Combinators.lexeme(PSTokens.PIPE)
            .then(indented(Combinators.commaSep(parseValueRef)))
            .`as`(
                PSElements.Guard
            )
        private val parseDataDeclaration = reserved(PSTokens.DATA)
            .then(indented(properName).`as`(PSElements.TypeConstructor))
            .then(many(indented(kindedIdent)).`as`(PSElements.TypeArgs))
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
                                        many(
                                            indented(parseTypeAtom)
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
                indented(
                    Combinators.lexeme(
                        PSTokens.DCOLON
                    )
                )
            )
        )
            .then(Combinators.attempt(parsePolyTypeRef))
            .`as`(PSElements.TypeDeclaration)
        private val parseNewtypeDeclaration =
            reserved(PSTokens.NEWTYPE)
                .then(
                    indented(properName)
                        .`as`(PSElements.TypeConstructor)
                )
                .then(
                    many(indented(kindedIdent)).`as`(
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
                                            many(
                                                indented(
                                                    Combinators.lexeme(
                                                        identifier
                                                    )
                                                )
                                            )
                                        )
                                    )
                                    .then(
                                        Combinators.optional(
                                            indented(
                                                parseTypeAtom
                                            )
                                        )
                                    )
                            )
                    )
                )
                .`as`(PSElements.NewtypeDeclaration)
        private val parseTypeSynonymDeclaration = reserved(
            PSTokens.TYPE
        )
            .then(
                reserved(PSTokens.PROPER_NAME)
                    .`as`(PSElements.TypeConstructor)
            )
            .then(
                many(
                    indented(
                        Combinators.lexeme(
                            kindedIdent
                        )
                    )
                )
            )
            .then(
                indented(Combinators.lexeme(PSTokens.EQ))
                    .then(parsePolyTypeRef)
            )
            .`as`(PSElements.TypeSynonymDeclaration)
        private val parseValueWithWhereClause = parseValueRef
            .then(
                Combinators.optional(
                    indented(Combinators.lexeme(PSTokens.WHERE))
                        .then(
                            indented(
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
        private val parsePatternMatchObject = indented(
            Combinators.braces(
                Combinators.commaSep(
                    Combinators.lexeme(identifier).or(lname).or(stringLiteral)
                        .then(
                            Combinators.optional(
                                indented(
                                    Combinators.lexeme(
                                        PSTokens.EQ
                                    ).or(Combinators.lexeme(PSTokens.OPERATOR))
                                )
                            )
                        )
                        .then(
                            Combinators.optional(
                                indented(
                                    parseBinderRef
                                )
                            )
                        )
                )
            )
        ).`as`(PSElements.Binder)
        private val parseRowPatternBinder = indented(
            Combinators.lexeme(
                PSTokens.OPERATOR
            )
        )
            .then(indented(parseBinderRef))
        private val parseValueDeclaration // this is for when used with LET
                = Combinators.optional(
            Combinators.attempt(
                reserved(PSTokens.LPAREN)
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
                        indented(
                            Combinators.lexeme("@")
                        ).then(
                            indented(
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
                        reserved(
                            PSTokens.RPAREN
                        )
                    )
                )
            ) // ---------- end of LET stuff -----------
            .then(Combinators.attempt(many(parseBinderNoParensRef)))
            .then(
                Combinators.choice(
                    Combinators.attempt(
                        indented(
                            Combinators.many1(
                                parseGuard.then(
                                    indented(
                                        Combinators.lexeme(
                                            PSTokens.EQ
                                        ).then(parseValueWithWhereClause)
                                    )
                                )
                            )
                        )
                    ),
                    Combinators.attempt(
                        indented(
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
                ).then(many(parseTypeAtom))
            )
        )
            .then(indented(reserved(PSTokens.DARROW)))
        private val parseExternDeclaration =
            reserved(PSTokens.FOREIGN)
                .then(indented(reserved(PSTokens.IMPORT)))
                .then(
                    indented(
                        Combinators.choice(
                            reserved(PSTokens.DATA)
                                .then(
                                    indented(
                                        reserved(PSTokens.PROPER_NAME)
                                            .`as`(
                                                PSElements.TypeConstructor
                                            )
                                    )
                                )
                                .then(Combinators.lexeme(PSTokens.DCOLON))
                                .then(parseKind)
                                .`as`(PSElements.ExternDataDeclaration),
                            reserved(PSTokens.INSTANCE)
                                .then(parseIdent)
                                .then(
                                    indented(
                                        Combinators.lexeme(
                                            PSTokens.DCOLON
                                        )
                                    )
                                )
                                .then(Combinators.optional(parseDeps))
                                .then(parseQualified(properName).`as`(PSElements.pClassName))
                                .then(
                                    many(
                                        indented(
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
                                    indented(
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
            reserved(PSTokens.INFIXL),
            reserved(PSTokens.INFIXR),
            reserved(PSTokens.INFIX)
        )
        private val parseFixity = parseAssociativity.then(
            indented(
                Combinators.lexeme(
                    PSTokens.NATURAL
                )
            )
        ).`as`(PSElements.Fixity)
        private val parseFixityDeclaration = parseFixity
            .then(Combinators.optional(reserved(PSTokens.TYPE)))
            .then(
                parseQualified(properName).`as`(PSElements.pModuleName).or(
                    parseIdent.`as`(
                        PSElements.ProperName
                    )
                )
            )
            .then(reserved(PSTokens.AS))
            .then(Combinators.lexeme(operator))
            .`as`(PSElements.FixityDeclaration)
        private val parseDeclarationRef = Combinators.choice(
            reserved("kind").then(
                parseQualified(properName).`as`(
                    PSElements.pClassName
                )
            ),
            parseIdent.`as`(PSElements.ValueRef),
            reserved(PSTokens.TYPE)
                .then(Combinators.optional(Combinators.parens(operator))),
            reserved(PSTokens.MODULE).then(moduleName)
                .`as`(PSElements.importModuleName),
            reserved(PSTokens.CLASS).then(
                parseQualified(properName).`as`(
                    PSElements.pClassName
                )
            ),
            properName.`as`(PSElements.ProperName).then(
                Combinators.optional(
                    Combinators.parens(
                        Combinators.optional(
                            Combinators.choice(
                                reserved(PSTokens.DDOT),
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
                        indented(
                            Combinators.choice(
                                Combinators.parens(
                                    Combinators.commaSep1(
                                        parseQualified(properName).`as`(
                                            PSElements.TypeConstructor
                                        ).then(many(parseTypeAtom))
                                    )
                                ),
                                Combinators.commaSep1(
                                    parseQualified(properName).`as`(
                                        PSElements.TypeConstructor
                                    ).then(many(parseTypeAtom))
                                )
                            )
                        ).then(
                            Combinators.optional(reserved(PSTokens.LDARROW))
                                .`as`(
                                    PSElements.pImplies
                                )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        indented(
                            properName.`as`(
                                PSElements.pClassName
                            )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        many(
                            indented(
                                kindedIdent
                            )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.lexeme(PSTokens.PIPE).then(
                            indented(
                                Combinators.commaSep1(parsePolyTypeRef)
                            )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.attempt(
                            indented(reserved(PSTokens.WHERE))
                                .then(
                                    indentedList(positioned(parseTypeDeclaration))
                                )
                        )
                    )
                )
                .`as`(PSElements.TypeClassDeclaration)
        private val parseTypeInstanceDeclaration = Combinators.optional(
            reserved(
                PSTokens.DERIVE
            )
        ).then(
            Combinators.optional(
                reserved(
                    PSTokens.NEWTYPE
                )
            )
        ).then(
            reserved(PSTokens.INSTANCE)
                .then(
                    parseIdent.`as`(PSElements.GenericIdentifier).then(
                        indented(
                            Combinators.lexeme(
                                PSTokens.DCOLON
                            )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.optional(reserved(PSTokens.LPAREN))
                            .then(
                                Combinators.commaSep1(
                                    parseQualified(properName).`as`(
                                        PSElements.TypeConstructor
                                    ).then(many(parseTypeAtom))
                                )
                            ).then(
                            Combinators.optional(
                                reserved(
                                    PSTokens.RPAREN
                                )
                            )
                        )
                            .then(
                                Combinators.optional(
                                    indented(
                                        reserved(
                                            PSTokens.DARROW
                                        )
                                    )
                                )
                            )
                    )
                )
                .then(
                    Combinators.optional(
                        indented(parseQualified(properName)).`as`(
                            PSElements.pClassName
                        )
                    )
                )
                .then(
                    many(
                        indented(parseTypeAtom).or(
                            Combinators.lexeme(
                                PSTokens.STRING
                            )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        indented(
                            reserved(
                                PSTokens.DARROW
                            )
                        ).then(
                            Combinators.optional(
                                reserved(
                                    PSTokens.LPAREN
                                )
                            )
                        )
                            .then(parseQualified(properName).`as`(PSElements.TypeConstructor))
                            .then(many(parseTypeAtom)).then(
                            Combinators.optional(
                                reserved(
                                    PSTokens.RPAREN
                                )
                            )
                        )
                    )
                )
                .then(
                    Combinators.optional(
                        Combinators.attempt(
                            indented(reserved(PSTokens.WHERE))
                                .then(
                                    indented(
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
            indented(
                Combinators.parens(Combinators.commaSep(parseDeclarationRef))
            )
        )
        private val parseImportDeclaration =
            reserved(PSTokens.IMPORT)
                .then(
                    indented(moduleName)
                        .`as`(PSElements.importModuleName)
                )
                .then(
                    Combinators.optional(reserved(PSTokens.HIDING))
                        .then(importDeclarationType)
                )
                .then(
                    Combinators.optional(
                        reserved(PSTokens.AS).then(moduleName).`as`(
                            PSElements.importModuleName
                        )
                    )
                )
                .`as`(PSElements.ImportDeclaration)
        private val parseDecl = positioned(
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
        private val parseModule = reserved(PSTokens.MODULE)
            .then(indented(moduleName).`as`(PSElements.pModuleName))
            .then(
                Combinators.optional(
                    Combinators.parens(
                        Combinators.commaSep1(
                            parseDeclarationRef
                        )
                    )
                )
            )
            .then(reserved(PSTokens.WHERE))
            .then(indentedList(parseDecl))
            .`as`(PSElements.Module)
        val program: Parsec = indentedList(parseModule).`as`(PSElements.Program)

        // Literals
        private val parseBooleanLiteral = reserved(PSTokens.TRUE)
            .or(reserved(PSTokens.FALSE)).`as`(
            PSElements.BooleanLiteral
        )
        private val parseNumericLiteral =
            reserved(PSTokens.NATURAL).or(
                reserved(
                    PSTokens.FLOAT
                )
            ).`as`(PSElements.NumericLiteral)
        private val parseStringLiteral =
            reserved(PSTokens.STRING).`as`(
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
            indented(Combinators.lexeme(lname).or(stringLiteral))
                .then(
                    Combinators.optional(
                        indented(
                            Combinators.lexeme(
                                PSTokens.OPERATOR
                            ).or(reserved(PSTokens.COMMA))
                        )
                    )
                )
                .then(Combinators.optional(indented(parseValueRef)))
                .`as`(PSElements.ObjectBinderField)
        private val parseObjectLiteral =
            Combinators.braces(Combinators.commaSep(parseIdentifierAndValue))
                .`as`(
                    PSElements.ObjectLiteral
                )
        private val typedIdent = Combinators.optional(
            reserved(
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
                    indented(
                        Combinators.lexeme(
                            PSTokens.DCOLON
                        )
                    ).then(indented(parsePolyTypeRef))
                )
            )
            .then(Combinators.optional(parseObjectLiteral))
            .then(Combinators.optional(reserved(PSTokens.RPAREN)))
        private val parseAbs = reserved(PSTokens.BACKSLASH)
            .then(
                Combinators.choice(
                    Combinators.many1(typedIdent).`as`(PSElements.Abs),
                    Combinators.many1(
                        indented(
                            parseIdent.or(parseBinderNoParensRef).`as`(
                                PSElements.Abs
                            )
                        )
                    )
                )
            )
            .then(indented(reserved(PSTokens.ARROW)))
            .then(parseValueRef)
        private val parseVar = Combinators.attempt(
            many(
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
                indented(
                    Combinators.choice(
                        Combinators.many1(
                            parseGuard.then(
                                indented(
                                    Combinators.lexeme(
                                        PSTokens.ARROW
                                    ).then(parseValueRef)
                                )
                            )
                        ),
                        reserved(PSTokens.ARROW).then(parseValueRef)
                    )
                )
            )
            .`as`(PSElements.CaseAlternative)
        private val parseCase = reserved(PSTokens.CASE)
            .then(Combinators.commaSep1(parseValueRef.or(parseTypeWildcard)))
            .then(indented(reserved(PSTokens.OF)))
            .then(
                indented(
                    indentedList(
                        Combinators.mark(
                            parseCaseAlternative
                        )
                    )
                )
            )
            .`as`(PSElements.Case)
        private val parseIfThenElse = reserved(PSTokens.IF)
            .then(indented(parseValueRef))
            .then(indented(reserved(PSTokens.THEN)))
            .then(indented(parseValueRef))
            .then(indented(reserved(PSTokens.ELSE)))
            .then(indented(parseValueRef))
            .`as`(PSElements.IfThenElse)
        private val parseLet = reserved(PSTokens.LET)
            .then(indented(indentedList1(parseLocalDeclaration)))
            .then(indented(reserved(PSTokens.IN)))
            .then(parseValueRef)
            .`as`(PSElements.Let)
        private val parseDoNotationLet: Parsec =
            reserved(PSTokens.LET)
                .then(indented(indentedList1(parseLocalDeclaration)))
                .`as`(PSElements.DoNotationLet)
        private val parseDoNotationBind: Parsec = parseBinderRef
            .then(
                indented(reserved(PSTokens.LARROW))
                    .then(parseValueRef)
            )
            .`as`(PSElements.DoNotationBind)
        private val parseDoNotationElement = Combinators.choice(
            Combinators.attempt(parseDoNotationBind),
            parseDoNotationLet,
            Combinators.attempt(parseValueRef.`as`(PSElements.DoNotationValue))
        )
        private val parseDo = reserved(PSTokens.DO)
            .then(
                indented(
                    indentedList(
                        Combinators.mark(
                            parseDoNotationElement
                        )
                    )
                )
            )
        private val parsePropertyUpdate =
            reserved(lname.or(stringLiteral))
                .then(
                    Combinators.optional(
                        indented(
                            Combinators.lexeme(
                                PSTokens.EQ
                            )
                        )
                    )
                )
                .then(indented(parseValueRef))
        private val parseValueAtom = Combinators.choice(
            Combinators.attempt(parseTypeHole),
            Combinators.attempt(parseNumericLiteral),
            Combinators.attempt(parseStringLiteral),
            Combinators.attempt(parseBooleanLiteral),
            Combinators.attempt(
                reserved(PSTokens.TICK).then(
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
                    reserved(
                        PSTokens.TICK
                    )
                )
            ),
            parseArrayLiteral,
            parseCharLiteral,
            Combinators.attempt(
                indented(
                    Combinators.braces(
                        Combinators.commaSep1(
                            indented(
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
            indented(
                Combinators.token(
                    PSTokens.DOT
                )
            ).then(indented(lname.or(stringLiteral)))
        ).`as`(
            PSElements.Accessor
        )
        private val parseIdentInfix: Parsec = Combinators.choice(
            reserved(PSTokens.TICK)
                .then(parseQualified(Combinators.lexeme(identifier))).lexeme(
                PSTokens.TICK
            ),
            parseQualified(Combinators.lexeme(operator))
        ).`as`(PSElements.IdentInfix)
        private val indexersAndAccessors = parseValueAtom
            .then(
                many(
                    Combinators.choice(
                        parseAccessor,
                        Combinators.attempt(
                            indented(
                                Combinators.braces(
                                    Combinators.commaSep1(
                                        indented(parsePropertyUpdate)
                                    )
                                )
                            )
                        ),
                        indented(
                            reserved(PSTokens.DCOLON)
                                .then(parseType)
                        )
                    )
                )
            )
        private val parseValuePostFix = indexersAndAccessors
            .then(
                many(
                    Combinators.choice(
                        indented(indexersAndAccessors),
                        Combinators.attempt(
                            indented(
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
            indented(Combinators.lexeme("-")).then(parsePrefixRef)
                .`as`(
                    PSElements.UnaryMinus
                )
        ).`as`(PSElements.PrefixValue)
        private val parseValue = parsePrefix
            .then(
                Combinators.optional(
                    Combinators.attempt(indented(parseIdentInfix))
                        .then(parseValueRef)
                )
            )
            .`as`(PSElements.Value)

        // Binder
        private val parseIdentifierAndBinder =
            Combinators.lexeme(lname.or(stringLiteral))
                .then(
                    indented(
                        Combinators.lexeme(PSTokens.EQ).or(
                            Combinators.lexeme(
                                PSTokens.OPERATOR
                            )
                        )
                    )
                )
                .then(indented(parseBinderRef))
        private val parseObjectBinder =
            Combinators.braces(Combinators.commaSep(parseIdentifierAndBinder))
                .`as`(
                    PSElements.ObjectBinder
                )
        private val parseNullBinder = reserved("_").`as`(
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
            indented(Combinators.lexeme("@"))
                .then(indented(parseBinderRef))
        ).`as`(
            PSElements.NamedBinder
        )
        private val parseVarBinder = parseIdent.`as`(PSElements.VarBinder)
        private val parseConstructorBinder = Combinators.lexeme(
            parseQualified(properName).`as`(
                PSElements.GenericIdentifier
            ).then(
                many(
                    indented(
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
        private val parsePatternMatch = indented(
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