package net.kenro.ji.jin.purescript.parser;


public class PSLanguageParserTest extends PSLanguageParserTestBase {

    public PSLanguageParserTest() {
        super("parser", "purs", new PSParserDefinition());
    }

    // modules
    public void testmodule1() { doTest(true, true); }
    public void testmodule2() { doTest(true, true); }
    public void testmodule_export1() { doTest(true, true); }

    // imports
    public void testimport1() { doTest(true, true); }
    public void testimport2() { doTest(true, true); }

    // declarations
    public void testdeclarations() { doTest(true, true); }

    // data declaration
    public void testdata_declaration1() { doTest(true, true); }
    public void testdata_declaration2() { doTest(true, true); }
    public void testdata_declaration3() { doTest(true, true); }
    public void testdata_declaration4() { doTest(true, true); }
    public void testdata_declaration5() { doTest(true, true); }
    public void testdata_declaration6() { doTest(true, true); }
    public void testdata_declaration7() { doTest(true, true); }
    public void testdata_declaration8() { doTest(true, true); }
    public void testdata_declaration9() { doTest(true, true); }
    public void testdata_declaration10() { doTest(true, true); }
    public void testdata_declaration11() { doTest(true, true); }
    public void testdata_declaration12() { doTest(true, true); }

    // type declaration
    public void testtype_declaration1() { doTest(true, true); }
    public void testtype_declaration2() { doTest(true, true); }
    public void testtype_declaration3() { doTest(true, true); }
    public void testtype_declaration4() { doTest(true, true); }
    public void testtype_declaration5() { doTest(true, true); }
    public void testtype_declaration6() { doTest(true, true); }
    public void testtype_declaration7() { doTest(true, true); }
    public void testtype_declaration8() { doTest(true, true); }
    public void testtype_declaration9() { doTest(true, true); }
    public void testtype_declaration10() { doTest(true, true); }

    // newtype declaration
    public void testnewtype_declaration1() { doTest(true, true); }
    public void testnewtype_declaration2() { doTest(true, true); }
    public void testnewtype_declaration3() { doTest(true, true); }
    public void testnewtype_declaration4() { doTest(true, true); }
    public void testnewtype_declaration5() { doTest(true, true); }
    public void testnewtype_declaration6() { doTest(true, true); }
    public void testnewtype_declaration7() { doTest(true, true); }
    public void testnewtype_declaration8() { doTest(true, true); }
    public void testnewtype_declaration9() { doTest(true, true); }
    public void testnewtype_declaration10() { doTest(true, true); }

    // fixes for purescript examples failures
    public void test1570() { doTest(true, true); }
    public void test2049_fixity() { doTest(true, true); }
    public void test2288() { doTest(true, true); }
    public void test2609() { doTest(true, true); }
    public void test2616() { doTest(true, true); }
    public void test2695() { doTest(true, true); }
    public void test2626() { doTest(true, true); }
    public void testAutoPrelude2() { doTest(true, true); }
    public void testCaseInputWildcard() { doTest(true, true); }
    public void testCaseMultipleExpressions() { doTest(true, true); }
    public void testClassRefSyntax() { doTest(true, true); }
    public void testDctorName() { doTest(true, true); }
    public void testExportedInstanceDeclarations() { doTest(true, true); }
    public void testImportHiding() { doTest(true, true); }
    public void testPolyLabels() { doTest(true, true); }
    public void testQualifiedQualifiedImports() { doTest(true, true); }
    public void testDollar() { doTest(true, true); }
    public void testConstraintParens() { doTest(true, true); }
    public void testConstraintParsingIssue() { doTest(true, true); }
    public void testDerivingFunctor() { doTest(true, true); }
    public void testFunctionalDependencies() { doTest(true, true); }
    public void testGenericsRep() { doTest(true, true); }
    public void testIfWildcard() { doTest(true, true); }
    public void testMPTCs() { doTest(true, true); }
    public void testMonadState() { doTest(true, true); }
    public void testNewtypeClass() { doTest(true, true); }
    public void testOperatorAlias() { doTest(true, true); }
    public void testOperatorAliasElsewhere() { doTest(true, true); }
    public void testOperators() { doTest(true, true); }
    public void testRebindableSyntax() { doTest(true, true); }
    public void testRowInInstanceHeadDetermined() { doTest(true, true); }
    public void testRowPolyInstanceContext() { doTest(true, true); }
    public void testRowsInInstanceContext() { doTest(true, true); }
    public void testSuperclasses3() { doTest(true, true); }
    public void testUnicodeType() { doTest(true, true); }
    public void testUntupledConstraints() { doTest(true, true); }
    public void testUsableTypeClassMethods() { doTest(true, true); }
    public void testWildcardInInstance() { doTest(true, true); }
    public void testTypeClasses() { doTest(true, true); }
    public void testTypedBinders() { doTest(true, true); }
    public void testUnicodeOperators() { doTest(true, true); }
    public void testDctorOperatorAlias() { doTest(true, true); }
    public void testLetPattern() { doTest(true, true); }
    public void testTypeOperators() { doTest(true, true); }
    public void testTailCall() { doTest(true, true); }
    public void testForeignKind() { doTest(true, true); }
    public void testStream() { doTest(true, true); }
    public void testSolvingAppendSymbol() { doTest(true, true); }
    public void testSolvingCompareSymbol() { doTest(true, true); }
    public void test2663() { doTest(true, true); }
    public void test2378() { doTest(true, true); }
    public void test2049_named_pattern_matching() { doTest(true, true); }
    public void testDuplicateProperties() { doTest(true, true); }
    public void testExtendedInfixOperators() { doTest(true, true); }
    public void testFieldPuns() { doTest(true, true); }
    public void testFieldConsPuns() { doTest(true, true); }
    public void testFunWithFunDeps() { doTest(true, true); }
    public void testRowUnion() { doTest(true, true); }
    public void testKindedType() { doTest(true, true); }
    public void testMutRec2() { doTest(true, true); }
    public void testMutRec3() { doTest(true, true); }
    public void testNewtypeInstance() { doTest(true, true); }
    public void testIntAndChar() { doTest(true, true); }
    public void testNestedRecordUpdate() { doTest(true, true); }
    public void testNestedRecordUpdateWildcards() { doTest(true, true); }
    public void testPrimedTypeName() { doTest(true, true); }
    public void testRowConstructors() { doTest(true, true); }
    public void testGuards() { doTest(true, true); }


















}



