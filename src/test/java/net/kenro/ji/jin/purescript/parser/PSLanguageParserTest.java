package net.kenro.ji.jin.purescript.parser;


public class PSLanguageParserTest extends PSLanguageParserTestBase {

    public PSLanguageParserTest() {
        super("parser", "d", new PSParserDefinition());
    }

    // modules
    public void testmodule1() { doTest(true, true); }
    public void testmodule2() { doTest(true, true); }
    public void testmodule_exports1() { doTest(true, true); }
    public void testmodule_deprecated2() { doTest(true, true); }
    public void testimport() { doTest(true, true); }
    public void testmodule_scope() { doTest(true, true); }




}



