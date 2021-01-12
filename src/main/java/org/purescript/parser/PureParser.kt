package org.purescript.parser

import com.intellij.lang.*
import com.intellij.psi.tree.IElementType
import org.purescript.parser.Combinators.attempt
import org.purescript.parser.Combinators.braces
import org.purescript.parser.Combinators.choice
import org.purescript.parser.Combinators.commaSep
import org.purescript.parser.Combinators.commaSep1
import org.purescript.parser.Combinators.guard
import org.purescript.parser.Combinators.indented
import org.purescript.parser.Combinators.keyword
import org.purescript.parser.Combinators.lexeme
import org.purescript.parser.Combinators.many1
import org.purescript.parser.Combinators.manyOrEmpty
import org.purescript.parser.Combinators.mark
import org.purescript.parser.Combinators.optional
import org.purescript.parser.Combinators.parens
import org.purescript.parser.Combinators.reserved
import org.purescript.parser.Combinators.same
import org.purescript.parser.Combinators.sepBy1
import org.purescript.parser.Combinators.squares
import org.purescript.parser.Combinators.token
import org.purescript.parser.Combinators.untilSame
import org.purescript.psi.PSTokens
import org.purescript.psi.PSElements
import org.purescript.psi.PSElements.Companion.Bang
import org.purescript.psi.PSElements.Companion.BooleanBinder
import org.purescript.psi.PSElements.Companion.ConstrainedType
import org.purescript.psi.PSElements.Companion.FunKind
import org.purescript.psi.PSElements.Companion.NewtypeDeclaration
import org.purescript.psi.PSElements.Companion.NullBinder
import org.purescript.psi.PSElements.Companion.ObjectBinder
import org.purescript.psi.PSElements.Companion.ObjectType
import org.purescript.psi.PSElements.Companion.ProperName
import org.purescript.psi.PSElements.Companion.Qualified
import org.purescript.psi.PSElements.Companion.RowKind
import org.purescript.psi.PSElements.Companion.Star
import org.purescript.psi.PSElements.Companion.StringBinder
import org.purescript.psi.PSElements.Companion.TypeAnnotationName
import org.purescript.psi.PSElements.Companion.TypeArgs
import org.purescript.psi.PSElements.Companion.TypeConstructor
import org.purescript.psi.PSElements.Companion.TypeDeclaration
import org.purescript.psi.PSElements.Companion.TypeSynonymDeclaration
import org.purescript.psi.PSTokens.Companion.ARROW
import org.purescript.psi.PSTokens.Companion.DARROW
import org.purescript.psi.PSTokens.Companion.DOT
import org.purescript.psi.PSTokens.Companion.NEWTYPE
import org.purescript.psi.PSTokens.Companion.PIPE
import org.purescript.psi.PSTokens.Companion.PROPER_NAME
import org.purescript.psi.PSTokens.Companion.STRING
import org.purescript.psi.PSTokens.Companion.TICK

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
        private fun parseQualified(p: Parsec): Parsec =
            attempt(
                manyOrEmpty(
                    attempt(token(PROPER_NAME).`as`(ProperName) + token(DOT))
                ) + p
            ).`as`(Qualified)

        // tokens
        private val dcolon = lexeme(PSTokens.DCOLON)
        private val eq = lexeme(PSTokens.EQ)
        private val where = lexeme(PSTokens.WHERE)

        private val idents =
            choice(
                token(PSTokens.IDENT),
                token(PSTokens.AS),
                token(PSTokens.HIDING),
                token(PSTokens.FORALL),
                token(PSTokens.QUALIFIED),
            )
        private val identifier = idents
        private val lname = lexeme(
            choice(
                token(PSTokens.IDENT),
                token(PSTokens.DATA),
                token(NEWTYPE),
                token(PSTokens.TYPE),
                token(PSTokens.FOREIGN),
                token(PSTokens.IMPORT),
                token(PSTokens.INFIXL),
                token(PSTokens.INFIXR),
                token(PSTokens.INFIX),
                token(PSTokens.CLASS),
                token(PSTokens.DERIVE),
                token(PSTokens.INSTANCE),
                token(PSTokens.MODULE),
                token(PSTokens.CASE),
                token(PSTokens.OF),
                token(PSTokens.IF),
                token(PSTokens.THEN),
                token(PSTokens.ELSE),
                token(PSTokens.DO),
                token(PSTokens.LET),
                token(PSTokens.TRUE),
                token(PSTokens.FALSE),
                token(PSTokens.IN),
                token(PSTokens.WHERE),
                token(PSTokens.FORALL),
                token(PSTokens.QUALIFIED),
                token(PSTokens.HIDING),
                token(PSTokens.AS)
            ).`as`(PSElements.Identifier)
        )
        private val operator =
            choice(
                token(PSTokens.OPERATOR),
                token(DOT),
                token(PSTokens.DDOT),
                token(PSTokens.LARROW),
                token(PSTokens.LDARROW),
                token(PSTokens.OPTIMISTIC)
            )
        private val properName: Parsec = lexeme(PROPER_NAME).`as`(ProperName)
        private val moduleName = lexeme(parseQualified(token(PROPER_NAME)))
        private val stringLiteral = attempt(lexeme(STRING))
        private fun indentedList(p: Parsec): Parsec =
            mark(manyOrEmpty(untilSame(same(p))))

        private fun indentedList1(p: Parsec): Parsec =
            mark(many1(untilSame(same(p))))

        // Kinds.hs
        private val parseKind = Combinators.ref()
        private val parseKindPrefixRef = Combinators.ref()
        private val parseStar = keyword(PSTokens.START, "*").`as`(Star)
        private val parseBang = keyword(PSTokens.BANG, "!").`as`(Bang)
        private val parseKindAtom = indented(
            choice(
                parseStar, parseBang,
                parseQualified(properName).`as`(TypeConstructor),
                parens(parseKind)
            )
        )
        private val parseKindPrefix =
            choice(
                (lexeme("#") + parseKindPrefixRef).`as`(RowKind),
                parseKindAtom
            )

        // Types.hs
        private val type = Combinators.ref()
        private val parseForAllRef = Combinators.ref()
        private val parseTypeWildcard = reserved("_")
        private val parseFunction = parens(reserved(ARROW))
        private val parseTypeVariable: Parsec = lexeme(
            guard(
                idents,
                { content: String? -> !(content == "∀" || content == "forall") },
                "not `forall`"
            )
        ).`as`(PSElements.GenericIdentifier)
        private val parseTypeConstructor: Parsec =
            parseQualified(properName).`as`(TypeConstructor)

        private fun parseNameAndType(p: Parsec): Parsec =
            indented(lexeme(
                choice(lname, stringLiteral).`as`(PSElements.GenericIdentifier)
            )) + indented(lexeme(PSTokens.DCOLON)) + p

        private val parseRowEnding = optional(
            indented(lexeme(PIPE)).then(
                indented(
                    choice(
                        attempt(parseTypeWildcard),
                        attempt(
                            optional(
                                lexeme(
                                    manyOrEmpty(properName).`as`(
                                        TypeConstructor
                                    )
                                )
                            )
                                .then(
                                    optional(
                                        lexeme(identifier).`as`(
                                            PSElements.GenericIdentifier
                                        )
                                    )
                                )
                                .then(
                                    optional(
                                        indented(
                                            lexeme(
                                                choice(
                                                    lname,
                                                    stringLiteral
                                                )
                                            )
                                        )
                                    )
                                )
                                .then(
                                    optional(
                                        indented(
                                            lexeme(
                                                PSTokens.DCOLON
                                            )
                                        )
                                    ).then(
                                        optional(
                                            type
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
            commaSep(parseNameAndType(type))
                .then(parseRowEnding)
                .`as`(PSElements.Row)
        private val parseObject: Parsec = braces(parseRow).`as`(ObjectType)
        private val parseTypeAtom: Parsec = indented(
            choice(
                attempt(squares(optional(type))),
                attempt(parseFunction),
                attempt(parseObject),
                attempt(parseTypeWildcard),
                attempt(parseTypeVariable),
                attempt(parseTypeConstructor),
                attempt(parseForAllRef),
                attempt(parens(parseRow)),
                attempt(parens(type))
            )
        ).`as`(PSElements.TypeAtom)
        private val parseConstrainedType: Parsec =
            optional(
                attempt(
                    parens(commaSep1(
                        parseQualified(properName).`as`(TypeConstructor) +
                            indented(manyOrEmpty(parseTypeAtom))
                    )) + lexeme(DARROW)
                )
            ).then(indented(type)).`as`(ConstrainedType)
        private val parseForAll = reserved(PSTokens.FORALL)
            .then(
                many1(
                    indented(
                        lexeme(identifier).`as`(
                            PSElements.GenericIdentifier
                        )
                    )
                )
            )
            .then(indented(lexeme(DOT)))
            .then(parseConstrainedType).`as`(PSElements.ForAll)
        private val ident =
            choice(
                lexeme(identifier.`as`(PSElements.Identifier)),
                attempt(parens(lexeme(operator.`as`(PSElements.Identifier))))
            )
        private val parseTypePostfix = choice(
            parseTypeAtom, lexeme(
                STRING
            )
        )
            .then(
                optional(
                    attempt(
                        indented(
                            lexeme(
                                PSTokens.DCOLON
                            ).then(parseKind)
                        )
                    )
                )
            )
        private val parseType = many1(parseTypePostfix)
            .then(
                optional(
                    choice(
                        reserved(ARROW),
                        reserved(
                            DARROW
                        ),
                        reserved(PSTokens.OPTIMISTIC),
                        reserved(
                            PSTokens.OPERATOR
                        )
                    ).then(type)
                )
            ).`as`(PSElements.Type)

        // Declarations.hs
        private val kindedIdent =
            lexeme(identifier).`as`(PSElements.GenericIdentifier)
            .or(parens(
                lexeme(identifier).`as`(PSElements.GenericIdentifier)
                .then(indented(dcolon))
                .then(indented(parseKind))
            ))
        private val parseBinderNoParensRef = Combinators.ref()
        private val parseBinderRef = Combinators.ref()
        private val expr = Combinators.ref()
        private val parseLocalDeclarationRef = Combinators.ref()
        private val parseGuard = lexeme(PIPE)
            .then(indented(commaSep(expr)))
            .`as`(
                PSElements.Guard
            )
        private val dataHead =
            reserved(PSTokens.DATA) +
                indented(properName).`as`(TypeConstructor) +
                manyOrEmpty(indented(kindedIdent)).`as`(TypeArgs)

        val dataCtor =
            properName.`as`(TypeConstructor) +
                manyOrEmpty(indented(parseTypeAtom))
        private val parseTypeDeclaration =
            (ident.`as`(TypeAnnotationName) + dcolon + type).`as`(TypeDeclaration)

        private val newtypeHead =
            reserved(NEWTYPE) +
            indented(properName).`as`(TypeConstructor) +
            manyOrEmpty(indented(kindedIdent))
                .`as`(TypeArgs)

        private val parseNewtypeDeclaration =
            (newtypeHead + eq + properName.`as`(TypeConstructor) + parseTypeAtom)
                .`as`(NewtypeDeclaration)
        private val parseTypeSynonymDeclaration =
            reserved(PSTokens.TYPE)
                .then(reserved(PROPER_NAME).`as`(TypeConstructor))
                .then(manyOrEmpty(indented(lexeme(kindedIdent))))
                .then(indented(eq) + (type))
            .`as`(TypeSynonymDeclaration)
        private val exprWhere =
            expr + optional(where + indentedList1(parseLocalDeclarationRef))

        // Some Binders - rest at the bottom
        private val parseArrayBinder =
            squares(commaSep(parseBinderRef)).`as`(ObjectBinder)
        private val parsePatternMatchObject = indented(
            braces(
                commaSep(
                    lexeme(identifier).or(lname).or(stringLiteral)
                        .then(
                            optional(
                                indented(
                                    eq.or(lexeme(PSTokens.OPERATOR))
                                )
                            )
                        )
                        .then(
                            optional(
                                indented(
                                    parseBinderRef
                                )
                            )
                        )
                )
            )
        ).`as`(PSElements.Binder)
        private val parseRowPatternBinder = indented(
            lexeme(
                PSTokens.OPERATOR
            )
        )
            .then(indented(parseBinderRef))
        private val guardedDeclExpr = parseGuard + eq + exprWhere
        private val guardedDecl =
            choice(
                attempt(eq) + exprWhere,
                indented(many1(guardedDeclExpr)),
            )

        private val parseValueDeclaration // this is for when used with LET
            = optional(attempt(reserved(PSTokens.LPAREN)))
            .then(optional(attempt(properName).`as`(PSElements.Constructor)))
            .then(optional(attempt(many1(ident))))
            .then(optional(attempt(parseArrayBinder)))
            .then(
                optional(
                    attempt(
                        indented(lexeme("@"))
                            .then(indented(braces(commaSep(lexeme(identifier)))))
                    )
                ).`as`(PSElements.NamedBinder)
            ).then(optional(attempt(parsePatternMatchObject)))
            .then(optional(attempt(parseRowPatternBinder)))
            .then(optional(attempt(reserved(PSTokens.RPAREN))))
            // ---------- end of LET stuff -----------
            .then(attempt(manyOrEmpty(parseBinderNoParensRef)))
            .then(guardedDecl).`as`(PSElements.ValueDeclaration)
        private val parseDeps = parens(
            commaSep1(
                parseQualified(properName).`as`(
                    TypeConstructor
                ).then(manyOrEmpty(parseTypeAtom))
            )
        )
            .then(indented(reserved(DARROW)))
        private val parseExternDeclaration =
            reserved(PSTokens.FOREIGN)
                .then(indented(reserved(PSTokens.IMPORT)))
                .then(
                    indented(
                        choice(
                            reserved(PSTokens.DATA)
                                .then(
                                    indented(
                                        reserved(PROPER_NAME)
                                            .`as`(
                                                TypeConstructor
                                            )
                                    )
                                )
                                .then(dcolon)
                                .then(parseKind)
                                .`as`(PSElements.ExternDataDeclaration),
                            reserved(PSTokens.INSTANCE)
                                .then(ident)
                                .then(
                                    indented(
                                        lexeme(
                                            PSTokens.DCOLON
                                        )
                                    )
                                )
                                .then(optional(parseDeps))
                                .then(parseQualified(properName).`as`(PSElements.pClassName))
                                .then(
                                    manyOrEmpty(
                                        indented(
                                            parseTypeAtom
                                        )
                                    )
                                )
                                .`as`(PSElements.ExternInstanceDeclaration),
                            attempt(ident)
                                .then(
                                    optional(
                                        stringLiteral.`as`(
                                            PSElements.JSRaw
                                        )
                                    )
                                )
                                .then(
                                    indented(
                                        lexeme(
                                            PSTokens.DCOLON
                                        )
                                    )
                                )
                                .then(type)
                                .`as`(PSElements.ExternDeclaration)
                        )
                    )
                )
        private val parseAssociativity = choice(
            reserved(PSTokens.INFIXL),
            reserved(PSTokens.INFIXR),
            reserved(PSTokens.INFIX)
        )
        private val parseFixity = parseAssociativity.then(
            indented(
                lexeme(
                    PSTokens.NATURAL
                )
            )
        ).`as`(PSElements.Fixity)
        private val parseFixityDeclaration = parseFixity
            .then(optional(reserved(PSTokens.TYPE)))
            .then(
                parseQualified(properName).`as`(PSElements.pModuleName).or(
                    ident.`as`(
                        ProperName
                    )
                )
            )
            .then(reserved(PSTokens.AS))
            .then(lexeme(operator))
            .`as`(PSElements.FixityDeclaration)
        private val parseDeclarationRef = choice(
            reserved("kind").then(
                parseQualified(properName).`as`(
                    PSElements.pClassName
                )
            ),
            ident.`as`(PSElements.ValueRef),
            reserved(PSTokens.TYPE)
                .then(optional(parens(operator))),
            reserved(PSTokens.MODULE).then(moduleName)
                .`as`(PSElements.importModuleName),
            reserved(PSTokens.CLASS).then(
                parseQualified(properName).`as`(
                    PSElements.pClassName
                )
            ),
            properName.`as`(ProperName).then(
                optional(
                    parens(
                        optional(
                            choice(
                                reserved(PSTokens.DDOT),
                                commaSep1(properName.`as`(TypeConstructor))
                            )
                        )
                    )
                )
            )
        ).`as`(
            PSElements.PositionedDeclarationRef
        )
        private val parseTypeClassDeclaration =
            lexeme(PSTokens.CLASS)
                .then(
                    optional(
                        indented(
                            choice(
                                parens(
                                    commaSep1(
                                        parseQualified(properName).`as`(
                                            TypeConstructor
                                        ).then(manyOrEmpty(parseTypeAtom))
                                    )
                                ),
                                commaSep1(
                                    parseQualified(properName).`as`(
                                        TypeConstructor
                                    ).then(manyOrEmpty(parseTypeAtom))
                                )
                            )
                        ).then(
                            optional(reserved(PSTokens.LDARROW))
                                .`as`(
                                    PSElements.pImplies
                                )
                        )
                    )
                )
                .then(
                    optional(
                        indented(
                            properName.`as`(
                                PSElements.pClassName
                            )
                        )
                    )
                )
                .then(
                    optional(
                        manyOrEmpty(
                            indented(
                                kindedIdent
                            )
                        )
                    )
                )
                .then(
                    optional(
                        lexeme(PIPE).then(
                            indented(
                                commaSep1(type)
                            )
                        )
                    )
                )
                .then(
                    optional(
                        attempt(
                            indented(reserved(PSTokens.WHERE))
                                .then(
                                    indentedList(parseTypeDeclaration)
                                )
                        )
                    )
                )
                .`as`(PSElements.TypeClassDeclaration)
        private val parseTypeInstanceDeclaration = optional(
            reserved(
                PSTokens.DERIVE
            )
        ).then(
            optional(
                reserved(
                    NEWTYPE
                )
            )
        ).then(
            reserved(PSTokens.INSTANCE)
                .then(
                    ident.`as`(PSElements.GenericIdentifier).then(
                        indented(
                            lexeme(
                                PSTokens.DCOLON
                            )
                        )
                    )
                )
                .then(
                    optional(
                        optional(reserved(PSTokens.LPAREN))
                            .then(
                                commaSep1(
                                    parseQualified(properName).`as`(
                                        TypeConstructor
                                    ).then(manyOrEmpty(parseTypeAtom))
                                )
                            ).then(
                                optional(
                                    reserved(
                                        PSTokens.RPAREN
                                    )
                                )
                            )
                            .then(
                                optional(
                                    indented(
                                        reserved(
                                            DARROW
                                        )
                                    )
                                )
                            )
                    )
                )
                .then(
                    optional(
                        indented(parseQualified(properName)).`as`(
                            PSElements.pClassName
                        )
                    )
                )
                .then(
                    manyOrEmpty(
                        indented(parseTypeAtom).or(
                            lexeme(
                                STRING
                            )
                        )
                    )
                )
                .then(
                    optional(
                        indented(
                            reserved(
                                DARROW
                            )
                        ).then(
                            optional(
                                reserved(
                                    PSTokens.LPAREN
                                )
                            )
                        )
                            .then(parseQualified(properName).`as`(
                                TypeConstructor
                            ))
                            .then(manyOrEmpty(parseTypeAtom)).then(
                                optional(
                                    reserved(
                                        PSTokens.RPAREN
                                    )
                                )
                            )
                    )
                )
                .then(
                    optional(
                        attempt(
                            indented(reserved(PSTokens.WHERE))
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
        )
            .`as`(PSElements.TypeInstanceDeclaration)
        private val importDeclarationType = optional(
            indented(
                parens(commaSep(parseDeclarationRef))
            )
        )
        private val parseImportDeclaration =
            reserved(PSTokens.IMPORT)
                .then(
                    indented(moduleName)
                        .`as`(PSElements.importModuleName)
                )
                .then(
                    optional(reserved(PSTokens.HIDING))
                        .then(importDeclarationType)
                )
                .then(
                    optional(
                        reserved(PSTokens.AS).then(moduleName).`as`(
                            PSElements.importModuleName
                        )
                    )
                )
                .`as`(PSElements.ImportDeclaration)
        private val decl = choice(
            (dataHead + optional(eq + sepBy1(dataCtor, PIPE)))
                .`as`(PSElements.DataDeclaration),
            (newtypeHead + eq + properName.`as`(TypeConstructor) + parseTypeAtom)
                .`as`(NewtypeDeclaration),
            attempt(parseTypeDeclaration),
            parseTypeSynonymDeclaration,
            optional(attempt(reserved(PSTokens.LPAREN)))
                .then(optional(attempt(properName).`as`(PSElements.Constructor)))
                .then(optional(attempt(many1(ident))))
                .then(optional(attempt(parseArrayBinder)))
                .then(
                    optional(
                        attempt(
                            indented(lexeme("@"))
                                .then(indented(braces(commaSep(lexeme(identifier)))))
                        )
                    ).`as`(PSElements.NamedBinder)
                ).then(optional(attempt(parsePatternMatchObject)))
                .then(optional(attempt(parseRowPatternBinder)))
                .then(optional(attempt(reserved(PSTokens.RPAREN))))
                .then(attempt(manyOrEmpty(parseBinderNoParensRef)))
                .then(guardedDecl).`as`(PSElements.ValueDeclaration),
            parseExternDeclaration,
            parseFixityDeclaration,
            parseImportDeclaration,
            parseTypeClassDeclaration,
            parseTypeInstanceDeclaration
        )
        private val parseLocalDeclaration = choice(
            attempt(parseTypeDeclaration),
            // this is for when used with LET
            optional(attempt(reserved(PSTokens.LPAREN)))
                .then(optional(attempt(properName).`as`(PSElements.Constructor)))
                .then(optional(attempt(many1(ident))))
                .then(optional(attempt(parseArrayBinder)))
                .then(
                    optional(
                        attempt(
                            indented(lexeme("@"))
                                .then(indented(braces(commaSep(lexeme(identifier)))))
                        )
                    ).`as`(PSElements.NamedBinder)
                ).then(optional(attempt(parsePatternMatchObject)))
                .then(optional(attempt(parseRowPatternBinder)))
                .then(optional(attempt(reserved(PSTokens.RPAREN))))
                // ---------- end of LET stuff -----------
                .then(attempt(manyOrEmpty(parseBinderNoParensRef)))
                .then(guardedDecl).`as`(PSElements.ValueDeclaration)
        )
        private val parseModule = reserved(PSTokens.MODULE)
            .then(indented(moduleName).`as`(PSElements.pModuleName))
            .then(optional(parens(commaSep1(parseDeclarationRef))))
            .then(reserved(PSTokens.WHERE))
            .then(indentedList(decl))
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
            reserved(STRING).`as`(
                PSElements.StringLiteral
            )
        private val parseCharLiteral =
            lexeme("'").`as`(PSElements.StringLiteral)
        private val parseArrayLiteral =
            squares(commaSep(expr)).`as`(
                PSElements.ArrayLiteral
            )
        private val parseTypeHole =
            lexeme("?").`as`(PSElements.TypeHole)
        private val parseIdentifierAndValue =
            indented(lexeme(lname).or(stringLiteral))
                .then(
                    optional(
                        indented(
                            lexeme(
                                PSTokens.OPERATOR
                            ).or(reserved(PSTokens.COMMA))
                        )
                    )
                )
                .then(optional(indented(expr)))
                .`as`(PSElements.ObjectBinderField)
        private val parseObjectLiteral =
            braces(commaSep(parseIdentifierAndValue))
                .`as`(
                    PSElements.ObjectLiteral
                )
        private val typedIdent = optional(
            reserved(
                PSTokens.LPAREN
            )
        )
            .then(
                many1(
                    lexeme(identifier)
                        .`as`(PSElements.GenericIdentifier).or(
                            parseQualified(properName).`as`(
                                TypeConstructor
                            )
                        )
                )
            )
            .then(
                optional(
                    indented(
                        lexeme(
                            PSTokens.DCOLON
                        )
                    ).then(indented(type))
                )
            )
            .then(optional(parseObjectLiteral))
            .then(optional(reserved(PSTokens.RPAREN)))
        private val parseAbs = reserved(PSTokens.BACKSLASH)
            .then(
                choice(
                    many1(typedIdent).`as`(PSElements.Abs),
                    many1(
                        indented(
                            ident.or(parseBinderNoParensRef).`as`(
                                PSElements.Abs
                            )
                        )
                    )
                )
            )
            .then(indented(reserved(ARROW)))
            .then(expr)
        private val parseVar = attempt(
            manyOrEmpty(
                attempt(
                    token(
                        PROPER_NAME
                    ).`as`(PSElements.qualifiedModuleName).then(
                        token(
                            DOT
                        )
                    )
                )
            ).then(ident).`as`(Qualified)
        ).`as`(
            PSElements.Var
        )
        private val parseConstructor = parseQualified(properName).`as`(
            PSElements.Constructor
        )
        private val parseCaseAlternative = commaSep1(
            expr.or(
                parseTypeWildcard
            )
        )
            .then(
                indented(
                    choice(
                        many1(
                            parseGuard.then(
                                indented(
                                    lexeme(
                                        ARROW
                                    ).then(expr)
                                )
                            )
                        ),
                        reserved(ARROW).then(expr)
                    )
                )
            )
            .`as`(PSElements.CaseAlternative)
        private val parseCase = reserved(PSTokens.CASE)
            .then(commaSep1(expr.or(parseTypeWildcard)))
            .then(indented(reserved(PSTokens.OF)))
            .then(
                indented(
                    indentedList(
                        mark(
                            parseCaseAlternative
                        )
                    )
                )
            )
            .`as`(PSElements.Case)
        private val parseIfThenElse = reserved(PSTokens.IF)
            .then(indented(expr))
            .then(indented(reserved(PSTokens.THEN)))
            .then(indented(expr))
            .then(indented(reserved(PSTokens.ELSE)))
            .then(indented(expr))
            .`as`(PSElements.IfThenElse)
        private val parseLet = reserved(PSTokens.LET)
            .then(indented(indentedList1(parseLocalDeclaration)))
            .then(indented(reserved(PSTokens.IN)))
            .then(expr)
            .`as`(PSElements.Let)
        private val letBinding =
            choice(
                attempt(parseTypeDeclaration),
                optional(attempt(reserved(PSTokens.LPAREN)))
                    .then(optional(attempt(properName).`as`(PSElements.Constructor)))
                    .then(optional(attempt(many1(ident))))
                    .then(optional(attempt(parseArrayBinder)))
                    .then(
                        optional(
                            attempt(
                                indented(lexeme("@"))
                                    .then(
                                        indented(
                                            braces(
                                                commaSep(
                                                    lexeme(
                                                        identifier
                                                    )
                                                )
                                            )
                                        )
                                    )
                            )
                        ).`as`(PSElements.NamedBinder)
                    ).then(optional(attempt(parsePatternMatchObject)))
                    .then(optional(attempt(parseRowPatternBinder)))
                    .then(optional(attempt(reserved(PSTokens.RPAREN))))
                    .then(attempt(manyOrEmpty(parseBinderNoParensRef)))
                    .then(
                        choice(
                            attempt(
                                indented(
                                    many1(
                                        parseGuard.then(
                                            indented(
                                                eq.then(
                                                    exprWhere
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            attempt(
                                indented(
                                    eq.then(exprWhere)
                                )
                            )
                        )
                    ).`as`(
                        PSElements.ValueDeclaration
                    )
            )
        private val parseDoNotationBind: Parsec =
            parseBinderRef
                .then(
                    indented(reserved(PSTokens.LARROW))
                        .then(expr)
                )
                .`as`(PSElements.DoNotationBind)
        private val doExpr = expr.`as`(PSElements.DoNotationValue)
        private val doStatement =
            choice(
                reserved(PSTokens.LET)
                    .then(indented(indentedList1(letBinding)))
                    .`as`(PSElements.DoNotationLet),
                attempt(parseDoNotationBind),
                attempt(doExpr)
            )
        private val doBlock =
            reserved(PSTokens.DO)
                .then(indented(indentedList(mark(doStatement))))
        private val parsePropertyUpdate =
            reserved(lname.or(stringLiteral))
                .then(
                    optional(
                        indented(
                            eq
                        )
                    )
                )
                .then(indented(expr))
        private val parseValueAtom = choice(
            attempt(parseTypeHole),
            attempt(parseNumericLiteral),
            attempt(parseStringLiteral),
            attempt(parseBooleanLiteral),
            attempt(
                reserved(TICK) +
                properName.`as`(ProperName)
                .or(many1(lexeme(identifier).`as`(ProperName))) +
                reserved(TICK)
            ),
            parseArrayLiteral,
            parseCharLiteral,
            attempt(
                indented(
                    braces(
                        commaSep1(
                            indented(
                                parsePropertyUpdate
                            )
                        )
                    )
                )
            ),
            attempt(parseObjectLiteral),
            parseAbs,
            attempt(parseConstructor),
            attempt(parseVar),
            parseCase,
            parseIfThenElse,
            doBlock,
            parseLet,
            parens(expr).`as`(PSElements.Parens)
        )
        private val parseAccessor: Parsec = attempt(
            indented(
                token(
                    DOT
                )
            ).then(indented(lname.or(stringLiteral)))
        ).`as`(
            PSElements.Accessor
        )
        private val parseIdentInfix: Parsec = choice(
            reserved(TICK)
                .then(parseQualified(lexeme(identifier))).lexeme(
                    TICK
                ),
            parseQualified(lexeme(operator))
        ).`as`(PSElements.IdentInfix)
        private val indexersAndAccessors = parseValueAtom
            .then(
                manyOrEmpty(
                    choice(
                        parseAccessor,
                        attempt(
                            indented(
                                braces(
                                    commaSep1(
                                        indented(parsePropertyUpdate)
                                    )
                                )
                            )
                        ),
                        indented(
                            reserved(PSTokens.DCOLON)
                                .then(type)
                        )
                    )
                )
            )
        private val parseValuePostFix = indexersAndAccessors
            .then(
                manyOrEmpty(
                    choice(
                        indented(indexersAndAccessors),
                        attempt(
                            indented(
                                lexeme(
                                    PSTokens.DCOLON
                                )
                            ).then(type)
                        )
                    )
                )
            )
        private val parsePrefixRef = Combinators.ref()
        private val parsePrefix =
            choice(
                parseValuePostFix,
            indented(lexeme("-")).then(parsePrefixRef)
                .`as`(
                    PSElements.UnaryMinus
                )
        ).`as`(PSElements.PrefixValue)

        // Binder
        private val parseIdentifierAndBinder =
            lexeme(lname.or(stringLiteral))
            .then(indented(eq.or(lexeme(PSTokens.OPERATOR))))
            .then(indented(parseBinderRef))
        private val parseObjectBinder =
            braces(commaSep(parseIdentifierAndBinder)).`as`(ObjectBinder)
        private val parseNullBinder = reserved("_").`as`(NullBinder)
        private val parseStringBinder =
            lexeme(STRING).`as`(StringBinder)
        private val parseBooleanBinder =
            lexeme("true").or(lexeme("false")).`as`(BooleanBinder)
        private val parseNumberBinder =
            optional(lexeme("+").or(lexeme("-")))
            .then(lexeme(PSTokens.NATURAL).or(lexeme(PSTokens.FLOAT)))
            .`as`(PSElements.NumberBinder)
        private val parseNamedBinder =
            ident
            .then(indented(lexeme("@")).then(indented(parseBinderRef)))
            .`as`(PSElements.NamedBinder)
        private val parseVarBinder = ident.`as`(PSElements.VarBinder)
        private val parseConstructorBinder =
            lexeme(
                parseQualified(properName).`as`(PSElements.GenericIdentifier)
                .then(manyOrEmpty(indented(parseBinderNoParensRef)))
            )
            .`as`(PSElements.ConstructorBinder)
        private val parseNullaryConstructorBinder =
            lexeme(parseQualified(properName.`as`(ProperName)))
            .`as`(PSElements.ConstructorBinder)
        private val parsePatternMatch = indented(
            braces(commaSep(lexeme(identifier)))).`as`(PSElements.Binder)
        private val parseCharBinder =
            lexeme("'").`as`(StringBinder)
        private val parseBinderAtom = choice(
            attempt(parseNullBinder),
            attempt(parseStringBinder),
            attempt(parseBooleanBinder),
            attempt(parseNumberBinder),
            attempt(parseNamedBinder),
            attempt(parseVarBinder),
            attempt(parseConstructorBinder),
            attempt(parseObjectBinder),
            attempt(parseArrayBinder),
            attempt(parsePatternMatch),
            attempt(parseCharBinder),
            attempt(parens(parseBinderRef))
        ).`as`(PSElements.BinderAtom)
        private val parseBinder =
            parseBinderAtom
            .then(optional(lexeme(PSTokens.OPERATOR).then(parseBinderRef)))
            .`as`(PSElements.Binder)
        private val parseBinderNoParens = choice(
            attempt(parseNullBinder),
            attempt(parseStringBinder),
            attempt(parseBooleanBinder),
            attempt(parseNumberBinder),
            attempt(parseNamedBinder),
            attempt(parseVarBinder),
            attempt(parseNullaryConstructorBinder),
            attempt(parseObjectBinder),
            attempt(parseArrayBinder),
            attempt(parsePatternMatch),
            attempt(parseCharBinder),
            attempt(parens(parseBinderRef))
        ).`as`(PSElements.Binder)

        init {
            parseKindPrefixRef.setRef(parseKindPrefix)
            parseKind.setRef(
                (parseKindPrefix +
                    optional(
                        reserved(ARROW)
                            .or(
                                optional(
                                    parseQualified(properName).`as`(
                                        TypeConstructor
                                    )
                                )
                            ) +
                            optional(parseKind)
                    )).`as`(FunKind)
            )
            type.setRef(parseType)
            parseForAllRef.setRef(parseForAll)
            parseLocalDeclarationRef.setRef(parseLocalDeclaration)
            parsePrefixRef.setRef(parsePrefix)
            expr.setRef(
                (
                    parsePrefix + optional(attempt(indented(parseIdentInfix)) + expr)
                    ).`as`(PSElements.Value)
            )
            parseBinderRef.setRef(parseBinder)
            parseBinderNoParensRef.setRef(parseBinderNoParens)
        }
    }
}