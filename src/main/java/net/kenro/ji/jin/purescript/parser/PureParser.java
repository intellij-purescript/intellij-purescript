package net.kenro.ji.jin.purescript.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static net.kenro.ji.jin.purescript.parser.Combinators.Predicate;
import static net.kenro.ji.jin.purescript.parser.Combinators.attempt;
import static net.kenro.ji.jin.purescript.parser.Combinators.braces;
import static net.kenro.ji.jin.purescript.parser.Combinators.choice;
import static net.kenro.ji.jin.purescript.parser.Combinators.commaSep;
import static net.kenro.ji.jin.purescript.parser.Combinators.commaSep1;
import static net.kenro.ji.jin.purescript.parser.Combinators.guard;
import static net.kenro.ji.jin.purescript.parser.Combinators.indented;
import static net.kenro.ji.jin.purescript.parser.Combinators.keyword;
import static net.kenro.ji.jin.purescript.parser.Combinators.lexeme;
import static net.kenro.ji.jin.purescript.parser.Combinators.many;
import static net.kenro.ji.jin.purescript.parser.Combinators.many1;
import static net.kenro.ji.jin.purescript.parser.Combinators.mark;
import static net.kenro.ji.jin.purescript.parser.Combinators.optional;
import static net.kenro.ji.jin.purescript.parser.Combinators.parens;
import static net.kenro.ji.jin.purescript.parser.Combinators.ref;
import static net.kenro.ji.jin.purescript.parser.Combinators.reserved;
import static net.kenro.ji.jin.purescript.parser.Combinators.same;
import static net.kenro.ji.jin.purescript.parser.Combinators.sepBy1;
import static net.kenro.ji.jin.purescript.parser.Combinators.squares;
import static net.kenro.ji.jin.purescript.parser.Combinators.token;
import static net.kenro.ji.jin.purescript.parser.Combinators.untilSame;

import net.kenro.ji.jin.purescript.psi.PSElements;
import net.kenro.ji.jin.purescript.psi.PSTokens;
import org.jetbrains.annotations.NotNull;

public class PureParser implements PsiParser, PSTokens, PSElements {
    @NotNull
    @Override
    public ASTNode parse(IElementType root, PsiBuilder builder) {
        // builder.setDebugMode(true);
        ParserContext context = new ParserContext(builder);
        PsiBuilder.Marker mark = context.start();
        context.whiteSpace();
        // Creating a new instance here allows hot swapping while debugging.
        ParserInfo info = new PureParsecParser().program.parse(context);
        IElementType nextType = null;
        if (!context.eof()) {
            PsiBuilder.Marker errorMarker = null;
            while (!context.eof()) {
                if (context.getPosition() >= info.position && errorMarker == null) {
                    errorMarker = context.start();
                    nextType = builder.getTokenType();
                }
                context.advance();
            }
            if (errorMarker != null) {
                if (nextType != null)
                    errorMarker.error("Unexpected " + nextType.toString() + ". " + info.toString());
                else
                    errorMarker.error(info.toString());
            }
        }
        mark.done(root);
        return builder.getTreeBuilt();
    }

    public final static class PureParsecParser {
        private PureParsecParser() {
        }

        @NotNull
        private Parsec parseQualified(@NotNull Parsec p) {
            return attempt(many(attempt(token(PROPER_NAME).then(token(DOT))).as(pModuleName)).then(p).as(Qualified));
        }

        private final Parsec idents = choice(token(IDENT), choice(token(FORALL), token(QUALIFIED), token(HIDING), token(AS)).as(Identifier));
        private final Parsec identifier = lexeme(idents);
        private final Parsec lname
                = lexeme(choice(
                token(IDENT),
                token(DATA),
                token(NEWTYPE),
                token(TYPE),
                token(FOREIGN),
                token(IMPORT),
                token(INFIXL),
                token(INFIXR),
                token(INFIX),
                token(CLASS),
                token(DERIVE),
                token(INSTANCE),
                token(MODULE),
                token(CASE),
                token(OF),
                token(IF),
                token(THEN),
                token(ELSE),
                token(DO),
                token(LET),
                token(TRUE),
                token(FALSE),
                token(IN),
                token(WHERE),
                token(FORALL),
                token(QUALIFIED),
                token(HIDING),
                token(AS)).as(Identifier));
        private final Parsec operator = choice(token(OPERATOR), token(DDOT), token(LARROW), token(LDARROW), token(OPTIMISTIC));
        private final Parsec properName = lexeme(PROPER_NAME);
        private final Parsec moduleName = lexeme(parseQualified(token(PROPER_NAME).as(pModuleName)));

        private final Parsec stringLiteral = attempt(lexeme(STRING));

        @NotNull
        private Parsec positioned(@NotNull final Parsec p) {
            return p;
        }

        @NotNull
        private Parsec indentedList(@NotNull final Parsec p) {
            return mark(many(untilSame(same(p))));
        }

        @NotNull
        private Parsec indentedList1(@NotNull final Parsec p) {
            return mark(many1(untilSame(same(p))));
        }

        // Kinds.hs
        private final ParsecRef parseKindRef = ref();
        private final ParsecRef parseKindPrefixRef = ref();
        private final SymbolicParsec parseStar = keyword(START, "*").as(Star);
        private final SymbolicParsec parseBang = keyword(BANG, "!").as(Bang);
        private final Parsec parseKindAtom = indented(choice(parseStar, parseBang, parens(parseKindRef)));
        private final Parsec parseKindPrefix
                = choice(
                lexeme("#").then(parseKindPrefixRef).as(RowKind),
                parseKindAtom);
        private final SymbolicParsec parseKind
                = parseKindPrefix.then(optional(reserved(ARROW).then(parseKindRef))).as(FunKind);

        {
            parseKindPrefixRef.setRef(parseKindPrefix);
            parseKindRef.setRef(parseKind);
        }

        // Types.hs
        private final ParsecRef parsePolyTypeRef = ref();
        private final ParsecRef parseTypeRef = ref();
        private final ParsecRef parseForAllRef = ref();

        private final Parsec parseTypeWildcard = reserved("_");

        private final Parsec parseFunction = parens(reserved(ARROW));
        private final Parsec parseTypeVariable = lexeme(guard(idents, new Predicate<String>() {
            @Override
            public boolean test(String content) {
                return !(content.equals("∀") || content.equals("forall"));
            }
        }, "not `forall`")).as(TypeVar);

        private final Parsec parseTypeConstructor = parseQualified(properName).as(TypeConstructor);

        @NotNull
        private Parsec parseNameAndType(Parsec p) {
            return indented(lexeme(choice(lname, stringLiteral))).then(indented(lexeme(DCOLON))).then(p);
        }

        //= indented(seq(attempt(lexeme(identifier).or(lexeme(STRING))), reserved(DCOLON)));
        private final Parsec parseRowEnding
                = optional(
                        indented(lexeme(PIPE)).then(indented(
                                choice(
                                        attempt(parseTypeWildcard),
                                        attempt(lexeme(identifier).as(TypeVar))))));

        private final Parsec parseRow
                = commaSep(parseNameAndType(parsePolyTypeRef))
                .then(parseRowEnding)
                .as(Row);

        private final Parsec parseObject = braces(parseRow).as(ObjectType);
        private final Parsec parseTypeAtom = indented(
                choice(
                        attempt(squares(optional(parseTypeRef))),
                        attempt(parseFunction),
                        attempt(parseObject),
                        attempt(parseTypeWildcard),
                        attempt(parseTypeVariable),
                        attempt(parseTypeConstructor),
                        attempt(parseForAllRef),
                        attempt(parens(parseRow)),
                        attempt(parens(parsePolyTypeRef)))
        ).as(TypeAtom);

        private final Parsec parseConstrainedType =
                optional(attempt(
                        parens(commaSep1(parseQualified(properName).then(indented(many(parseTypeAtom)))))
                                .then(lexeme(DARROW))
                )).then(indented(parseTypeRef)).as(ConstrainedType);
        private final SymbolicParsec parseForAll
                = reserved(FORALL)
                .then(many1(indented(lexeme(identifier))))
                .then(indented(lexeme(DOT)))
                .then(parseConstrainedType).as(ForAll);

        private final Parsec parseIdent = choice(
                lexeme(identifier),
                attempt(parens(lexeme(operator.as(Identifier))))
        );

        private final Parsec parseTypePostfix
                = choice(parseTypeAtom, lexeme(STRING))
                .then(optional(attempt(indented(lexeme(DCOLON).then(parseKind)))));

        private final SymbolicParsec parseType
                = many1(parseTypePostfix)
                .then(optional(
                        choice(reserved(ARROW),reserved(DARROW), reserved(OPTIMISTIC), reserved(OPERATOR)).then(parseTypeRef)
                )).as(Type);

        {
            parsePolyTypeRef.setRef(parseType);
            parseTypeRef.setRef(parseType);
            parseForAllRef.setRef(parseForAll);
        }

        // Declarations.hs
        private final Parsec kindedIdent
                = lexeme(identifier)
                .or(parens(lexeme(identifier).then(indented(lexeme(DCOLON))).then(indented(parseKindRef))));
        private final ParsecRef parseBinderNoParensRef = ref();
        private final ParsecRef parseBinderRef = ref();
        private final ParsecRef parseValueRef = ref();
        private final ParsecRef parseLocalDeclarationRef = ref();

        private final SymbolicParsec parseGuard = lexeme(PIPE).then(indented(parseValueRef)).as(Guard);
        private final SymbolicParsec parseDataDeclaration
                = reserved(DATA)
                .then(indented(properName))
                .then(many(indented(kindedIdent)).as(TypeArgs))
                .then(optional(attempt(lexeme(EQ))
                        .then(sepBy1(properName.then(many(indented(parseTypeAtom))), PIPE))))
                .as(DataDeclaration);

        private final SymbolicParsec parseTypeDeclaration
                = attempt(parseIdent.then(indented(lexeme(DCOLON))))
                .then(parsePolyTypeRef)
                .as(TypeDeclaration);

        private final SymbolicParsec parseNewtypeDeclaration
                = reserved(NEWTYPE)
                .then(indented(properName))
                .then(optional(many(indented(identifier))))
                .then(optional(lexeme(EQ)
                .then(properName
                       .then(optional(many(indented(identifier))))
                       .then(optional(indented(parseTypeAtom))))))
                .as(NewtypeDeclaration);
        private final SymbolicParsec parseTypeSynonymDeclaration
                = reserved(TYPE)
                .then(reserved(PROPER_NAME))
                .then(many(indented(lexeme(kindedIdent))))
                .then(indented(lexeme(EQ)).then(parsePolyTypeRef))
                .as(TypeSynonymDeclaration);

        private final Parsec parseValueWithWhereClause
                = parseValueRef
                .then(optional(
                        indented(lexeme(WHERE))
                                .then(indented(mark(many1(same(parseLocalDeclarationRef)))))));

        // Some Binders - rest at the bottom
        private final Parsec parseIdentifierAndBinder
                = lexeme(lname.or(stringLiteral))
                .then(indented(lexeme(EQ).or(lexeme(OPERATOR))))
                .then(indented(parseBinderRef));
        private final SymbolicParsec parseObjectBinder
                = braces(commaSep(parseIdentifierAndBinder)).as(ObjectBinder);
        private final SymbolicParsec parseArrayBinder = squares(commaSep(parseBinderRef)).as(ObjectBinder);

        private final Parsec parseRowPatternBinder = indented(lexeme(OPERATOR))
                .then(indented(parseBinderRef));

        private final SymbolicParsec parseValueDeclaration
                // this is for when used with LET
                = optional(reserved(LPAREN))
                        .then(optional(properName))
                        .then(optional(many1(parseIdent)))
                        .then(optional(parseArrayBinder))
                        .then(optional(indented(lexeme("@")).then(indented(braces(commaSep(identifier))))).as(NamedBinder))
                        .then(optional(parseObjectBinder))
                        .then(optional(parseRowPatternBinder))
                        .then(optional(reserved(RPAREN)))
                // ---------- end of LET stuff -----------
                .then(many(parseBinderNoParensRef))
                .then(choice(
                        indented(many1(parseGuard.then(indented(lexeme(EQ).then(parseValueWithWhereClause))))),
                        indented(lexeme(EQ).then(parseValueWithWhereClause)))).as(ValueDeclaration);

        private final Parsec parseDeps
                = parens(commaSep1(parseQualified(properName).then(many(parseTypeAtom))))
                .then(indented(reserved(DARROW)));

        private final Parsec parseExternDeclaration
                = reserved(FOREIGN)
                .then(indented(reserved(IMPORT)))
                .then(indented(
                        choice(
                                reserved(DATA)
                                        .then(indented(reserved(PROPER_NAME)))
                                        .then(lexeme(DCOLON))
                                        .then(parseKind)
                                        .as(ExternDataDeclaration),
                                reserved(INSTANCE)
                                        .then(parseIdent)
                                        .then(indented(lexeme(DCOLON)))
                                        .then(optional(parseDeps))
                                        .then(parseQualified(properName).as(pClassName))
                                        .then(many(indented(parseTypeAtom)))
                                        .as(ExternInstanceDeclaration),
                                attempt(parseIdent)
                                        .then(optional(stringLiteral.as(JSRaw)))
                                        .then(indented(lexeme(DCOLON)))
                                        .then(parsePolyTypeRef)
                                        .as(ExternDeclaration)
                        )
                ));
        private final Parsec parseAssociativity = choice(
                reserved(INFIXL),
                reserved(INFIXR),
                reserved(INFIX)
        );
        private final SymbolicParsec parseFixity = parseAssociativity.then(indented(lexeme(NATURAL))).as(Fixity);
        private final SymbolicParsec parseFixityDeclaration
                = parseFixity
                .then((parseQualified(properName).as(pModuleName)).or(parseIdent))
                .then((reserved(AS)))
                .then((lexeme(operator)))
                .as(FixityDeclaration);



        private final SymbolicParsec parseDeclarationRef =
                 choice(
                         reserved("kind").then(parseQualified(properName).as(pClassName)),
                         parseIdent.as(ValueRef),
                         reserved(TYPE).then(optional(parens(operator))),
                reserved(MODULE).then(moduleName),
                reserved(CLASS).then(parseQualified(properName).as(pClassName)),
                properName.then(
                        optional(parens(optional(choice(
                                        reserved(DDOT),
                                commaSep1(properName))))))).as(PositionedDeclarationRef);


        private final SymbolicParsec parseTypeClassDeclaration
                = lexeme(CLASS)
                .then(optional(indented(
                        choice(parens(commaSep1(parseQualified(properName).then(many(parseTypeAtom)))),
                                commaSep1(parseQualified(properName).then(many(parseTypeAtom))))
                ).then(optional(reserved(LDARROW)).as(pImplies))))
                .then(optional(indented(properName.as(pClassName))))
                .then(optional(many(indented(kindedIdent))))
                .then(optional(lexeme(PIPE).then(indented(commaSep1(parsePolyTypeRef)))))
                .then(optional(attempt(
                        indented(reserved(WHERE)).then(
                                indentedList(positioned(parseTypeDeclaration)))
                        )
                ))
                .as(TypeClassDeclaration);

        private final SymbolicParsec parseTypeInstanceDeclaration
                = optional(reserved(DERIVE)).then(reserved(INSTANCE)
                .then(parseIdent.then(indented(lexeme(DCOLON))))
                .then(optional(
                      optional(reserved(LPAREN)).then(commaSep1(parseQualified(properName).then(many(parseTypeAtom)))).then(optional(reserved(RPAREN)))
                                .then(optional(indented(reserved(DARROW))))
                ))
                .then(optional(indented(parseQualified(properName)).as(pClassName)))
                .then(many(indented(parseTypeAtom).or(lexeme(STRING))))
                .then(optional(attempt(
                        indented(reserved(WHERE))
                                .then(indented(indentedList(positioned(parseValueDeclaration))))
                ))))
                .as(TypeInstanceDeclaration);

        private final Parsec importDeclarationType
                = optional(indented(parens(commaSep(parseDeclarationRef))));

        private final SymbolicParsec parseImportDeclaration
                = reserved(IMPORT)
                .then(indented(moduleName))
                .then(optional(reserved(HIDING))
                .then(importDeclarationType))
                .then(optional(reserved(AS).then(moduleName)))
                .as(ImportDeclaration);

        private final Parsec parseDeclaration = positioned(choice(
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
        ));

        private final Parsec parseLocalDeclaration = positioned(choice(
                parseTypeDeclaration,
                parseValueDeclaration
        ));

        {
            parseLocalDeclarationRef.setRef(parseLocalDeclaration);
        }

        private final SymbolicParsec parseModule
                = reserved(MODULE)
                .then(indented(moduleName))
                .then(optional(parens(commaSep1(parseDeclarationRef))))
                .then(reserved(WHERE))
                .then(indentedList(parseDeclaration))
                .as(Module);

        private final Parsec program = indentedList(parseModule).as(Program);
        // Literals
        private final SymbolicParsec parseBooleanLiteral = reserved(PSTokens.TRUE).or(reserved(PSTokens.FALSE)).as(BooleanLiteral);
        private final SymbolicParsec parseNumericLiteral = reserved(NATURAL).or(reserved(FLOAT)).as(NumericLiteral);
        private final SymbolicParsec parseStringLiteral = reserved(STRING).as(StringLiteral);
        private final SymbolicParsec parseArrayLiteral = squares(commaSep(parseValueRef)).as(ArrayLiteral);
        private final SymbolicParsec parseIdentifierAndValue
                = indented(lexeme(lname).or(stringLiteral))
                .then(indented(lexeme(OPERATOR).or(reserved(COMMA))))
                .then(indented(parseValueRef))
                .as(ObjectBinderField);
        private final SymbolicParsec parseObjectLiteral =
                braces(commaSep(parseIdentifierAndValue)).as(ObjectLiteral);

        private final Parsec typedIdent
                = choice(many1(lexeme(identifier).or(properName)),
                parens(many1(lexeme(identifier).or(properName)).then(indented(lexeme(DCOLON))).then(indented(parsePolyTypeRef))));

        private final Parsec parseAbs
                = reserved(BACKSLASH)
                .then(choice(many1(typedIdent).as(Abs), many1(indented(parseIdent.or(parseBinderNoParensRef).as(Abs)))))
                .then(indented(reserved(ARROW)))
                .then(parseValueRef);

        private final SymbolicParsec parseVar = parseQualified(parseIdent).as(Var);
        private final SymbolicParsec parseConstructor = parseQualified(properName).as(Constructor);

        private final SymbolicParsec parseCaseAlternative
                = commaSep1(parseValueRef.or(parseTypeWildcard))
                .then(indented(choice(
                        many1(parseGuard.then(indented(lexeme(ARROW).then(parseValueRef)))),
                        reserved(ARROW).then(parseValueRef))))
                .as(CaseAlternative);



        private final SymbolicParsec parseCase
                = reserved(CASE)
                .then(commaSep1(parseValueRef.or(parseTypeWildcard)))
                .then(indented(reserved(OF)))
                .then(indented(indentedList(mark(parseCaseAlternative))))
                .as(Case);
        private final SymbolicParsec parseIfThenElse
                = reserved(IF)
                .then(indented(parseValueRef))
                .then(indented(reserved(THEN)))
                .then(indented(parseValueRef))
                .then(indented(reserved(ELSE)))
                .then(indented(parseValueRef))
                .as(IfThenElse);
        private final SymbolicParsec parseLet
                = reserved(LET)
                .then(indented(indentedList1(parseLocalDeclaration)))
                .then(indented(reserved(IN)))
                .then(parseValueRef)
                .as(Let);

        private final Parsec parseDoNotationLet
                = reserved(LET)
                .then(indented(indentedList1(parseLocalDeclaration)))
                .as(DoNotationLet);
        private final Parsec parseDoNotationBind
                = parseBinderRef
                .then(indented(reserved(LARROW)).then(parseValueRef))
                .as(DoNotationBind);
        private final Parsec parseDoNotationElement = choice(
                attempt(parseDoNotationBind),
                parseDoNotationLet,
                attempt(parseValueRef.as(DoNotationValue))
        );
        private final Parsec parseDo
                = reserved(DO)
                .then(indented(indentedList(mark(parseDoNotationElement))));


        private final Parsec parseValueAtom = choice(
                attempt(parseNumericLiteral),
                attempt(parseStringLiteral),
                attempt(parseBooleanLiteral),
                attempt(reserved(TICK).then(properName).then(reserved(TICK))),
                parseArrayLiteral,
                attempt(parseObjectLiteral),
                parseAbs,
                attempt(parseConstructor),
                attempt(parseVar),
                parseCase,
                parseIfThenElse,
                parseDo,
                parseLet,
                parens(parseValueRef).as(Parens)
        );

        private final Parsec parsePropertyUpdate
                = reserved(lname.or(stringLiteral))
                .then(indented(lexeme(EQ)))
                .then(indented(parseValueRef));
        private final Parsec parseAccessor
                = attempt(indented(token(DOT)).then(indented(lname.or(stringLiteral)))).as(Accessor);

        private final Parsec parseIdentInfix =
                choice(
                        reserved(TICK).then(parseQualified(identifier)).lexeme(TICK),
                        parseQualified(lexeme(operator))
                ).as(IdentInfix);

        private final Parsec indexersAndAccessors
                = parseValueAtom
                .then(many(choice(
                        parseAccessor,
                        attempt(indented(braces(commaSep1(indented(parsePropertyUpdate))))),
                        indented(reserved(DCOLON).then(parseType))
                )));

        private final Parsec parseValuePostFix
                = indexersAndAccessors
                .then(many(choice(indented(indexersAndAccessors), attempt(indented(lexeme(DCOLON)).then(parsePolyTypeRef)))));

        private final ParsecRef parsePrefixRef = ref();
        private final SymbolicParsec parsePrefix =
                choice(
                        parseValuePostFix,
                        indented(lexeme("-")).then(parsePrefixRef).as(UnaryMinus)
                ).as(PrefixValue);

        {
            parsePrefixRef.setRef(parsePrefix);
        }

        private final SymbolicParsec parseValue
                = parsePrefix
                .then(optional(
                        attempt(indented(parseIdentInfix)).then(parseValueRef)
                ))
                .as(Value);

        // Binders
        private final SymbolicParsec parseNullBinder = reserved("_").as(NullBinder);
        private final SymbolicParsec parseStringBinder = lexeme(STRING).as(StringBinder);
        private final SymbolicParsec parseBooleanBinder = lexeme("true").or(lexeme("false")).as(BooleanBinder);
        private final SymbolicParsec parseNumberBinder
                = optional(choice(lexeme("+"), lexeme("-")))
                .then(lexeme(NATURAL).or(lexeme(FLOAT))).as(NumberBinder);
        private final SymbolicParsec parseNamedBinder = parseIdent.then(indented(lexeme("@")).then(indented(parseBinderRef))).as(NamedBinder);
        private final SymbolicParsec parseVarBinder = parseIdent.as(VarBinder);
        private final SymbolicParsec parseConstructorBinder = lexeme(parseQualified(properName).then(many(indented(parseBinderNoParensRef)))).as(ConstructorBinder);
        private final SymbolicParsec parseNullaryConstructorBinder = lexeme(parseQualified(properName)).as(ConstructorBinder);

        private final SymbolicParsec parsePatternMatch = indented(braces(commaSep(identifier))).as(Binder);

        private final SymbolicParsec parseBinderAtom = choice(
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
                attempt(parens(parseBinderRef))
        ).as(BinderAtom);
        private final SymbolicParsec parseBinder
                = parseBinderAtom
                .then(optional(lexeme(OPERATOR).then(parseBinderRef)))
                .as(Binder);
        private final SymbolicParsec parseBinderNoParens = choice(
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
                attempt(parens(parseBinderRef))
        ).as(Binder);

        {
            parseValueRef.setRef(parseValue);
            parseBinderRef.setRef(parseBinder);
            parseBinderNoParensRef.setRef(parseBinderNoParens);
        }
    }
}
