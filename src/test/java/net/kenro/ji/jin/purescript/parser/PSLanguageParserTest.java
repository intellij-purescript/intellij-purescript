package net.kenro.ji.jin.purescript.parser;


public class PSLanguageParserTest extends PSLanguageParserTestBase {

    public PSLanguageParserTest() {
        super("parser", "purs", new PSParserDefinition());
    }

    // modules
    public void testmodule1() { doTest(true, true); }
    public void testmodule2() { doTest(true, true); }
    public void testmodule_export1() { doTest(true, true); }
    public void testimport1() { doTest(true, true); }
    public void testimport2() { doTest(true, true); }





}



