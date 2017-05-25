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



}



