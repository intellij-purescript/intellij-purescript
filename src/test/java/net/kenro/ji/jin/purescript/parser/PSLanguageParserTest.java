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
//    public void testdata_declaration5() { doTest(true, true); }
    public void testdata_declaration6() { doTest(true, true); }
    public void testdata_declaration7() { doTest(true, true); }
    public void testdata_declaration8() { doTest(true, true); }





}



