package net.kenro.ji.jin.purescript.parser;


public class PSLanguageParserTest extends PSLanguageParserTestBase {

    public PSLanguageParserTest() {
        super("parser", "purs", new PSParserDefinition());
    }

    // modules
    public void testmodule1() { doTestThatMaybyShouldPass(true, true); }
    public void testmodule2() { doTestThatMaybyShouldPass(true, true); }
    public void testmodule_export1() { doTestThatMaybyShouldPass(true, true); }

    // imports
    public void testimport1() { doTestThatMaybyShouldPass(true, true); }
    public void testimport2() { doTestThatMaybyShouldPass(true, true); }

    // declarations
    public void testdeclarations() { doTestThatMaybyShouldPass(true, true); }

    // data declaration
    public void testdata_declaration1() { doTestThatMaybyShouldPass(true, true); }
    public void testdata_declaration2() { doTestThatMaybyShouldPass(true, true); }
    public void testdata_declaration3() { doTestThatMaybyShouldPass(true, true); }
    public void testdata_declaration4() { doTestThatMaybyShouldPass(true, true); }
    public void testdata_declaration5() { doTestThatMaybyShouldPass(true, true); }
    public void testdata_declaration6() { doTestThatMaybyShouldPass(true, true); }
    public void testdata_declaration7() { doTestThatMaybyShouldPass(true, true); }
    public void testdata_declaration8() { doTestThatMaybyShouldPass(true, true); }
    public void testdata_declaration9() { doTestThatMaybyShouldPass(true, true); }
    public void testdata_declaration10() { doTestThatMaybyShouldPass(true, true); }
    public void testdata_declaration11() { doTestThatMaybyShouldPass(true, true); }
    public void testdata_declaration12() { doTestThatMaybyShouldPass(true, true); }

    // type declaration
    public void testtype_declaration1() { doTestThatMaybyShouldPass(true, true); }
    public void testtype_declaration2() { doTestThatMaybyShouldPass(true, true); }
    public void testtype_declaration3() { doTestThatMaybyShouldPass(true, true); }
    public void testtype_declaration4() { doTestThatMaybyShouldPass(true, true); }
    public void testtype_declaration5() { doTestThatMaybyShouldPass(true, true); }
    public void testtype_declaration6() { doTestThatMaybyShouldPass(true, true); }
    public void testtype_declaration7() { doTestThatMaybyShouldPass(true, true); }
    public void testtype_declaration8() { doTestThatMaybyShouldPass(true, true); }
    public void testtype_declaration9() { doTestThatMaybyShouldPass(true, true); }
    public void testtype_declaration10() { doTestThatMaybyShouldPass(true, true); }

    // newtype declaration
    public void testnewtype_declaration1() { doTestThatMaybyShouldPass(true, true); }
    public void testnewtype_declaration2() { doTestThatMaybyShouldPass(true, true); }
    public void testnewtype_declaration3() { doTestThatMaybyShouldPass(true, true); }
    public void testnewtype_declaration4() { doTestThatMaybyShouldPass(true, true); }
    public void testnewtype_declaration5() { doTestThatMaybyShouldPass(true, true); }
    public void testnewtype_declaration6() { doTestThatMaybyShouldPass(true, true); }
    public void testnewtype_declaration7() { doTestThatMaybyShouldPass(true, true); }
    public void testnewtype_declaration8() { doTestThatMaybyShouldPass(true, true); }
    public void testnewtype_declaration9() { doTestThatMaybyShouldPass(true, true); }
    public void testnewtype_declaration10() { doTestThatMaybyShouldPass(true, true); }

    // fixes for purescript examples failures
    public void test1570() { doTestThatMaybyShouldPass(true, true); }
    public void test2049_fixity() { doTestThatMaybyShouldPass(true, true); }
    public void test2288() { doTestThatMaybyShouldPass(true, true); }
    public void test2609() { doTestThatMaybyShouldPass(true, true); }
    public void test2616() { doTestThatMaybyShouldPass(true, true); }
    public void test2695() { doTestThatMaybyShouldPass(true, true); }
    public void test2626() { doTestThatMaybyShouldPass(true, true); }
    public void testAutoPrelude2() { doTestThatMaybyShouldPass(true, true); }
    public void testCaseInputWildcard() { doTestThatMaybyShouldPass(true, true); }
    public void testCaseMultipleExpressions() { doTestThatMaybyShouldPass(true, true); }
    public void testClassRefSyntax() { doTestThatMaybyShouldPass(true, true); }
    public void xtestDctorName() { doTestThatMaybyShouldPass(true, true); }
    public void testExportedInstanceDeclarations() { doTestThatMaybyShouldPass(true, true); }
    public void testImportHiding() { doTestThatMaybyShouldPass(true, true); }
    public void testPolyLabels() { doTestThatMaybyShouldPass(true, true); }
    public void testQualifiedQualifiedImports() { doTestThatMaybyShouldPass(true, true); }
    public void testDollar() { doTestThatMaybyShouldPass(true, true); }
    public void xtestConstraintParens() { doTestThatMaybyShouldPass(true, true); }
    public void testConstraintParsingIssue() { doTestThatMaybyShouldPass(true, true); }
    public void testDerivingFunctor() { doTestThatMaybyShouldPass(true, true); }
    public void testFunctionalDependencies() { doTestThatMaybyShouldPass(true, true); }
    public void testGenericsRep() { doTestThatMaybyShouldPass(true, true); }
    public void xtestIfWildcard() { doTestThatMaybyShouldPass(true, true); }
    public void testMPTCs() { doTestThatMaybyShouldPass(true, true); }
    public void testMonadState() { doTestThatMaybyShouldPass(true, true); }
    public void testNewtypeClass() { doTestThatMaybyShouldPass(true, true); }
    public void testOperatorAlias() { doTestThatMaybyShouldPass(true, true); }
    public void testOperatorAliasElsewhere() { doTestThatMaybyShouldPass(true, true); }
    public void testOperators() { doTestThatMaybyShouldPass(true, true); }
    public void testRebindableSyntax() { doTestThatMaybyShouldPass(true, true); }
    public void testRowInInstanceHeadDetermined() { doTestThatMaybyShouldPass(true, true); }
    public void testRowPolyInstanceContext() { doTestThatMaybyShouldPass(true, true); }
    public void testRowsInInstanceContext() { doTestThatMaybyShouldPass(true, true); }
    public void testSuperclasses3() { doTestThatMaybyShouldPass(true, true); }
    public void xtestUnicodeType() { doTestThatMaybyShouldPass(true, true); }
    public void testUntupledConstraints() { doTestThatMaybyShouldPass(true, true); }
    public void testUsableTypeClassMethods() { doTestThatMaybyShouldPass(true, true); }
    public void testWildcardInInstance() { doTestThatMaybyShouldPass(true, true); }
    public void testTypeClasses() { doTestThatMaybyShouldPass(true, true); }
    public void testTypedBinders() { doTestThatMaybyShouldPass(true, true); }
    public void testUnicodeOperators() { doTestThatMaybyShouldPass(true, true); }
    public void xtestDctorOperatorAlias() { doTestThatMaybyShouldPass(true, true); }
    public void testLetPattern() { doTestThatMaybyShouldPass(true, true); }
    public void xtestTypeOperators() { doTestThatMaybyShouldPass(true, true); }
    public void testTailCall() { doTestThatMaybyShouldPass(true, true); }
    public void testForeignKind() { doTestThatMaybyShouldPass(true, true); }
    public void testStream() { doTestThatMaybyShouldPass(true, true); }
    public void testSolvingAppendSymbol() { doTestThatMaybyShouldPass(true, true); }
    public void testSolvingCompareSymbol() { doTestThatMaybyShouldPass(true, true); }
    public void test2663() { doTestThatMaybyShouldPass(true, true); }
    public void test2378() { doTestThatMaybyShouldPass(true, true); }
    public void test2049_named_pattern_matching() { doTestThatMaybyShouldPass(true, true); }
    public void testDuplicateProperties() { doTestThatMaybyShouldPass(true, true); }
    public void testExtendedInfixOperators() { doTestThatMaybyShouldPass(true, true); }
    public void testFieldPuns() { doTestThatMaybyShouldPass(true, true); }
    public void testFieldConsPuns() { doTestThatMaybyShouldPass(true, true); }
    public void testFunWithFunDeps() { doTestThatMaybyShouldPass(true, true); }
    public void testRowUnion() { doTestThatMaybyShouldPass(true, true); }
    public void testKindedType() { doTestThatMaybyShouldPass(true, true); }
    public void testMutRec2() { doTestThatMaybyShouldPass(true, true); }
    public void testMutRec3() { doTestThatMaybyShouldPass(true, true); }
    public void testNewtypeInstance() { doTestThatMaybyShouldPass(true, true); }
    public void testIntAndChar() { doTestThatMaybyShouldPass(true, true); }
    public void testNestedRecordUpdate() { doTestThatMaybyShouldPass(true, true); }
    public void testNestedRecordUpdateWildcards() { doTestThatMaybyShouldPass(true, true); }
    public void xtestPrimedTypeName() { doTestThatMaybyShouldPass(true, true); }
    public void testRowConstructors() { doTestThatMaybyShouldPass(true, true); }
    public void testGuards() { doTestThatMaybyShouldPass(true, true); }
    public void testDiffKindsSameName() { doTestThatMaybyShouldPass(true, true); }
    public void testProgrammableTypeErrors() { doTestThatMaybyShouldPass(true, true); }
    public void testSuggestComposition() { doTestThatMaybyShouldPass(true, true); }
    public void testTypedHole() { doTestThatMaybyShouldPass(true, true); }
    public void testShadowedNameParens() { doTestThatMaybyShouldPass(true, true); }
    public void testTypeLevelString() { doTestThatMaybyShouldPass(true, true); }

    // Bugs
    public void testbug_do_block1() { doTestThatMaybyShouldPass(true, true); }
    public void xtestbug_do_block2() { doTestThatMaybyShouldPass(true, true); }
    public void testbug_functions1() { doTestThatMaybyShouldPass(true, true); }
    public void xtestbug_functions2() { doTestThatMaybyShouldPass(true, true); }
    public void xtestbug_functions3() { doTestThatMaybyShouldPass(true, true); }
    public void xtestbug_functions4() { doTestThatMaybyShouldPass(true, true); }
    public void xtestbug_functions5() { doTestThatMaybyShouldPass(true, true); }
    public void xtestbug_instance1() { doTestThatMaybyShouldPass(true, true); }
    public void testbug_newtype1() { doTestThatMaybyShouldPass(true, true); }
    public void testbug_newtype2() { doTestThatMaybyShouldPass(true, true); }
    public void testbug_syntax_sugar1() { doTestThatMaybyShouldPass(true, true); }
    public void testbug_syntax_sugar3() { doTestThatMaybyShouldPass(true, true); }
    public void xtestbug_syntax_sugar4() { doTestThatMaybyShouldPass(true, true); }
    public void testbug_syntax_sugar2() { doTestThatMaybyShouldPass(true, true); }
    public void xtestbug_functions6() { doTestThatMaybyShouldPass(true, true); }
    public void testbug_abs1() { doTestThatMaybyShouldPass(true, true); }
    public void testbug_import1() { doTestThatMaybyShouldPass(true, true); }
//    public void testbug_function7() { doTest(true, true); }
//    public void testbug_function8() { doTest(true, true); }
    //    public void testbug_hilighting1() { doTest(true, true); }
















}



