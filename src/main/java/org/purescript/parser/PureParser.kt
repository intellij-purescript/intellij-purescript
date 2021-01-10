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
import org.purescript.parser.Combinators.lexeme
import org.purescript.parser.Combinators.many1
import org.purescript.parser.Combinators.manyOrEmpty
import org.purescript.parser.Combinators.mark
import org.purescript.parser.Combinators.optional
import org.purescript.parser.Combinators.parens
import org.purescript.parser.Combinators.reserved
import org.purescript.parser.Combinators.same
import org.purescript.parser.Combinators.sepBy1
import org.purescript.parser.Combinators.token
import org.purescript.parser.Combinators.untilSame
import org.purescript.psi.PSTokens
import org.purescript.psi.PSElements
import org.purescript.psi.PSTokens.Companion.PIPE

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
            return attempt(
                manyOrEmpty(
                    attempt(
                        token(
                            PSTokens.PROPER_NAME
                        ).`as`(PSElements.ProperName).then(
                            token(
                                PSTokens.DOT
                            )
                        )
                    )
                ).then(p).`as`(PSElements.Qualified)
            )
        }
        
        // tokens
        private val dcolon = lexeme(PSTokens.DCOLON)
        private val eq = lexeme(PSTokens.EQ)

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
                token(PSTokens.NEWTYPE),
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
                token(PSTokens.DOT),
                token(PSTokens.DDOT),
                token(PSTokens.LARROW),
                token(PSTokens.LDARROW),
                token(PSTokens.OPTIMISTIC)
            )
        private val properName: Parsec =
            lexeme(PSTokens.PROPER_NAME).`as`(
                PSElements.ProperName
            )
        private val moduleName = lexeme(
            parseQualified(
                token(
                    PSTokens.PROPER_NAME
                )
            )
        )
        private val stringLiteral = attempt(
            lexeme(
                PSTokens.STRING
            )
        )

        private fun positioned(p: Parsec): Parsec {
            return p
        }

        private fun indentedList(p: Parsec): Parsec {
            return mark(manyOrEmpty(untilSame(same(p))))
        }

        private fun indentedList1(p: Parsec): Parsec {
            return mark(
                many1(
                    untilSame(
                        same(p)
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
            choice(
                parseStar, parseBang, parseQualified(properName).`as`(
                    PSElements.TypeConstructor
                ), parens(parseKindRef)
            )
        )
        private val parseKindPrefix = choice(
            lexeme("#").then(parseKindPrefixRef)
                .`as`(PSElements.RowKind),
            parseKindAtom
        )
        private val parseKind = parseKindPrefix.then(
            optional(
                reserved(
                    PSTokens.ARROW
                ).or(
                    optional(
                        parseQualified(properName).`as`(
                            PSElements.TypeConstructor
                        )
                    )
                ).then(optional(parseKindRef))
            )
        ).`as`(
            PSElements.FunKind
        )

        // Types.hs
        private val parsePolyTypeRef = Combinators.ref()
        private val parseTypeRef = Combinators.ref()
        private val parseForAllRef = Combinators.ref()
        private val parseTypeWildcard = reserved("_")
        private val parseFunction = parens(
            reserved(
                PSTokens.ARROW
            )
        )
        private val parseTypeVariable: Parsec = lexeme(
            guard(
                idents,
                { content: String? -> !(content == "∀" || content == "forall") },
                "not `forall`"
            )
        ).`as`(PSElements.GenericIdentifier)
        private val parseTypeConstructor: Parsec =
            parseQualified(properName).`as`(
                PSElements.TypeConstructor
            )

        private fun parseNameAndType(p: Parsec): Parsec {
            return indented(
                lexeme(
                    choice(lname, stringLiteral).`as`(
                        PSElements.GenericIdentifier
                    )
                )
            ).then(
                indented(
                    lexeme(
                        PSTokens.DCOLON
                    )
                )
            ).then(p)
        }

        private val parseRowEnding = optional(
            indented(lexeme(PIPE)).then(
                indented(
                    choice(
                        attempt(parseTypeWildcard),
                        attempt(
                            optional(
                                lexeme(
                                    manyOrEmpty(properName).`as`(
                                        PSElements.TypeConstructor
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
            commaSep(parseNameAndType(parsePolyTypeRef))
                .then(parseRowEnding)
                .`as`(PSElements.Row)
        private val parseObject: Parsec = braces(parseRow).`as`(
            PSElements.ObjectType
        )
        private val parseTypeAtom: Parsec = indented(
            choice(
                attempt(
                    Combinators.squares(
                        optional(
                            parseTypeRef
                        )
                    )
                ),
                attempt(parseFunction),
                attempt(parseObject),
                attempt(parseTypeWildcard),
                attempt(parseTypeVariable),
                attempt(parseTypeConstructor),
                attempt(parseForAllRef),
                attempt(parens(parseRow)),
                attempt(parens(parsePolyTypeRef))
            )
        ).`as`(PSElements.TypeAtom)
        private val parseConstrainedType: Parsec = optional(
            attempt(
                parens(
                    commaSep1(
                        parseQualified(properName).`as`(
                            PSElements.TypeConstructor
                        ).then(
                            indented(
                                manyOrEmpty(parseTypeAtom)
                            )
                        )
                    )
                )
                    .then(lexeme(PSTokens.DARROW))
            )
        ).then(indented(parseTypeRef))
            .`as`(PSElements.ConstrainedType)
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
            .then(indented(lexeme(PSTokens.DOT)))
            .then(parseConstrainedType).`as`(PSElements.ForAll)
        private val parseIdent =
            choice(
                lexeme(identifier.`as`(PSElements.Identifier)),
                attempt(parens(lexeme(operator.`as`(PSElements.Identifier))))
            )
        private val parseTypePostfix = choice(
            parseTypeAtom, lexeme(
                PSTokens.STRING
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
        private val kindedIdent = lexeme(identifier).`as`(
            PSElements.GenericIdentifier
        )
            .or(
                parens(
                    lexeme(identifier)
                        .`as`(PSElements.GenericIdentifier).then(
                            indented(
                                lexeme(
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
        private val parseGuard = lexeme(PIPE)
            .then(indented(commaSep(parseValueRef)))
            .`as`(
                PSElements.Guard
            )
        private val dataHead =
            reserved(PSTokens.DATA) +
                indented(properName).`as`(PSElements.TypeConstructor) +
                manyOrEmpty(indented(kindedIdent)).`as`(PSElements.TypeArgs)

        val dataCtor =
            properName.`as`(PSElements.TypeConstructor) +
                manyOrEmpty(indented(parseTypeAtom))
        private val parseTypeDeclaration =
            (
                parseIdent.`as`(PSElements.TypeAnnotationName) +
                indented(dcolon) +
                parsePolyTypeRef
            ).`as`(PSElements.TypeDeclaration)
        private val parseNewtypeDeclaration =
            reserved(PSTokens.NEWTYPE)
                .then(
                    indented(properName)
                        .`as`(PSElements.TypeConstructor)
                )
                .then(
                    manyOrEmpty(indented(kindedIdent)).`as`(
                        PSElements.TypeArgs
                    )
                )
                .then(
                    optional(
                        eq
                            .then(
                                properName.`as`(PSElements.TypeConstructor)
                                    .then(
                                        optional(
                                            manyOrEmpty(
                                                indented(
                                                    lexeme(
                                                        identifier
                                                    )
                                                )
                                            )
                                        )
                                    )
                                    .then(
                                        optional(
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
                manyOrEmpty(
                    indented(
                        lexeme(
                            kindedIdent
                        )
                    )
                )
            )
            .then(
                indented(eq)
                    .then(parsePolyTypeRef)
            )
            .`as`(PSElements.TypeSynonymDeclaration)
        private val parseValueWithWhereClause = parseValueRef
            .then(
                optional(
                    indented(lexeme(PSTokens.WHERE))
                        .then(
                            indented(
                                mark(
                                    many1(
                                        same(
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
            Combinators.squares(commaSep(parseBinderRef)).`as`(
                PSElements.ObjectBinder
            )
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
        private val parseValueDeclaration // this is for when used with LET
            = optional(attempt(reserved(PSTokens.LPAREN)))
            .then(optional(attempt(properName).`as`(PSElements.Constructor)))
            .then(optional(attempt(many1(parseIdent))))
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
            .then(
                choice(
                    attempt(
                        indented(
                            many1(
                                parseGuard.then(
                                    indented(
                                        eq.then(parseValueWithWhereClause)
                                    )
                                )
                            )
                        )
                    ),
                    attempt(
                        indented(
                            eq.then(parseValueWithWhereClause)
                        )
                    )
                )
            ).`as`(
                PSElements.ValueDeclaration
            )
        private val parseDeps = parens(
            commaSep1(
                parseQualified(properName).`as`(
                    PSElements.TypeConstructor
                ).then(manyOrEmpty(parseTypeAtom))
            )
        )
            .then(indented(reserved(PSTokens.DARROW)))
        private val parseExternDeclaration =
            reserved(PSTokens.FOREIGN)
                .then(indented(reserved(PSTokens.IMPORT)))
                .then(
                    indented(
                        choice(
                            reserved(PSTokens.DATA)
                                .then(
                                    indented(
                                        reserved(PSTokens.PROPER_NAME)
                                            .`as`(
                                                PSElements.TypeConstructor
                                            )
                                    )
                                )
                                .then(dcolon)
                                .then(parseKind)
                                .`as`(PSElements.ExternDataDeclaration),
                            reserved(PSTokens.INSTANCE)
                                .then(parseIdent)
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
                            attempt(parseIdent)
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
                                .then(parsePolyTypeRef)
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
                    parseIdent.`as`(
                        PSElements.ProperName
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
            parseIdent.`as`(PSElements.ValueRef),
            reserved(PSTokens.TYPE)
                .then(optional(parens(operator))),
            reserved(PSTokens.MODULE).then(moduleName)
                .`as`(PSElements.importModuleName),
            reserved(PSTokens.CLASS).then(
                parseQualified(properName).`as`(
                    PSElements.pClassName
                )
            ),
            properName.`as`(PSElements.ProperName).then(
                optional(
                    parens(
                        optional(
                            choice(
                                reserved(PSTokens.DDOT),
                                commaSep1(properName.`as`(PSElements.TypeConstructor))
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
                                            PSElements.TypeConstructor
                                        ).then(manyOrEmpty(parseTypeAtom))
                                    )
                                ),
                                commaSep1(
                                    parseQualified(properName).`as`(
                                        PSElements.TypeConstructor
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
                                commaSep1(parsePolyTypeRef)
                            )
                        )
                    )
                )
                .then(
                    optional(
                        attempt(
                            indented(reserved(PSTokens.WHERE))
                                .then(
                                    indentedList(positioned(parseTypeDeclaration))
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
                    PSTokens.NEWTYPE
                )
            )
        ).then(
            reserved(PSTokens.INSTANCE)
                .then(
                    parseIdent.`as`(PSElements.GenericIdentifier).then(
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
                                        PSElements.TypeConstructor
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
                                            PSTokens.DARROW
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
                                PSTokens.STRING
                            )
                        )
                    )
                )
                .then(
                    optional(
                        indented(
                            reserved(
                                PSTokens.DARROW
                            )
                        ).then(
                            optional(
                                reserved(
                                    PSTokens.LPAREN
                                )
                            )
                        )
                            .then(parseQualified(properName).`as`(PSElements.TypeConstructor))
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
                                            positioned(parseValueDeclaration)
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
        private val parseDecl = positioned(
            choice(
                attempt(dataHead + eq + sepBy1(dataCtor, PIPE))
                    .`as`(PSElements.DataDeclaration),
                (dataHead).`as`(PSElements.DataDeclaration),
                parseNewtypeDeclaration,
                attempt(parseTypeDeclaration),
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
            choice(
                attempt(parseTypeDeclaration),
                parseValueDeclaration
            )
        )
        private val parseModule = reserved(PSTokens.MODULE)
            .then(indented(moduleName).`as`(PSElements.pModuleName))
            .then(optional(parens(commaSep1(parseDeclarationRef))))
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
            lexeme("'").`as`(PSElements.StringLiteral)
        private val parseArrayLiteral =
            Combinators.squares(commaSep(parseValueRef)).`as`(
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
                .then(optional(indented(parseValueRef)))
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
                                PSElements.TypeConstructor
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
                    ).then(indented(parsePolyTypeRef))
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
                            parseIdent.or(parseBinderNoParensRef).`as`(
                                PSElements.Abs
                            )
                        )
                    )
                )
            )
            .then(indented(reserved(PSTokens.ARROW)))
            .then(parseValueRef)
        private val parseVar = attempt(
            manyOrEmpty(
                attempt(
                    token(
                        PSTokens.PROPER_NAME
                    ).`as`(PSElements.qualifiedModuleName).then(
                        token(
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
        private val parseCaseAlternative = commaSep1(
            parseValueRef.or(
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
            .then(commaSep1(parseValueRef.or(parseTypeWildcard)))
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
        private val letBinding =
            choice(
                attempt(parseTypeDeclaration),
                optional(attempt(reserved(PSTokens.LPAREN)))
                    .then(optional(attempt(properName).`as`(PSElements.Constructor)))
                    .then(optional(attempt(many1(parseIdent))))
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
                                                eq.then(parseValueWithWhereClause)
                                            )
                                        )
                                    )
                                )
                            ),
                            attempt(
                                indented(
                                    eq.then(parseValueWithWhereClause)
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
                        .then(parseValueRef)
                )
                .`as`(PSElements.DoNotationBind)
        private val doExpr = parseValueRef.`as`(PSElements.DoNotationValue)
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
                .then(indented(parseValueRef))
        private val parseValueAtom = choice(
            attempt(parseTypeHole),
            attempt(parseNumericLiteral),
            attempt(parseStringLiteral),
            attempt(parseBooleanLiteral),
            attempt(
                reserved(PSTokens.TICK).then(
                    choice(
                        properName.`as`(
                            PSElements.ProperName
                        ),
                        many1(
                            lexeme(identifier)
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
            parens(parseValueRef).`as`(PSElements.Parens)
        )
        private val parseAccessor: Parsec = attempt(
            indented(
                token(
                    PSTokens.DOT
                )
            ).then(indented(lname.or(stringLiteral)))
        ).`as`(
            PSElements.Accessor
        )
        private val parseIdentInfix: Parsec = choice(
            reserved(PSTokens.TICK)
                .then(parseQualified(lexeme(identifier))).lexeme(
                    PSTokens.TICK
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
                                .then(parseType)
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
                            ).then(parsePolyTypeRef)
                        )
                    )
                )
            )
        private val parsePrefixRef = Combinators.ref()
        private val parsePrefix = choice(
            parseValuePostFix,
            indented(lexeme("-")).then(parsePrefixRef)
                .`as`(
                    PSElements.UnaryMinus
                )
        ).`as`(PSElements.PrefixValue)
        private val parseValue = parsePrefix
            .then(
                optional(
                    attempt(indented(parseIdentInfix))
                        .then(parseValueRef)
                )
            )
            .`as`(PSElements.Value)

        // Binder
        private val parseIdentifierAndBinder =
            lexeme(lname.or(stringLiteral))
                .then(
                    indented(
                        eq.or(
                            lexeme(
                                PSTokens.OPERATOR
                            )
                        )
                    )
                )
                .then(indented(parseBinderRef))
        private val parseObjectBinder =
            braces(commaSep(parseIdentifierAndBinder))
                .`as`(
                    PSElements.ObjectBinder
                )
        private val parseNullBinder = reserved("_").`as`(
            PSElements.NullBinder
        )
        private val parseStringBinder =
            lexeme(PSTokens.STRING).`as`(
                PSElements.StringBinder
            )
        private val parseBooleanBinder =
            lexeme("true").or(lexeme("false")).`as`(
                PSElements.BooleanBinder
            )
        private val parseNumberBinder = optional(
            choice(
                lexeme("+"),
                lexeme("-")
            )
        )
            .then(
                lexeme(PSTokens.NATURAL).or(
                    lexeme(
                        PSTokens.FLOAT
                    )
                )
            ).`as`(PSElements.NumberBinder)
        private val parseNamedBinder = parseIdent.then(
            indented(lexeme("@"))
                .then(indented(parseBinderRef))
        ).`as`(
            PSElements.NamedBinder
        )
        private val parseVarBinder = parseIdent.`as`(PSElements.VarBinder)
        private val parseConstructorBinder = lexeme(
            parseQualified(properName).`as`(
                PSElements.GenericIdentifier
            ).then(
                manyOrEmpty(
                    indented(
                        parseBinderNoParensRef
                    )
                )
            )
        ).`as`(
            PSElements.ConstructorBinder
        )
        private val parseNullaryConstructorBinder = lexeme(
            parseQualified(
                properName.`as`(
                    PSElements.ProperName
                )
            )
        ).`as`(PSElements.ConstructorBinder)
        private val parsePatternMatch = indented(
            braces(
                commaSep(lexeme(identifier))
            )
        ).`as`(
            PSElements.Binder
        )
        private val parseCharBinder =
            lexeme("'").`as`(PSElements.StringBinder)
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
        private val parseBinder = parseBinderAtom
            .then(
                optional(
                    lexeme(PSTokens.OPERATOR).then(parseBinderRef)
                )
            )
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