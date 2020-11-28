package net.kenro.ji.jin.purescript.parser;


public class PSLanguageParserTest extends PSLanguageParserTestBase {

    // modules
    public void testmodule1() { doTestOld(true, true); }
    public void testmodule2() { doTestOld(true, true); }
    public void testmodule_export1() { doTestOld(true, true); }

    // imports
    public void testimport1() { doTestOld(true, true); }
    public void testimport2() { doTestOld(true, true); }

    // declarations
    public void testdeclarations() { doTestOld(true, true); }

    // data declaration
    public void testdata_declaration1() { doTestOld(true, true); }
    public void testdata_declaration2() { doTestOld(true, true); }
    public void testdata_declaration3() { doTestOld(true, true); }
    public void testdata_declaration4() { doTestOld(true, true); }
    public void testdata_declaration5() { doTestOld(true, true); }
    public void testdata_declaration6() { doTestOld(true, true); }
    public void testdata_declaration7() { doTestOld(true, true); }
    public void testdata_declaration8() { doTestOld(true, true); }
    public void testdata_declaration9() { doTestOld(true, true); }
    public void testdata_declaration10() { doTestOld(true, true); }
    public void testdata_declaration11() { doTestOld(true, true); }
    public void testdata_declaration12() { doTestOld(true, true); }

    // type declaration
    public void testtype_declaration1() { doTestOld(true, true); }
    public void testtype_declaration2() { doTestOld(true, true); }
    public void testtype_declaration3() { doTestOld(true, true); }
    public void testtype_declaration4() { doTestOld(true, true); }
    public void testtype_declaration5() { doTestOld(true, true); }
    public void testtype_declaration6() { doTestOld(true, true); }
    public void testtype_declaration7() { doTestOld(true, true); }
    public void testtype_declaration8() { doTestOld(true, true); }
    public void testtype_declaration9() { doTestOld(true, true); }
    public void testtype_declaration10() { doTestOld(true, true); }

    // newtype declaration
    public void testnewtype_declaration1() { doTestOld(true, true); }
    public void testnewtype_declaration2() { doTestOld(true, true); }
    public void testnewtype_declaration3() { doTestOld(true, true); }
    public void testnewtype_declaration4() { doTestOld(true, true); }
    public void testnewtype_declaration5() { doTestOld(true, true); }
    public void testnewtype_declaration6() { doTestOld(true, true); }
    public void testnewtype_declaration7() { doTestOld(true, true); }
    public void testnewtype_declaration8() { doTestOld(true, true); }
    public void testnewtype_declaration9() { doTestOld(true, true); }
    public void testnewtype_declaration10() { doTestOld(true, true); }

    // fixes for purescript examples failures
    public void test1570() { doTestOld(true, true); }
    public void test2049_fixity() { doTestOld(true, true); }
    public void test2288() { doTestOld(true, true); }
    public void test2609() { doTestOld(true, true); }
    public void test2616() { doTestOld(true, true); }
    public void test2695() { doTestOld(true, true); }
    public void test2626() { doTestOld(true, true); }
    public void testAutoPrelude2() { doTestOld(true, true); }
    public void testCaseInputWildcard() { doTestOld(true, true); }
    public void testCaseMultipleExpressions() { doTestOld(true, true); }
    public void testClassRefSyntax() { doTestOld(true, true); }
    public void testDctorName() { doTestOld(true, true); }
    public void testExportedInstanceDeclarations() { doTestOld(true, true); }
    public void testImportHiding() { doTestOld(true, true); }
    public void testPolyLabels() { doTestOld(true, true); }
    public void testQualifiedQualifiedImports() { doTestOld(true, true); }
    public void testDollar() { doTestOld(true, true); }
    public void testConstraintParens() { doTestOld(true, true); }
    public void testConstraintParsingIssue() { doTestOld(true, true); }
    public void testDerivingFunctor() { doTestOld(true, true); }
    public void testFunctionalDependencies() { doTestOld(true, true); }
    public void testGenericsRep() { doTestOld(true, true); }
    public void testIfWildcard() { doTestOld(true, true); }
    public void testMPTCs() { doTestOld(true, true); }
    public void testMonadState() { doTestOld(true, true); }
    public void testNewtypeClass() { doTestOld(true, true); }
    public void testOperatorAlias() { doTestOld(true, true); }
    public void testOperatorAliasElsewhere() { doTestOld(true, true); }
    public void testOperators() { doTestOld(true, true); }
    public void testRebindableSyntax() { doTestOld(true, true); }
    public void testRowInInstanceHeadDetermined() { doTestOld(true, true); }
    public void testRowPolyInstanceContext() { doTestOld(true, true); }
    public void testRowsInInstanceContext() { doTestOld(true, true); }
    public void testSuperclasses3() { doTestOld(true, true); }
    public void testUnicodeType() { doTestOld(true, true); }
    public void testUntupledConstraints() { doTestOld(true, true); }
    public void testUsableTypeClassMethods() { doTestOld(true, true); }
    public void testWildcardInInstance() { doTestOld(true, true); }
    public void testTypeClasses() { doTestOld(true, true); }
    public void testTypedBinders() { doTestOld(true, true); }
    public void testUnicodeOperators() { doTestOld(true, true); }
    public void testDctorOperatorAlias() { doTestOld(true, true); }
    public void testLetPattern() { doTestOld(true, true); }
    public void testTypeOperators() { doTestOld(true, true); }
    public void testTailCall() { doTestOld(true, true); }
    public void testForeignKind() { doTestOld(true, true); }
    public void testStream() { doTestOld(true, true); }
    public void testSolvingAppendSymbol() { doTestOld(true, true); }
    public void testSolvingCompareSymbol() { doTestOld(true, true); }
    public void test2663() { doTestOld(true, true); }
    public void test2378() { doTestOld(true, true); }
    public void test2049_named_pattern_matching() { doTestOld(true, true); }
    public void testDuplicateProperties() { doTestOld(true, true); }
    public void testExtendedInfixOperators() { doTestOld(true, true); }
    public void testFieldPuns() { doTestOld(true, true); }
    public void testFieldConsPuns() { doTestOld(true, true); }
    public void testFunWithFunDeps() { doTestOld(true, true); }
    public void testRowUnion() { doTestOld(true, true); }
    public void testKindedType() { doTestOld(true, true); }
    public void testMutRec2() { doTestOld(true, true); }
    public void testMutRec3() { doTestOld(true, true); }
    public void testNewtypeInstance() { doTestOld(true, true); }
    public void testIntAndChar() { doTestOld(true, true); }
    public void testNestedRecordUpdate() { doTestOld(true, true); }
    public void testNestedRecordUpdateWildcards() { doTestOld(true, true); }
    public void testPrimedTypeName() { doTestOld(true, true); }
    public void testRowConstructors() { doTestOld(true, true); }
    public void testGuards() { doTestOld(true, true); }
    public void testDiffKindsSameName() { doTestOld(true, true); }
    public void testProgrammableTypeErrors() { doTestOld(true, true); }
    public void testSuggestComposition() { doTestOld(true, true); }
    public void testTypedHole() { doTestOld(true, true); }
    public void testShadowedNameParens() { doTestOld(true, true); }
    public void testTypeLevelString() { doTestOld(true, true); }

    // Bugs
    public void testbug_do_block1() { doTestOld(true, true); }
    public void testbug_do_block2() { doTestOld(true, true); }
    public void testbug_functions1() { doTestOld(true, true); }
    public void testbug_functions2() { doTestOld(true, true); }
    public void testbug_functions3() { doTestOld(true, true); }
    public void testbug_functions4() { doTestOld(true, true); }
    public void testbug_functions5() { doTestOld(true, true); }
    public void testbug_instance1() { doTestOld(true, true); }
    public void testbug_newtype1() { doTestOld(true, true); }
    public void testbug_newtype2() { doTestOld(true, true); }
    public void testbug_syntax_sugar1() { doTestOld(true, true); }
    public void testbug_syntax_sugar3() { doTestOld(true, true); }
    public void testbug_syntax_sugar4() { doTestOld(true, true); }
    public void testbug_syntax_sugar2() { doTestOld(true, true); }
    public void testbug_functions6() { doTestOld(true, true); }
    public void testbug_abs1() { doTestOld(true, true); }
    public void testbug_import1() { doTestOld(true, true); }
//    public void testbug_function7() { doTest(true, true); }
//    public void testbug_function8() { doTest(true, true); }
    //    public void testbug_hilighting1() { doTest(true, true); }
















}



