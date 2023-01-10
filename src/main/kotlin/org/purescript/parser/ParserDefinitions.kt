package org.purescript.parser

class ParserDefinitions {

    // Literals
    private val boolean = `true` / `false`
    private val number = NumericLiteral(NATURAL / FLOAT)
    private val moduleName = ModuleName(Optional(MODULE_PREFIX) + PROPER_NAME)
    private val qualifier = ModuleName(MODULE_PREFIX)
    private fun qualified(p: DSL) = Optional(qualifier) + p

    // ElementTokens

    private val idents =
        IDENT / `as` / HIDING / forall / QUALIFIED / KIND / `'type'`

    private val lname = Identifier(
        IDENT / data / `'newtype'` / `'type'` / `'foreign'` / `'import'` /
            infixl / infixr / infix / `class` / `'derive'` / KIND /
            `'instance'` / module / case / of / `if` / then / `else` / `do` /
            ado / let / `true` / `false` / `in` / where / forall / QUALIFIED /
            HIDING / `as`
    )

    private val label = string / lname

    // this doesn't match parser.y but i dont feel like changing it right now
    // it might be due to differences in the lexer
    private val operator =
        OPERATOR / dot / ddot / ldarrow / OPTIMISTIC / "<=" / "-" / "#" / ":"

    private val properName: DSL = ProperName(PROPER_NAME)
    private val qualifiedProperName = qualified(properName)
    private val type: DSL = Type(Reference { type1 }.sepBy1(dcolon))

    private val parseForAll: DSL = ForAll(
        forall + GenericIdentifier(idents).oneOrMore + dot +
            Reference { parseConstrainedType }
    )

    private val rowLabel =
        GenericIdentifier((lname / string.withRollback)) + dcolon + type

    private val parseRow: DSL =
        Row((pipe + type) / (rowLabel.sepBy(COMMA) + Optional(pipe + type)))

    private val typeAtom: DSL = TypeAtom(
        squares(Optional(type)) /
            ObjectType(braces(parseRow)) /
            `_` /
            string /
            number /
            parseForAll.withRollback /
            GenericIdentifier(idents.withRollback) /
            TypeConstructor(qualifiedProperName.withRollback) /
            parens(arrow / parseRow).withRollback /
            parens(type)
    )

    private fun braces(p: DSL) = LCURLY + p + RCURLY
    private fun parens(p: DSL) = LPAREN + p + RPAREN
    private fun squares(p: DSL) = LBRACK + p + RBRACK

    private val parseConstrainedType = ConstrainedType(
        Optional(
            (parens(
                (TypeConstructor(qualifiedProperName.withRollback) + typeAtom.noneOrMore)
                    .sepBy1(COMMA)
            ) + darrow).withRollback
        ) + type
    )

    private val ident =
        Identifier(idents) / parens(Identifier(operator)).withRollback

    private val typeVarBinding = Choice.of(
        TypeVarName(idents),
        TypeVarKinded(parens(GenericIdentifier(idents) + dcolon + type))
    )
    private val binderAtom: DSL = Reference {
        Choice.of(
            NullBinder(`_`).withRollback,
            NamedBinder((VarBinder(ident) + `@` + this).withRollback),
            VarBinder(ident).withRollback,
            ConstructorBinder(qualProperName).withRollback,
            BooleanBinder(boolean).withRollback,
            CharBinder(char.withRollback),
            StringBinder(string).withRollback,
            NumberBinder(number).withRollback,
            ObjectBinder(squares(binder.sepBy(COMMA))).withRollback,
            braces(recordBinder.sepBy(COMMA)).withRollback,
            parens(binder).withRollback
        )
    }
    private val binder: DSL = Reference { binder1 } + Optional(dcolon + type)
    private val expr = Value((Reference { expr1 } + Optional(dcolon + type)))
    private val qualOp =
        QualifiedOperatorName(qualified(OperatorName(operator)))
    private val type5 = typeAtom.oneOrMore
    private val type4 =
        ("-".dsl + number).withRollback / ("#".dsl.noneOrMore + type5)
    private val type3 = type4.sepBy1(qualOp)
    private val type2: DSL =
        type3 + Optional(arrow / darrow + Reference { type1 })
    private val type1 =
        (forall + typeVarBinding.oneOrMore + dot).noneOrMore + type2
    private val parsePropertyUpdate: DSL = label + Optional(eq) + expr
    private val hole = TypeHole("?".dsl + idents)
    val symbol = Symbol(parens(OperatorName(operator)))
    private val recordLabel = ObjectBinderField(
        ((label + ":").withRollback + expr) /
            ((label + eq).withRollback + expr) /
            ExpressionIdentifier(QualifiedIdentifier(label))
    )
    private val exprAtom = Choice.of(
        `_`,
        hole.withRollback,
        ExpressionIdentifier(QualifiedIdentifier(qualified(Identifier(idents))))
            .withRollback,
        ExpressionSymbol(QualifiedSymbol(qualified(symbol))).withRollback,
        ExpressionConstructor(QualifiedProperName(qualifiedProperName))
            .withRollback,
        BooleanLiteral(boolean),
        CharLiteral(char),
        StringLiteral(string),
        number,
        ArrayLiteral(squares(expr.sepBy(COMMA))),
        ObjectLiteral(braces(recordLabel.sepBy(COMMA))),
        Parens(parens(expr)),
    )
    private val expr7 = exprAtom + Accessor(dot + label).noneOrMore


    private val parseBadSingleCaseBranch: DSL =
        Reference { `L{` + binder1 + (arrow + `L}` + exprWhere) / (`L}` + guardedCase) }

    /*
    * if there is only one case branch it can ignore layout so we need
    * to allow layout end at any time.
    */
    private val exprCase: DSL = Case(
        case + expr.sepBy1(COMMA) + of + Choice.of(
            parseBadSingleCaseBranch.withRollback,
            `L{` + Reference { caseBranch }.sepBy1(`L-sep`) + `L}`
        )
    )
    private val expr5 = Reference {
        braces(parsePropertyUpdate.sepBy1(COMMA)).withRollback /
            expr7 /
            Lambda(backslash + binderAtom.oneOrMore + arrow + expr) /
            exprCase /
            parseIfThenElse /
            doBlock /
            (adoBlock + `in` + expr) /
            parseLet
    }
    private val expr4: DSL = expr5.oneOrMore
    private val expr3 = UnaryMinus("-".dsl.oneOrMore + expr4) / expr4
    private val exprBacktick2 = expr3.sepBy1(qualOp)
    private val expr2 = expr3.sepBy1(tick + exprBacktick2 + tick)
    private val expr1 = expr2.sepBy1(ExpressionOperator(qualOp.withRollback))

    // TODO: pattern guards should parse expr1 not expr
    private val patternGuard = Optional((binder + larrow).withRollback) + expr
    private val parseGuard = Guard(pipe + patternGuard.sepBy(COMMA))
    private val dataHead =
        data + properName + TypeArgs(typeVarBinding.noneOrMore)
    private val dataCtor = DataConstructor(properName + typeAtom.noneOrMore)
    private val parseTypeDeclaration = Signature(ident + dcolon + type)
    private val newtypeHead =
        `'newtype'` + properName + TypeArgs(typeVarBinding.noneOrMore)
    private val exprWhere: DSL =
        expr + Optional(
            ExpressionWhere(
                where + `L{` + Reference { letBinding }.sepBy1(`L-sep`) + `L}`
            )
        )
    private val guardedDeclExpr = parseGuard + eq + exprWhere
    private val guardedDecl =
        (eq.withRollback + exprWhere) / guardedDeclExpr.oneOrMore
    private val instBinder =
        Choice.of(
            (ident + dcolon).withRollback + type,
            ValueDeclaration(ident + binderAtom.noneOrMore + guardedDecl)
        )
    private val parseForeignDeclaration = `'foreign'` + `'import'` + Choice.of(
        ForeignDataDeclaration(data + properName + dcolon + type),
        ForeignValueDeclaration(ident.withRollback + dcolon + type)
    )
    private val parseAssociativity = Choice.of(infixl, infixr, infix)
    private val parseFixity = Fixity(parseAssociativity + NATURAL)
    private val qualIdentifier =
        QualifiedIdentifier(Optional(qualifier) + ident)
    private val qualProperName =
        QualifiedProperName(Optional(qualifier) + properName)
    private val parseFixityDeclaration = FixityDeclarationType(
        parseFixity + Choice.of(
            // TODO Should we differentiate Types and DataConstructors?
            // that would mean that when there is a `type` prefix we parse as Type
            // otherwise if it's a capital name it's a DataConstructor
            (Optional(`'type'`) + properName / qualProperName).withRollback,
            qualIdentifier
        ) + `as` + OperatorName(operator)
    )

    private val fundep = ClassFunctionalDependency(type)
    private val fundeps = pipe + fundep.sepBy1(COMMA)
    private val constraint =
        ClassConstraint(ClassName(qualProperName) + typeAtom.noneOrMore)
    private val constraints = parens(constraint.sepBy1(COMMA)) / constraint
    private val classSuper =
        ClassConstraintList(constraints + pImplies(ldarrow))
    private val classNameAndFundeps =
        ClassName(properName) + typeVarBinding.noneOrMore +
            Optional(ClassFunctionalDependencyList(fundeps))
    private val classSignature = ClassName(properName) + dcolon + type
    private val classHead = Choice.of(
        // this first is described in haskell code and not in normal happy expression
        // see `fmap (Left . DeclKindSignature () $1) parseClassSignature`
        (`class` + classSignature).withRollback,
        (`class` + classSuper + classNameAndFundeps).withRollback,
        `class` + classNameAndFundeps
    )
    private val classMember =
        ClassMember((Identifier(idents) + dcolon + type))

    private val classDeclaration =
        ClassDeclaration(
            (classHead + Optional(
                ClassMemberList(
                    (where + `L{` + (classMember).sepBy1(`L-sep`) + `L}`).withRollback
                )
            ))
        )
    private val instHead =
        `'instance'` + Optional(ident + dcolon) +
            Optional((constraints + darrow).withRollback) +
            constraint // this constraint is the instance type
    private val importedDataMembers = ImportedDataMemberList(
        parens(ddot / ImportedDataMember(properName).sepBy(COMMA))
    )
    private val importedItem =
        Choice.of(
            ImportedType(`'type'` + parens(Identifier(operator))),
            ImportedClass(`class` + properName),
            ImportedKind(KIND + properName),
            ImportedOperator(symbol),
            ImportedValue(ident),
            ImportedData(properName + Optional(importedDataMembers)),
        )
    private val importList =
        ImportList(Optional(HIDING) + parens(importedItem.sepBy(COMMA)))
    private val parseImportDeclaration = ImportType(
        `'import'` + moduleName + Optional(importList) +
            Optional(ImportAlias(`as` + moduleName))
    )

    /**
     * nominal = the type can never be coerced to another type.
     * representational = the type can be coerced to another type if certain conditions apply.
     * phantom - the type can always be coerced to another type.
     * */
    private val role = Choice.of(nominal, representational, phantom)
    private val decl = Choice.of(
        (dataHead + dcolon).withRollback + type,
        DataDeclaration(
            dataHead + Optional(DataConstructorList(eq + dataCtor.sepBy1(PIPE)))
        ),
        (`'newtype'` + properName + dcolon).withRollback + type,
        NewtypeDeclaration(
            newtypeHead + eq + NewTypeConstructor((properName + typeAtom))
        ),
        parseTypeDeclaration.withRollback,
        (`'type'` + `'role'`).withRollback + properName + role.noneOrMore,
        (`'type'` + properName + dcolon).withRollback + type,
        TypeSynonymDeclaration(
            `'type'` + properName + typeVarBinding.noneOrMore + eq + type
        ),
        ValueDeclaration
            (ident.withRollback + binderAtom.noneOrMore + guardedDecl),
        parseForeignDeclaration,
        parseFixityDeclaration,
        classDeclaration,
        InstanceDeclaration(
            (Optional(`'derive'` + Optional(`'newtype'`))
                + instHead
                + Optional(where + `L{` + instBinder.sepBy1(`L-sep`) + `L}`)
                )
        )
    )
    private val exportedClass = ExportedClassType((`class` + properName))
    private val dataMembers = ExportedDataMemberList(
        parens(ddot / ExportedDataMember(properName).sepBy(COMMA))
    )
    private val exportedData =
        ExportedDataType(properName + Optional(dataMembers))
    private val exportedKind = ExportedKindType(KIND + properName)
    private val exportedModule = ExportedModuleType(module + moduleName)
    private val exportedOperator = ExportedOperatorType(symbol)
    private val exportedType =
        ExportedTypeType(`'type'` + parens(Identifier(operator)))
    private val exportedValue = ExportedValueType(ident)
    private val exportList = ExportListType(
        parens(
            Choice.of(
                exportedClass,
                exportedData,
                exportedKind,
                exportedModule,
                exportedOperator,
                exportedType,
                exportedValue,
            ).sepBy1(COMMA)
        )
    )

    private val elseDecl = `else` + Optional(`L-sep`)

    val parseModuleHeader =
        module + moduleName + Optional(exportList) + where + `L{` +
            (parseImportDeclaration + `L-sep`).noneOrMore
    val parseModuleBody = (decl.sepBy(elseDecl) + `L-sep`).noneOrMore + `L}`
    val parseModule = ModuleType(parseModuleHeader + parseModuleBody)
    private val binder2 = Choice.of(
        (ConstructorBinder(qualProperName) + binderAtom.noneOrMore).withRollback,
        NumberBinder(("-".dsl + number).withRollback),
        binderAtom,
    )
    private val binder1 = binder2.sepBy1(qualOp)
    private val guardedCaseExpr = parseGuard + arrow + exprWhere
    private val guardedCase =
        (arrow + exprWhere).withRollback / guardedCaseExpr.noneOrMore
    private val caseBranch =
        CaseAlternative(binder1.sepBy1(COMMA) + guardedCase)

    private val parseIfThenElse =
        IfThenElse(`if` + expr + then + expr + `else` + expr)
    private val letBinding =
        Choice.of(
            parseTypeDeclaration.withRollback,
            ValueDeclaration(ident + binderAtom.noneOrMore + guardedDecl).withRollback,
            (binder1 + eq + exprWhere).withRollback,
            (ident + binderAtom.noneOrMore + guardedDecl).withRollback
        )
    private val parseLet =
        Let(let + `L{` + (letBinding).sepBy1(`L-sep`) + `L}` + `in` + expr)
    private val doStatement = Choice.of(
        DoNotationLet(let + `L{` + letBinding.sepBy1(`L-sep`) + `L}`),
        DoNotationBind(binder + larrow + expr).withRollback,
        DoNotationValue(expr).withRollback
    )
    private val doBlock = DoBlock(
        qualified(`do`).withRollback +
            `L{` + (doStatement).sepBy1(`L-sep`) + `L}`
    )

    private val adoBlock = ado + `L{` + doStatement.sepBy(`L-sep`) + `L}`
    private val recordBinder =
        ((label + eq / ":").withRollback + binder) / VarBinder(label)
}
