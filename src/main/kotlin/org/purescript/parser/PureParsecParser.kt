package org.purescript.parser

import org.purescript.parser.Combinators.braces
import org.purescript.parser.Combinators.choice
import org.purescript.parser.Combinators.guard
import org.purescript.parser.Combinators.optional
import org.purescript.parser.Combinators.parens
import org.purescript.parser.Combinators.ref
import org.purescript.parser.Combinators.squares
import org.purescript.parser.Combinators.token

class PureParsecParser {

    // Literals
    private val boolean = `true`.or(`false`)
    private val number = token(NATURAL).or(token(FLOAT)).`as`(NumericLiteral)

    private val moduleName =
        (optional(token(MODULE_PREFIX)) + token(PROPER_NAME)).`as`(ModuleName)
    private val qualifier = token(MODULE_PREFIX).`as`(ModuleName)
    private fun qualified(p: Parsec) = optional(qualifier) + p

    // tokens

    private val idents =
        choice(
            token(IDENT),
            `as`,
            token(HIDING),
            forall,
            token(QUALIFIED),
            token(KIND),
            `'type'`,
        )

    private val lname = choice(
        token(IDENT),
        data,
        `'newtype'`,
        `'type'`,
        `'foreign'`,
        `'import'`,
        infixl,
        infixr,
        infix,
        `class`,
        `'derive'`,
        token(KIND),
        `'instance'`,
        module,
        case,
        of,
        `if`,
        then,
        `else`,
        `do`,
        ado,
        let,
        `true`,
        `false`,
        `in`,
        where,
        forall,
        token(QUALIFIED),
        token(HIDING),
        `as`
    ).`as`(Identifier)

    private val label = choice(string, lname)

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
    private val parseKind: Parsec = ref {
        (parseKindPrefix +
            optional(
                arrow.or(
                    optional(
                        qualified(properName)
                            .withRollback()
                            .`as`(TypeConstructor)
                    )
                ) + optional(this)
            )
            ).`as`(FunKind)
    }
    private val parseKindPrefixRef: Parsec = ref { parseKindPrefix }
    private val parseKindAtom = choice(
        token("*").`as`(START).`as`(Star),
        token("!").`as`(BANG).`as`(Bang),
        qualified(properName).withRollback().`as`(TypeConstructor),
        parens(parseKind)
    )
    private val parseKindPrefix =
        ((token("#") + parseKindPrefixRef).`as`(RowKind)).or(parseKindAtom)

    private val type: Parsec = ref { type1.sepBy1(dcolon).`as`(Type) }

    private val parseForAll: Parsec = ref {
        (forall + idents
            .`as`(GenericIdentifier)
            .oneOrMore() + dot + parseConstrainedType)
            .`as`(ForAll)
    }

    private val parseTypeVariable: Parsec =
        guard(idents, "not `forall`") { !(it == "∀" || it == "forall") }
            .`as`(GenericIdentifier)

    private val rowLabel =
        lname.or(string.withRollback()).`as`(GenericIdentifier) + dcolon + type

    private val parseRow: Parsec =
        (pipe + type)
            .or(rowLabel.sepBy(token(COMMA)) + optional(pipe + type))
            .`as`(Row)

    private val typeAtom: Parsec =
        choice(
            squares(optional(type)).withRollback(),
            parens(arrow).withRollback(),
            braces(parseRow).`as`(ObjectType).withRollback(),
            `_`.withRollback(),
            string.withRollback(),
            number.withRollback(),
            parseForAll.withRollback(),
            parseTypeVariable.withRollback(),
            qualified(properName)
                .withRollback()
                .`as`(TypeConstructor)
                .withRollback(),
            parens(parseRow).withRollback(),
            parens(type).withRollback()
        ).`as`(TypeAtom)
    private val parseConstrainedType: Parsec =
        (optional(
            (parens(
                (qualified(properName)
                    .withRollback()
                    .`as`(TypeConstructor) +
                    typeAtom.noneOrMore()
                    ).sepBy1(token(COMMA))
            ) + darrow
                ).withRollback()
        ) + type).`as`(ConstrainedType)

    private val ident =
        idents
            .`as`(Identifier)
            .or(parens(operator.`as`(Identifier)).withRollback())

    private val typeVarBinding = choice(
        idents.`as`(TypeVarName),
        parens(idents.`as`(GenericIdentifier) + dcolon + type)
            .`as`(TypeVarKinded)
    )
    private val binderAtom: Parsec = ref {
        choice(
            `_`.`as`(NullBinder).withRollback(),
            (ident.`as`(VarBinder) + `@` + this)
                .withRollback()
                .`as`(NamedBinder),
            ident.`as`(VarBinder).withRollback(),
            qualProperName.`as`(ConstructorBinder).withRollback(),
            boolean.`as`(BooleanBinder).withRollback(),
            char.withRollback().`as`(CharBinder),
            string.`as`(StringBinder).withRollback(),
            number.`as`(NumberBinder).withRollback(),
            squares(binder.sepBy(token(COMMA)))
                .`as`(ObjectBinder)
                .withRollback(),
            braces(recordBinder.sepBy(token(COMMA))).withRollback(),
            parens(binder).withRollback()
        )
    }
    private val binder: Parsec = ref { binder1 + optional(dcolon + type) }
    private val expr: Parsec =
        ref { (expr1 + optional(dcolon + type)).`as`(Value) }
    private val qualOp =
        qualified(operator.`as`(OperatorName)).`as`(QualifiedOperatorName)
    private val type5 = typeAtom.oneOrMore()
    private val type4 =
        (token("-") + number).withRollback().or(token("#").noneOrMore() + type5)
    private val type3 = ref { type4.sepBy1(qualOp) }
    private val type2: Parsec =
        ref { type3 + optional(arrow.or(darrow) + type1) }
    private val type1 =
        (forall + typeVarBinding.oneOrMore() + dot).noneOrMore() + type2
    private val parsePropertyUpdate: Parsec =
        ref { label + optional(eq) + expr }
    private val exprAtom = ref {
        choice(
            `_`,
            hole.withRollback(),
            qualified(idents.`as`(Identifier))
                .`as`(QualifiedIdentifier)
                .`as`(ExpressionIdentifier).withRollback(),
            qualified(symbol).`as`(QualifiedSymbol)
                .`as`(ExpressionSymbol).withRollback(),
            qualified(properName)
                .`as`(QualifiedProperName)
                .`as`(ExpressionConstructor).withRollback(),
            boolean.`as`(BooleanLiteral),
            char.`as`(CharLiteral),
            string.`as`(StringLiteral),
            number,
            squares(expr.sepBy(token(COMMA))).`as`(ArrayLiteral),
            braces(recordLabel.sepBy(token(COMMA))).`as`(ObjectLiteral),
            parens(expr).`as`(Parens),
        )
    }
    private val expr7 = exprAtom + (dot + label).`as`(Accessor).noneOrMore()

    /*
    * if there is only one case branch it can ignore layout so we need
    * to allow layout end at any time.
    */
    private val exprCase: Parsec = ref {
        (case + expr.sepBy1(token(COMMA)) + of + choice(
            parseBadSingleCaseBranch.withRollback(),
            `L{` + caseBranch.sepBy1(`L-sep`) + `L}`
        )).`as`(Case)
    }
    private val parseBadSingleCaseBranch: Parsec =
        ref { `L{` + binder1 + (arrow + `L}` + exprWhere).or(`L}` + guardedCase) }
    private val expr5 = ref {
        choice(
            braces(parsePropertyUpdate.sepBy1(token(COMMA))).withRollback(),
            expr7,
            (backslash + binderAtom.oneOrMore() + arrow + expr).`as`(Lambda),
            exprCase,
            parseIfThenElse,
            doBlock,
            adoBlock + `in` + expr,
            parseLet
        )
    }
    private val expr4 = expr5.oneOrMore()
    private val expr3 =
        (token("-").oneOrMore() + expr4).`as`(UnaryMinus).or(expr4)
    private val exprBacktick2 = expr3.sepBy1(qualOp)
    private val expr2 = expr3.sepBy1(tick + exprBacktick2 + tick)
    private val expr1 = expr2.sepBy1(
        qualOp
            .withRollback()
            .`as`(ExpressionOperator)
    )


    // TODO: pattern guards should parse expr1 not expr
    private val patternGuard = optional((binder + larrow).withRollback()) + expr
    private val parseGuard =
        (pipe + patternGuard.sepBy(token(COMMA))).`as`(Guard)
    private val dataHead =
        data + properName + typeVarBinding.noneOrMore().`as`(TypeArgs)
    private val dataCtor =
        (properName + typeAtom.noneOrMore()).`as`(DataConstructor)
    private val parseTypeDeclaration =
        (ident + dcolon + type).`as`(Signature)
    private val newtypeHead =
        `'newtype'` + properName + typeVarBinding.noneOrMore().`as`(TypeArgs)
    private val exprWhere: Parsec = ref {
        expr + optional(
            (where + `L{` + letBinding.sepBy1(`L-sep`) + `L}`).`as`(
                ExpressionWhere
            )
        )
    }
    private val guardedDeclExpr = parseGuard + eq + exprWhere
    private val guardedDecl =
        choice(eq.withRollback() + exprWhere, guardedDeclExpr.oneOrMore())
    private val instBinder =
        choice(
            (ident + dcolon).withRollback() + type,
            (ident + binderAtom.noneOrMore() + guardedDecl)
                .`as`(ValueDeclaration)
        )
    private val parseDeps =
        parens(
            (qualified(properName).withRollback().`as`(TypeConstructor)
                + typeAtom.noneOrMore()
                ).sepBy1(token(COMMA))
        ) + darrow
    private val parseForeignDeclaration =
        `'foreign'` + `'import'` + choice(
            data + properName + dcolon + type `as` ForeignDataDeclaration,
            ident.withRollback() + dcolon + type `as` ForeignValueDeclaration
        )
    private val parseAssociativity = choice(infixl, infixr, infix)
    private val parseFixity = (parseAssociativity + token(NATURAL)).`as`(Fixity)
    private val qualIdentifier =
        (optional(qualifier) + ident).`as`(QualifiedIdentifier)
    private val qualProperName =
        (optional(qualifier) + properName).`as`(QualifiedProperName)
    private val parseFixityDeclaration =
        (parseFixity + choice(
            // TODO Should we differentiate Types and DataConstructors?
            // that would mean that when there is a `type` prefix we parse as Type
            // otherwise if it's a capital name it's a DataConstructor
            (optional(`'type'`) + properName.or(qualProperName)).withRollback(),
            ident.or(qualIdentifier)
        ) + `as` + operator.`as`(OperatorName))
            .`as`(FixityDeclaration)

    private val fundep = type.`as`(ClassFunctionalDependency)
    private val fundeps = pipe + fundep.sepBy1(token(COMMA))
    private val constraint =
        (qualProperName.`as`(ClassName) + typeAtom.noneOrMore())
            .`as`(ClassConstraint)
    private val constraints =
        parens(constraint.sepBy1(token(COMMA))).or(constraint)

    private val classSuper =
        (constraints + ldarrow.`as`(pImplies)).`as`(ClassConstraintList)
    private val classNameAndFundeps =
        properName.`as`(ClassName) + typeVarBinding.noneOrMore() +
            optional(fundeps.`as`(ClassFunctionalDependencyList))
    private val classSignature = properName.`as`(ClassName) + dcolon + type
    private val classHead = choice(
        // this first is described in haskell code and not in normal happy expression
        // see `fmap (Left . DeclKindSignature () $1) parseClassSignature`
        (`class` + classSignature).withRollback(),
        (`class` + classSuper + classNameAndFundeps).withRollback(),
        `class` + classNameAndFundeps
    )
    private val classMember =
        (idents.`as`(Identifier) + dcolon + type).`as`(ClassMember)

    private val classDeclaration =
        (classHead + optional(
            (where + `L{` + (classMember).sepBy1(`L-sep`) + `L}`).withRollback()
                .`as`(ClassMemberList)
        )).`as`(ClassDeclaration)
    private val instHead =
        `'instance'` + optional(ident + dcolon) +
            optional((constraints + darrow).withRollback()) +
            constraint // this constraint is the instance type
    private val importedDataMembers =
        parens(
            ddot.or(
                properName
                    .`as`(ImportedDataMember)
                    .sepBy(token(COMMA))
            )
        )
            .`as`(ImportedDataMemberList)
    val symbol = parens(operator.`as`(OperatorName)).`as`(Symbol)
    private val importedItem =
        choice(
            (`'type'` + parens(operator.`as`(Identifier))).`as`(ImportedType),
            (`class` + properName).`as`(ImportedClass),
            (token(KIND) + properName).`as`(ImportedKind),
            symbol.`as`(ImportedOperator),
            ident.`as`(ImportedValue),
            (properName + optional(importedDataMembers)).`as`(ImportedData),
        )
    private val importList =
        (optional(token(HIDING)) + parens(importedItem.sepBy(token(COMMA))))
            .`as`(ImportList)
    private val parseImportDeclaration = (
        `'import'` +
            moduleName +
            optional(importList) +
            optional((`as` + moduleName).`as`(ImportAlias))
        ).`as`(ImportDeclaration)

    /**
     * nominal = the type can never be coerced to another type.
     * representational = the type can be coerced to another type if certain conditions apply.
     * phantom - the type can always be coerced to another type.
     * */
    private val role = choice(nominal, representational, phantom)
    private val decl = choice(
        (dataHead + dcolon).withRollback() + type,
        (dataHead +
            optional(
                (eq + dataCtor.sepBy1(token(PIPE))).`as`(
                    DataConstructorList
                )
            )
            ).`as`(DataDeclaration),
        (`'newtype'` + properName + dcolon).withRollback() + type,
        (newtypeHead + eq + (properName + typeAtom).`as`(NewTypeConstructor))
            .`as`(NewtypeDeclaration),
        parseTypeDeclaration.withRollback(),
        (`'type'` + `'role'`).withRollback() + properName + role.noneOrMore(),
        (`'type'` + properName + dcolon).withRollback() + type,
        (`'type'` + properName + typeVarBinding.noneOrMore() + eq + type)
            .`as`(TypeSynonymDeclaration),
        (ident.withRollback() + binderAtom.noneOrMore() + guardedDecl).`as`(
            ValueDeclaration
        ),
        parseForeignDeclaration,
        parseFixityDeclaration,
        classDeclaration,
        (optional(`'derive'` + optional(`'newtype'`))
            + instHead
            + optional(where + `L{` + instBinder.sepBy1(`L-sep`) + `L}`)
            ).`as`(InstanceDeclaration)
    )
    private val exportedClass = (`class` + properName).`as`(ExportedClass)
    private val dataMembers =
        parens(ddot.or(properName.`as`(ExportedDataMember).sepBy(token(COMMA))))
            .`as`(ExportedDataMemberList)
    private val exportedData =
        (properName + optional(dataMembers)).`as`(ExportedData)
    private val exportedKind = (token(KIND) + properName).`as`(ExportedKind)
    private val exportedModule =
        (module + moduleName).`as`(ExportedModule)
    private val exportedOperator = symbol.`as`(ExportedOperator)
    private val exportedType =
        (`'type'` + parens(operator.`as`(Identifier))).`as`(ExportedType)
    private val exportedValue = ident.`as`(ExportedValue)
    private val exportList = parens(
        choice(
            exportedClass,
            exportedData,
            exportedKind,
            exportedModule,
            exportedOperator,
            exportedType,
            exportedValue,
        ).sepBy1(token(COMMA))
    ).`as`(ExportList)

    private val elseDecl = token("else") + optional(`L-sep`)
    private val moduleDecl = parseImportDeclaration.or(decl.sepBy(elseDecl))

    val parseModule = (
        module + moduleName + optional(exportList) + where +
            `L{` + moduleDecl.sepBy(`L-sep`) + `L}`
        ).`as`(Module)

    private val hole = (token("?") + idents).`as`(TypeHole)
    private val recordLabel = choice(
        (label + token(":")).withRollback() + expr,
        (label + eq).withRollback() + expr,
        label.`as`(QualifiedIdentifier).`as`(ExpressionIdentifier),
    ).`as`(ObjectBinderField)

    private val binder2 = choice(
        (qualProperName.`as`(ConstructorBinder) + binderAtom.noneOrMore()).withRollback(),
        (token("-") + number).withRollback().`as`(NumberBinder),
        binderAtom,
    )
    private val binder1 = binder2.sepBy1(qualOp)
    private val guardedCaseExpr = parseGuard + (arrow + exprWhere)
    private val guardedCase =
        (arrow + exprWhere).withRollback().or(guardedCaseExpr.noneOrMore())
    private val caseBranch =
        (binder1.sepBy1(token(COMMA)) + guardedCase).`as`(CaseAlternative)

    private val parseIfThenElse =
        (`if` + expr + then + expr + `else` + expr).`as`(IfThenElse)
    private val letBinding =
        choice(
            parseTypeDeclaration.withRollback(),
            (ident + binderAtom.noneOrMore() + guardedDecl).withRollback()
                .`as`(ValueDeclaration),
            (binder1 + eq + exprWhere).withRollback(),
            (ident + binderAtom.noneOrMore() + guardedDecl).withRollback()
        )
    private val parseLet =
        (let + `L{` + (letBinding).sepBy1(`L-sep`) + `L}` + `in` + expr)
            .`as`(Let)
    private val doStatement = choice(
        (let + (`L{` + (letBinding).sepBy1(`L-sep`) + `L}`)).`as`(DoNotationLet),
        (binder + larrow + expr).withRollback().`as`(DoNotationBind),
        expr.`as`(DoNotationValue).withRollback()
    )
    private val doBlock =
        (qualified(`do`).withRollback() + `L{` + (doStatement).sepBy1(
            `L-sep`
        ) + `L}`).`as`(DoBlock)

    private val adoBlock = ado + `L{` + (doStatement).sepBy(`L-sep`) + `L}`

    private val recordBinder =
        ((label + eq.or(token(":"))).withRollback() + binder).or(
            label.`as`(
                VarBinder
            )
        )
}
