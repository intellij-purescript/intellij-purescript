package org.purescript.parser

class ParserDefinitions {

    // Literals
    private val boolean = `true`.or(`false`)
    private val number =
        ElementToken(NATURAL).or(ElementToken(FLOAT)).`as`(NumericLiteral)

    private val moduleName =
        (Optional(ElementToken(MODULE_PREFIX)) + ElementToken(PROPER_NAME)).`as`(
            ModuleName
        )
    private val qualifier = ElementToken(MODULE_PREFIX).`as`(ModuleName)
    private fun qualified(p: DSL) = Optional(qualifier) + p

    // ElementTokens

    private val idents =
        Choice.of(
            ElementToken(IDENT),
            `as`,
            ElementToken(HIDING),
            forall,
            ElementToken(QUALIFIED),
            ElementToken(KIND),
            `'type'`,
        )

    private val lname = Choice.of(
        ElementToken(IDENT),
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
        ElementToken(KIND),
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
        ElementToken(QUALIFIED),
        ElementToken(HIDING),
        `as`
    ).`as`(Identifier)

    private val label = Choice.of(string, lname)

    // this doesn't match parser.y but i dont feel like changing it right now
    // it might be due to differences in the lexer
    private val operator =
        Choice.of(
            ElementToken(OPERATOR),
            dot,
            ddot,
            ldarrow,
            ElementToken(OPTIMISTIC),
            StringToken("<="),
            StringToken("-"),
            StringToken("#"),
            StringToken(":"),
        )

    private val properName: DSL = ElementToken(PROPER_NAME).`as`(ProperName)

    // Kinds.hs
    private val parseKind: DSL = Reference {
        (parseKindPrefix +
            Optional(
                arrow.or(
                    Optional(
                        qualified(properName)
                            .withRollback
                            .`as`(TypeConstructor)
                    )
                ) + Optional(this)
            )
            ).`as`(FunKind)
    }
    private val parseKindAtom = Choice.of(
        StringToken("*").`as`(START).`as`(Star),
        StringToken("!").`as`(BANG).`as`(Bang),
        qualified(properName).withRollback.`as`(TypeConstructor),
        parens(parseKind)
    )
    private val parseKindPrefix = Reference {
        ((StringToken("#") + this).`as`(RowKind)).or(parseKindAtom)
    }
    private val type: DSL = Reference{ type1 }.sepBy1(dcolon).`as`(Type)

    private val parseForAll: DSL =
        (forall + idents
            .`as`(GenericIdentifier)
            .oneOrMore + dot + Reference { parseConstrainedType })
            .`as`(ForAll)

    private val rowLabel =
        lname.or(string.withRollback).`as`(GenericIdentifier) + dcolon + type

    private val parseRow: DSL =
        (pipe + type)
            .or(rowLabel.sepBy(ElementToken(COMMA)) + Optional(pipe + type))
            .`as`(Row)

    private val typeAtom: DSL =
        Choice.of(
            squares(Optional(type)).withRollback,
            parens(arrow).withRollback,
            braces(parseRow).`as`(ObjectType).withRollback,
            `_`.withRollback,
            string.withRollback,
            number.withRollback,
            parseForAll.withRollback,
            idents.`as`(GenericIdentifier).withRollback,
            qualified(properName)
                .withRollback
                .`as`(TypeConstructor)
                .withRollback,
            parens(parseRow).withRollback,
            parens(type).withRollback
        ).`as`(TypeAtom)

    private fun braces(p: DSL): DSL =
        ElementToken(LCURLY) + p + ElementToken(RCURLY)

    private fun parens(p: DSL): DSL =
        ElementToken(LPAREN) + p + ElementToken(RPAREN)

    private fun squares(p: DSL): DSL =
        ElementToken(LBRACK) + p + ElementToken(RBRACK)

    private val parseConstrainedType: DSL =
        (Optional(
            (parens(
                (qualified(properName)
                    .withRollback
                    .`as`(TypeConstructor) +
                    typeAtom.noneOrMore
                    ).sepBy1(ElementToken(COMMA))
            ) + darrow
                ).withRollback
        ) + type).`as`(ConstrainedType)

    private val ident =
        idents
            .`as`(Identifier)
            .or(parens(operator.`as`(Identifier)).withRollback)

    private val typeVarBinding = Choice.of(
        idents.`as`(TypeVarName),
        parens(idents.`as`(GenericIdentifier) + dcolon + type)
            .`as`(TypeVarKinded)
    )
    private val binderAtom: DSL = Reference {
        Choice.of(
            `_`.`as`(NullBinder).withRollback,
            (ident.`as`(VarBinder) + `@` + this)
                .withRollback
                .`as`(NamedBinder),
            ident.`as`(VarBinder).withRollback,
            qualProperName.`as`(ConstructorBinder).withRollback,
            boolean.`as`(BooleanBinder).withRollback,
            char.withRollback.`as`(CharBinder),
            string.`as`(StringBinder).withRollback,
            number.`as`(NumberBinder).withRollback,
            squares(binder.sepBy(ElementToken(COMMA)))
                .`as`(ObjectBinder)
                .withRollback,
            braces(recordBinder.sepBy(ElementToken(COMMA))).withRollback,
            parens(binder).withRollback
        )
    }
    private val binder: DSL = Reference { binder1 } + Optional(dcolon + type)
    private val expr: DSL = (Reference {expr1} + Optional(dcolon + type))
        .`as`(Value)
    private val qualOp = qualified(operator.`as`(OperatorName))
        .`as`(QualifiedOperatorName)
    private val type5 = typeAtom.oneOrMore
    private val type4 =
        (StringToken("-") + number)
            .withRollback
            .or(StringToken("#").noneOrMore + type5)
    private val type3 = type4.sepBy1(qualOp)
    private val type2: DSL = type3 + Optional(arrow.or(darrow) + Reference { type1 })
    private val type1 = (forall + typeVarBinding.oneOrMore + dot).noneOrMore + type2
    private val parsePropertyUpdate: DSL = label + Optional(eq) + expr
    private val hole = (StringToken("?") + idents).`as`(TypeHole)
    val symbol = parens(operator.`as`(OperatorName)).`as`(Symbol)
    private val recordLabel = Choice.of(
        (label + StringToken(":")).withRollback + expr,
        (label + eq).withRollback + expr,
        label.`as`(QualifiedIdentifier).`as`(ExpressionIdentifier),
    ).`as`(ObjectBinderField)
    private val exprAtom =
        Choice.of(
            `_`,
            hole.withRollback,
            qualified(idents.`as`(Identifier))
                .`as`(QualifiedIdentifier)
                .`as`(ExpressionIdentifier).withRollback,
            qualified(symbol).`as`(QualifiedSymbol)
                .`as`(ExpressionSymbol).withRollback,
            qualified(properName)
                .`as`(QualifiedProperName)
                .`as`(ExpressionConstructor).withRollback,
            boolean.`as`(BooleanLiteral),
            char.`as`(CharLiteral),
            string.`as`(StringLiteral),
            number,
            squares(expr.sepBy(ElementToken(COMMA))).`as`(ArrayLiteral),
            braces(recordLabel.sepBy(ElementToken(COMMA))).`as`(ObjectLiteral),
            parens(expr).`as`(Parens),
        )
    private val expr7 = exprAtom + (dot + label).`as`(Accessor).noneOrMore


    private val parseBadSingleCaseBranch: DSL =
        Reference { `L{` + binder1 + (arrow + `L}` + exprWhere).or(`L}` + guardedCase) }
    /*
    * if there is only one case branch it can ignore layout so we need
    * to allow layout end at any time.
    */
    private val exprCase: DSL = 
        (case + expr.sepBy1(ElementToken(COMMA)) + of + Choice.of(
            parseBadSingleCaseBranch.withRollback,
            `L{` + Reference { caseBranch }.sepBy1(`L-sep`) + `L}`
        )).`as`(Case)
    private val expr5 = Reference {
        Choice.of(
            braces(parsePropertyUpdate.sepBy1(ElementToken(COMMA))).withRollback,
            expr7,
            (backslash + binderAtom.oneOrMore + arrow + expr).`as`(Lambda),
            exprCase,
            parseIfThenElse,
            doBlock,
            adoBlock + `in` + expr,
            parseLet
        )
    }
    private val expr4 = expr5.oneOrMore
    private val expr3 =
        (StringToken("-").oneOrMore + expr4).`as`(UnaryMinus).or(expr4)
    private val exprBacktick2 = expr3.sepBy1(qualOp)
    private val expr2 = expr3.sepBy1(tick + exprBacktick2 + tick)
    private val expr1 = expr2.sepBy1(
        qualOp
            .withRollback
            .`as`(ExpressionOperator)
    )


    // TODO: pattern guards should parse expr1 not expr
    private val patternGuard = Optional((binder + larrow).withRollback) + expr
    private val parseGuard =
        (pipe + patternGuard.sepBy(ElementToken(COMMA))).`as`(Guard)
    private val dataHead =
        data + properName + typeVarBinding.noneOrMore.`as`(TypeArgs)
    private val dataCtor =
        (properName + typeAtom.noneOrMore).`as`(DataConstructor)
    private val parseTypeDeclaration =
        (ident + dcolon + type).`as`(Signature)
    private val newtypeHead =
        `'newtype'` + properName + typeVarBinding.noneOrMore.`as`(TypeArgs)
    private val exprWhere: DSL =
        expr + Optional(
            (where + `L{` + Reference { letBinding }.sepBy1(`L-sep`) + `L}`)
                .`as`(ExpressionWhere)
        )
    private val guardedDeclExpr = parseGuard + eq + exprWhere
    private val guardedDecl =
        Choice.of(eq.withRollback + exprWhere, guardedDeclExpr.oneOrMore)
    private val instBinder =
        Choice.of(
            (ident + dcolon).withRollback + type,
            (ident + binderAtom.noneOrMore + guardedDecl)
                .`as`(ValueDeclaration)
        )
    private val parseForeignDeclaration =
        `'foreign'` + `'import'` + Choice.of(
            data + properName + dcolon + type `as` ForeignDataDeclaration,
            ident.withRollback + dcolon + type `as` ForeignValueDeclaration
        )
    private val parseAssociativity = Choice.of(infixl, infixr, infix)
    private val parseFixity =
        (parseAssociativity + ElementToken(NATURAL)).`as`(Fixity)
    private val qualIdentifier =
        (Optional(qualifier) + ident).`as`(QualifiedIdentifier)
    private val qualProperName =
        (Optional(qualifier) + properName).`as`(QualifiedProperName)
    private val parseFixityDeclaration =
        (parseFixity + Choice.of(
            // TODO Should we differentiate Types and DataConstructors?
            // that would mean that when there is a `type` prefix we parse as Type
            // otherwise if it's a capital name it's a DataConstructor
            (Optional(`'type'`) + properName.or(qualProperName)).withRollback,
            ident.or(qualIdentifier)
        ) + `as` + operator.`as`(OperatorName))
            .`as`(FixityDeclaration)

    private val fundep = type.`as`(ClassFunctionalDependency)
    private val fundeps = pipe + fundep.sepBy1(ElementToken(COMMA))
    private val constraint =
        (qualProperName.`as`(ClassName) + typeAtom.noneOrMore)
            .`as`(ClassConstraint)
    private val constraints =
        parens(constraint.sepBy1(ElementToken(COMMA))).or(constraint)

    private val classSuper =
        (constraints + ldarrow.`as`(pImplies)).`as`(ClassConstraintList)
    private val classNameAndFundeps =
        properName.`as`(ClassName) + typeVarBinding.noneOrMore +
            Optional(fundeps.`as`(ClassFunctionalDependencyList))
    private val classSignature = properName.`as`(ClassName) + dcolon + type
    private val classHead = Choice.of(
        // this first is described in haskell code and not in normal happy expression
        // see `fmap (Left . DeclKindSignature () $1) parseClassSignature`
        (`class` + classSignature).withRollback,
        (`class` + classSuper + classNameAndFundeps).withRollback,
        `class` + classNameAndFundeps
    )
    private val classMember =
        (idents.`as`(Identifier) + dcolon + type).`as`(ClassMember)

    private val classDeclaration =
        (classHead + Optional(
            (where + `L{` + (classMember).sepBy1(`L-sep`) + `L}`).withRollback
                .`as`(ClassMemberList)
        )).`as`(ClassDeclaration)
    private val instHead =
        `'instance'` + Optional(ident + dcolon) +
            Optional((constraints + darrow).withRollback) +
            constraint // this constraint is the instance type
    private val importedDataMembers =
        parens(
            ddot.or(
                properName
                    .`as`(ImportedDataMember)
                    .sepBy(ElementToken(COMMA))
            )
        )
            .`as`(ImportedDataMemberList)
    private val importedItem =
        Choice.of(
            (`'type'` + parens(operator.`as`(Identifier))).`as`(ImportedType),
            (`class` + properName).`as`(ImportedClass),
            (ElementToken(KIND) + properName).`as`(ImportedKind),
            symbol.`as`(ImportedOperator),
            ident.`as`(ImportedValue),
            (properName + Optional(importedDataMembers)).`as`(ImportedData),
        )
    private val importList =
        (Optional(ElementToken(HIDING)) + parens(
            importedItem.sepBy(
                ElementToken(
                    COMMA
                )
            )
        ))
            .`as`(ImportList)
    private val parseImportDeclaration = (
        `'import'` +
            moduleName +
            Optional(importList) +
            Optional((`as` + moduleName).`as`(ImportAlias))
        ).`as`(ImportDeclaration)

    /**
     * nominal = the type can never be coerced to another type.
     * representational = the type can be coerced to another type if certain conditions apply.
     * phantom - the type can always be coerced to another type.
     * */
    private val role = Choice.of(nominal, representational, phantom)
    private val decl = Choice.of(
        (dataHead + dcolon).withRollback + type,
        (dataHead +
            Optional(
                (eq + dataCtor.sepBy1(ElementToken(PIPE))).`as`(
                    DataConstructorList
                )
            )
            ).`as`(DataDeclaration),
        (`'newtype'` + properName + dcolon).withRollback + type,
        (newtypeHead + eq + (properName + typeAtom).`as`(NewTypeConstructor))
            .`as`(NewtypeDeclaration),
        parseTypeDeclaration.withRollback,
        (`'type'` + `'role'`).withRollback + properName + role.noneOrMore,
        (`'type'` + properName + dcolon).withRollback + type,
        (`'type'` + properName + typeVarBinding.noneOrMore + eq + type)
            .`as`(TypeSynonymDeclaration),
        (ident.withRollback + binderAtom.noneOrMore + guardedDecl).`as`(
            ValueDeclaration
        ),
        parseForeignDeclaration,
        parseFixityDeclaration,
        classDeclaration,
        (Optional(`'derive'` + Optional(`'newtype'`))
            + instHead
            + Optional(where + `L{` + instBinder.sepBy1(`L-sep`) + `L}`)
            ).`as`(InstanceDeclaration)
    )
    private val exportedClass = (`class` + properName).`as`(ExportedClass)
    private val dataMembers =
        parens(
            ddot.or(
                properName
                    .`as`(ExportedDataMember)
                    .sepBy(ElementToken(COMMA))
            )
        )
            .`as`(ExportedDataMemberList)
    private val exportedData =
        (properName + Optional(dataMembers)).`as`(ExportedData)
    private val exportedKind =
        (ElementToken(KIND) + properName).`as`(ExportedKind)
    private val exportedModule =
        (module + moduleName).`as`(ExportedModule)
    private val exportedOperator = symbol.`as`(ExportedOperator)
    private val exportedType =
        (`'type'` + parens(operator.`as`(Identifier))).`as`(ExportedType)
    private val exportedValue = ident.`as`(ExportedValue)
    private val exportList = parens(
        Choice.of(
            exportedClass,
            exportedData,
            exportedKind,
            exportedModule,
            exportedOperator,
            exportedType,
            exportedValue,
        ).sepBy1(ElementToken(COMMA))
    ).`as`(ExportList)

    private val elseDecl = `else` + Optional(`L-sep`)

    val parseModuleHeader = 
        module + moduleName + Optional(exportList) + where + `L{` +
            (parseImportDeclaration + `L-sep`).noneOrMore
    val parseModuleBody = (decl.sepBy(elseDecl) + `L-sep`).noneOrMore + `L}`
    val parseModule = ( parseModuleHeader + parseModuleBody).`as`(ModuleType)


    private val binder2 = Choice.of(
        (qualProperName.`as`(ConstructorBinder) + binderAtom.noneOrMore).withRollback,
        (StringToken("-") + number).withRollback.`as`(NumberBinder),
        binderAtom,
    )
    private val binder1 = binder2.sepBy1(qualOp)
    private val guardedCaseExpr = parseGuard + (arrow + exprWhere)
    private val guardedCase =
        (arrow + exprWhere).withRollback.or(guardedCaseExpr.noneOrMore)
    private val caseBranch =
        (binder1.sepBy1(ElementToken(COMMA)) + guardedCase).`as`(CaseAlternative)

    private val parseIfThenElse =
        (`if` + expr + then + expr + `else` + expr).`as`(IfThenElse)
    private val letBinding =
        Choice.of(
            parseTypeDeclaration.withRollback,
            (ident + binderAtom.noneOrMore + guardedDecl).withRollback
                .`as`(ValueDeclaration),
            (binder1 + eq + exprWhere).withRollback,
            (ident + binderAtom.noneOrMore + guardedDecl).withRollback
        )
    private val parseLet =
        (let + `L{` + (letBinding).sepBy1(`L-sep`) + `L}` + `in` + expr)
            .`as`(Let)
    private val doStatement = Choice.of(
        (let + (`L{` + (letBinding).sepBy1(`L-sep`) + `L}`)).`as`(DoNotationLet),
        (binder + larrow + expr).withRollback.`as`(DoNotationBind),
        expr.`as`(DoNotationValue).withRollback
    )
    private val doBlock =
        (qualified(`do`).withRollback + `L{` + (doStatement).sepBy1(
            `L-sep`
        ) + `L}`).`as`(DoBlock)

    private val adoBlock = ado + `L{` + (doStatement).sepBy(`L-sep`) + `L}`

    private val recordBinder =
        ((label + eq.or(StringToken(":"))).withRollback + binder).or(
            label.`as`(
                VarBinder
            )
        )
}
