package org.purescript.lexer

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase

abstract class PSLexerTestBase(private val folder: String) : LexerTestCase() {
    override fun createLexer(): Lexer = LayoutLexer(PSLexer())
    override fun getDirPath(): String = folder
    override fun getExpectedFileExtension(): String = ".lex.txt"
    override fun getPathToTestDataFile(extension: String): String =
        "test-data" + "/" + dirPath + "/" + super.getTestName(false) + extension
}
