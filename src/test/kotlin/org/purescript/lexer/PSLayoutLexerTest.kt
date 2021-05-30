package org.purescript.lexer

class PSLayoutLexerTest: PSLexerTestBase("layout") {
    fun testSimple() = doFileTest("purs")

    // modules
    fun testmodule1() = doFileTest("purs")
    fun testmodule2() = doFileTest("purs")
    fun testmodule_export1() = doFileTest("purs")

    fun testSnuglyCaseWithWhere() = doFileTest("purs")

    fun testLarger() = doFileTest("purs")
    fun testClassMemberWithComment() = doFileTest("purs")
}