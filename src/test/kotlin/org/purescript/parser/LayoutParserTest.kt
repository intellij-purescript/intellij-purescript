package org.purescript.parser

class LayoutParserTest: PSLanguageParserTestBase("layout") {
    // modules
    fun testmodule1() = doTest(true, true)
    fun testmodule2() = doTest(true, true)
    fun testmodule_export1() = doTest(true, true)

    fun testSnuglyCaseWithWhere() = doTest(true, true)

    fun testLarger() = doTest(true, true)
    fun testClassMemberWithComment() = doTest(true, true)

}