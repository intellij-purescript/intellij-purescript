package org.purescript.parser

class ParserDefinitions {
    // Literals
    private val boolean = `'true'` / `'false'`
    private val number = NumericLiteral(NATURAL / FLOAT)
    private val moduleName = ModuleName(!MODULE_PREFIX + PROPER_NAME)
    private val qualifier = ModuleName(MODULE_PREFIX)

    // Utils
    private fun qualified(p: DSL) = !(qualifier) + p
    private fun braces(p: DSL) = LCURLY + p + RCURLY
    private fun parens(p: DSL) = LPAREN + p + RPAREN
    private fun squares(p: DSL) = LBRACK + p + RBRACK

    // TODO: add 'representational' and 'phantom'
    private val ident = Identifier(LOWER / `'as'` / `'hiding'` / `'role'` / `'nominal'`)
    private val label = Identifier(
        Choice.of(
            LOWER.dsl,
            string,
            `'ado'`,
            `'as'`,
            `'case'`,
            `'class'`,
            `'data'`,
            `'derive'`,
            `'do'`,
            `'else'`,
            `'false'`,
            `'forall'`,
            `'foreign'`,
            `'hiding'`,
            `'import'`,
            `'if'`,
            `'in'`,
            `'infix'`,
            `'infixl'`,
            `'infixr'`,
            `'instance'`,
            `'let'`,
            `'module'`,
            `'newtype'`,
            `'nominal'`,
            `'of'`,
            // TODO: phantom
            // TODO: representational
            `'role'`,
            `'then'`,
            `'true'`,
            `'type'`,
            `'where'`,
        )
    )

    // this doesn't match parser.y but i dont feel like changing it right now
    // it might be due to differences in the lexer
    private val operator = OPERATOR / dot / ddot / ldarrow / OPTIMISTIC / "<=" / "-" / ":"
    private val properName: DSL = ProperName(PROPER_NAME)

    /**
     * ProperName with optional qualification
     */
    private val qualProperName = QualifiedProperName(qualified(properName))
    private val type: DSL = Type(Reference { type1 }.sepBy1(dcolon))
    private val forAll = ForAll(`'forall'` + +ident + dot + Reference { constrainedType })
    private val rowLabel = label + dcolon + type.relax("malformed type")
    private val row = Row(
        (`|` + type) /
                (!(rowLabel + !+(`,` + rowLabel.relaxTo(RCURLY.dsl / `,`, "malformed row label")).heal) + !(`|` + type))
    )
    private val typeCtor = TypeCtor(qualProperName)
    private val hole = TypeHole("?".dsl + ident)
    private val typeAtom: DSL = TypeAtom(
        hole /
                squares(!type) /
                ObjectType(braces(row)) /
                `_` /
                string /
                number /
                typeCtor /
                forAll.heal /
                ident /
                parens(arrow / row).heal /
                parens(type)
    )

    private val constrainedType = ConstrainedType(!(parens((typeCtor + !+typeAtom).sepBy1(`,`)) + darrow).heal + type)
    private val typeVar = TypeVarName(ident) / TypeVarKinded(parens(ident + dcolon + type))
    private val binderAtom: DSL = Reference {
        Choice.of(
            NullBinder(`_`),
            CharBinder(char),
            StringBinder(string),
            NumberBinder(number),
            ObjectBinder(squares(binder.sepBy(`,`))),
            recordLayout(recordBinder, "record binder"),
            parens(binder),
            BooleanBinder(boolean),
            CtorBinder(qualProperName),
            NamedBinder(VarBinder(ident) + `@` + this).heal,
            VarBinder(ident),
        )
    }
    private val binder: DSL = Reference { binder1 } + !(dcolon + type)
    private val expr = Value(Reference { expr1 } + !(dcolon + type))
    private val `expr?` = expr.relax("missing expression")
    private val operatorName = OperatorName(operator)
    private val qualOp = QualifiedOperatorName(qualified(operatorName))
    private val type5 = +typeAtom
    private val type4 = ("-".dsl + number) / type5
    private val type3 = type4.sepBy1(qualOp)
    private val type2: DSL = type3 + !(arrow / darrow + Reference { type1 })
    private val type1 = !+(`'forall'` + +typeVar + dot) + type2
    private val propertyUpdate: DSL = label + !eq + expr
    val symbol = Symbol(parens(operatorName))

    private val recordLabel = ObjectBinderField(
        ((label + ":").heal + expr.relaxTo(RCURLY.dsl / `,`, "malformed expression")) /
                ((label + eq).heal + expr.relaxTo(RCURLY.dsl / `,`, "malformed expression")) /
                ExpressionIdentifier(QualifiedIdentifier(label)).relaxTo(RCURLY.dsl / `,`, "malformed label")
    )
    /**
     * exprAtom :: { Expr () }
     *   : '_' { ExprSection () $1 }
     *   | hole { ExprHole () $1 }
     *   | qualIdent { ExprIdent () $1 }
     *   | qualProperName { ExprConstructor () (getQualifiedProperName $1) }
     *   | qualSymbol { ExprOpName () (getQualifiedOpName $1) }
     *   | boolean { uncurry (ExprBoolean ()) $1 }
     *   | char { uncurry (ExprChar ()) $1 }
     *   | string { uncurry (ExprString ()) $1 }
     *   | number { uncurry (ExprNumber ()) $1 }
     *   | delim('[', expr, ',', ']') { ExprArray () $1 }
     *   | delim('{', recordLabel, ',', '}') { ExprRecord () $1 }
     *   | '(' expr ')' { ExprParens () (Wrapped $1 $2 $3) }
     *   */
    private val exprAtom = Choice.of(
        `_`,
        hole.heal,
        ExpressionIdentifier(QualifiedIdentifier(qualified(ident))).heal,
        ExpressionSymbol(QualifiedSymbol(qualified(symbol))).heal,
        ExpressionCtor(qualProperName).heal,
        BooleanLiteral(boolean),
        CharLiteral(char),
        StringLiteral(string),
        number,
        ArrayLiteral(squares(!(expr + !+(`,` + expr.relax("missing array element"))).heal)),
        ObjectLiteral(recordLayout(recordLabel, "record label")),
        Parens(parens(expr.relax("empty parenthesis"))),
    )
    private val expr7 = exprAtom + !+Accessor(dot + label)

    private val badSingleCaseBranch = Reference { `L{` + binder1 + (arrow + `L}` + exprWhere) / (`L}` + guardedCase) }
    /*
    * if there is only one case branch it can ignore layout so we need
    * to allow layout end at any time.
    */
    private val exprCase: DSL = Case(
        `'case'` + `expr?`.sepBy1(`,`) + `'of'` + Choice.of(
            badSingleCaseBranch.heal,
            layout1(Reference { caseBranch }, "case branch")
        ).relax("missing case branches")
    )
    private val expr5 = Reference {
        recordLayout1(propertyUpdate, "property update").heal /
                expr7 /
                Lambda(backslash + +binderAtom + arrow.relax("missing lambda arrow") + expr) /
                exprCase /
                ifThenElse /
                doBlock /
                (adoBlock + `'in'` + expr) /
                letIn
    }
    private val expr4: DSL = +expr5
    private val expr3 = UnaryMinus(+"-".dsl + expr4) / expr4
    private val exprBacktick2 = expr3.sepBy1(qualOp)
    private val expr2 = expr3.sepBy1(tick + exprBacktick2 + tick)
    private val expr1 = expr2.sepBy1(ExpressionOperator(qualOp.heal)) +
            !(ExpressionOperator(qualOp.heal) + expr2.relax("missing value")).heal
    private val patternGuard = !(binder + larrow).heal + Reference { Value(expr1) }
    private val guard = Guard(`|` + patternGuard.sepBy(`,`))
    private val dataHead = `'data'` + properName + TypeArgs(!+typeVar)
    private val dataCtor = DataCtor(properName + !+typeAtom)
    private val typeDeclaration = Signature(ident + dcolon + type.relax("malformed type"))
    private val newtypeHead = `'newtype'` + properName + TypeArgs(!+typeVar)

    private val exprWhere: DSL =
        `expr?` + !ExpressionWhere(
            `'where'` + layout1(
                Reference { letBinding },
                "where statement"
            )
        )
    private val guardedDeclExpr = guard + eq + exprWhere
    private val guardedDecl = (eq.heal + exprWhere.relax("Missing Value")) / +guardedDeclExpr
    private val instBinder = Choice.of((ident + dcolon).heal + type, valueDeclarationGroup())
    private val foreignDeclaration = `'foreign'` + `'import'` + Choice.of(
        ForeignDataDeclType(`'data'` + properName + dcolon + type),
        ForeignValueDeclType(ident.heal + dcolon + type)
    )
    private val fixity = Fixity(`'infixl'` / `'infixr'` / `'infix'` + NATURAL)
    private val qualIdentifier = QualifiedIdentifier(!qualifier + ident)
    private val fixityDeclaration = FixityDeclType(
        fixity + Choice.of(
            // TODO Should we differentiate Types and DataConstructors?
            // that would mean that when there is a `type` prefix we parse as Type
            // otherwise if it's a capital name it's a DataConstructor
            (!`'type'` + qualProperName).heal,
            qualIdentifier
        ) + `'as'` + operatorName
    )

    private val fundep = ClassFunctionalDependency(type)
    private val fundeps = `|` + fundep.sepBy1(`,`)
    private val constraint = ClassConstraint(ClassName(qualProperName) + !+typeAtom)
    private val constraints = parens(constraint.sepBy1(`,`)) / constraint
    private val classSuper = ClassConstraintList(constraints + pImplies(ldarrow))
    private val classNameAndFundeps = ClassName(properName) + !+typeVar + !ClassFunctionalDependencyList(fundeps)
    private val classSignature = ClassName(properName) + dcolon + type

    // this first is described in haskell code and not in normal happy expression
    // see `fmap (Left . DeclKindSignature () $1) parseClassSignature`
    private val classHead = `'class'` + classSignature.heal / (!classSuper.heal + classNameAndFundeps)
    private val classMember = ClassMember(ident + dcolon + type)
    private val classDeclaration =
        ClassDeclType(classHead + !ClassMemberList(`'where'` + layout1(classMember, "class member")).heal)
    private val instHead =
        `'instance'` + !(ident + dcolon) + !(constraints + darrow)
            .heal + constraint // this constraint is the instance type
    private val importedDataMembers = ImportedDataMemberList(parens(ddot / ImportedDataMember(properName).sepBy(`,`)))
    private val importedItem = Choice.of(
        ImportedType(`'type'` + parens(Identifier(operator))),
        ImportedClass(`'class'` + properName),
        ImportedOperator(symbol),
        ImportedValue(ident),
        ImportedData(properName + !importedDataMembers),
    )
    private val importList = ImportList(!HIDING + parens(importedItem.sepBy(`,`)))
    private val importDeclaration =
        ImportType(`'import'` + moduleName + !importList + !ImportAlias(`'as'` + moduleName))

    /**
     * nominal = the type can never be coerced to another type.
     * representational = the type can be coerced to another type if certain conditions apply.
     * phantom - the type can always be coerced to another type.
     * */
    private val role = `'nominal'` / representational / phantom
    private fun namedValueDecl(name: String) =
        ValueDeclType(Lookahead(ident.heal) { tokenText == name } + !+binderAtom + guardedDecl)

    private fun valueDeclarationGroup() =
        ValueDeclarationGroupType(Capture { name ->
            !(typeDeclaration + `L-sep`).heal + namedValueDecl(name).sepBy1(`L-sep`)
        }).heal

    private val decl = Choice.of(
        (dataHead + dcolon).heal + type,
        DataDecl(dataHead + !DataCtorList(eq + dataCtor.sepBy1(`|`))),
        (`'newtype'` + properName + dcolon).heal + type,
        NewtypeDeclType(newtypeHead + eq + NewtypeCtorType(properName + typeAtom)),
        (`'type'` + `'role'`).heal + properName + !+role,
        (`'type'` + properName + dcolon).heal + type,
        TypeDeclType(`'type'` + properName + !+typeVar + eq + type),
        valueDeclarationGroup(),
        typeDeclaration.heal,
        foreignDeclaration,
        fixityDeclaration,
        classDeclaration,
        InstanceDeclType(
            !(`'derive'` + !`'newtype'`) + instHead
                    + !(`'where'` + layout1(instBinder, "instance member"))
        )
    )
    private val dataMembers = ExportedDataMemberListType(parens(ddot / ExportedDataMember(properName).sepBy(`,`)))
    private val exportedItem = Choice.of(
        ExportedClassType(`'class'` + properName),
        ExportedDataType(properName + !dataMembers),
        ExportedModuleType(`'module'` + moduleName),
        ExportedOperatorType(symbol),
        ExportedTypeType(`'type'` + parens(Identifier(operator))),
        ExportedValueType(ident),
    )
    private val exportList = ExportListType(parens(exportedItem.sepBy1(`,`)))
    private val elseDecl = `'else'` + !`L-sep`
    val moduleHeader =
        `'module'` + moduleName + !exportList + `'where'` + `L{` +
                !+(importDeclaration + `L-sep`)
    val moduleBody = Choice.of(
        `L}`,
        !+(decl.sepBy1(elseDecl).relaxTo(`L-sep`, "malformed declaration") + `L-sep`) + `L}`
    )

    val module = ModuleType(moduleHeader + moduleBody)
    private val binder2 = Choice.of(
        (CtorBinder(qualProperName) + !+binderAtom).heal,
        NumberBinder(("-".dsl + number).heal),
        binderAtom,
    )
    private val binder1 = binder2.sepBy1(qualOp)
    private val guardedCaseExpr = guard + arrow + exprWhere
    private val guardedCase = (arrow + exprWhere).heal / !+guardedCaseExpr
    private val caseBranch = CaseAlternative(binder1.sepBy1(`,`) + guardedCase)
    private val ifThenElse = IfThenElse(
        `'if'` + `expr?` +
                (`'then'` + `expr?` +
                (`'else'` + `expr?`).relax("missing else")).relax("missing then")
    )

    private fun layout1(statement: DSL, name: String): DSL {
        val relaxedStatement = statement.relaxTo(`L-sep` / `L}`, "malformed $name")
        return `L{` + (statement + !+(`L-sep` + relaxedStatement).heal) + `L}`
    }

    private fun layout(statement: DSL, name: String): DSL =
        generalLayout(statement, `L{`, `L-sep`, `L}`, name)

    private fun recordLayout(statement: DSL, name: String): DSL =
        generalLayout(statement, LCURLY.dsl, `,`, RCURLY.dsl, name)


    private fun generalLayout(dsl: DSL, left: DSL, sep: DSL, right: DSL, name: String)
            : DSL {
        val stop = sep / right
        val relaxedDsl = dsl.relaxTo(stop, "malformed $name")
        val relaxedSep = sep / (sep.relaxTo(stop, "malformed $name separator") + sep)
        val relaxedRight = right / (right.relaxTo(right, "malformed end of $name") + right)
        return left + right / (relaxedDsl.sepBy(relaxedSep) + relaxedRight)
    }

    private fun recordLayout1(statement: DSL, name: String): DSL {
        val relaxedStatement = statement.relaxTo(`,` / RCURLY, "malformed $name")
        return LCURLY + (statement + !+(`,` + relaxedStatement).heal).heal + RCURLY
    }

    private val letBinding = Choice.of(
        valueDeclarationGroup(),
        typeDeclaration.heal,
        (binder1 + eq + exprWhere).heal,
        (ident + !+binderAtom + guardedDecl).heal
    )
    private val letIn = Let(`'let'` + layout1(letBinding, "let binding") + `'in'` + expr)
    private val doStatement = Choice.of(
        DoNotationLet(`'let'` + layout1(letBinding, "let binding")),
        DoNotationBind(binder + larrow + expr.relax("malformed expression")).heal,
        DoNotationValue(expr).heal
    )
    private val doBlock = DoBlock(
        qualified(`'do'`).heal +
                layout1(doStatement, "do statement").relax("missing do statement")
    )
    private val adoBlock = `'ado'` + layout(doStatement, "ado statement")
    private val recordBinder = ((label + eq / ":").heal + binder) / VarBinder(label)
}
