package org.purescript.highlighting

import com.intellij.openapi.editor.colors.CodeInsightColors.ERRORS_ATTRIBUTES
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType.BAD_CHARACTER
import com.intellij.psi.tree.IElementType
import org.purescript.lexer.PSHighlightLexer
import org.purescript.parser.*
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Default

object PSSyntaxHighlighter : SyntaxHighlighterBase() {
    private val keys: MutableMap<IElementType, TextAttributesKey> = HashMap()
    val LINE_COMMENT = createKey("PS_LINE_COMMENT", Default.LINE_COMMENT)
    val BLOCK_COMMENT = createKey("PS_BLOCK_COMMENT", Default.BLOCK_COMMENT)
    val KEYWORD = createKey("PS_KEYWORD", Default.KEYWORD)
    val STRING = createKey("PS_STRING", Default.STRING)
    val STRING_GAP = createKey("PS_STRING_GAP", STRING)
    val NUMBER = createKey("PS_NUMBER", Default.NUMBER)
    val PS_BRACKETS = createKey("PS_BRACKETS", Default.BRACKETS)
    val PS_BRACES = createKey("PS_BRACES", Default.BRACES)
    val PS_PARENTHESIS = createKey("PS_PARENTHESIS", Default.PARENTHESES)
    val OPERATOR = createKey("PS_OPERATOR", Default.OPERATION_SIGN)
    val IDENTIFIER = createKey("PS_IDENTIFIER", Default.IDENTIFIER)
    val FUNCTION_CALL = createKey("PS_FUNCTION_CALL", Default.FUNCTION_CALL)
    val FUNCTION_DECLARATION = createKey("PS_FUNCTION_DECLARATION", Default.FUNCTION_DECLARATION)
    val PARAMETER = createKey("PS_PARAMETER", Default.PARAMETER)
    val PS_EQ = createKey("PS_EQ", Default.OPERATION_SIGN)
    val PS_COMMA = createKey("PS_COMMA", Default.COMMA)
    val PS_DOT = createKey("PS_DOT", Default.OPERATION_SIGN)
    val PS_DCOLON = createKey("PS_DCOLON", Default.OPERATION_SIGN)
    val PS_ARROW = createKey("PS_ARROW", Default.OPERATION_SIGN)

    // annotation highlighting
    // 'log' in 'import Control.Monad.Eff.Console (log)'
    val IMPORT_REF = createKey("PS_IMPORT_REF", Default.LOCAL_VARIABLE)

    // 'String' in 'foo :: String -> String'
    val TYPE_NAME = createKey("PS_TYPE_NAME", Default.METADATA)

    // 'foo' in 'foo :: String -> String'
    val TYPE_ANNOTATION_NAME =
        createKey("PS_TYPE_ANNOTATION_NAME", Default.METADATA)

    // 'a' in 'foo:: forall a. a -> a -> String'
    val TYPE_VARIABLE = createKey("PS_TYPE_VARIABLE", Default.METADATA)
    private fun createKey(externalName: String, fallbackAttrs: TextAttributesKey) =
        createTextAttributesKey(externalName, fallbackAttrs)

    init {
        fillMap(keys, LINE_COMMENT, SLCOMMENT)
        fillMap(keys, BLOCK_COMMENT, MLCOMMENT)
        fillMap(keys, kKeywords, KEYWORD)
        fillMap(keys, NUMBER, NATURAL)
        fillMap(keys, kStrings, STRING)
        fillMap(keys, PS_PARENTHESIS, LPAREN, RPAREN)
        fillMap(keys, PS_BRACKETS, LBRACK, RBRACK)
        fillMap(keys, PS_BRACES, LCURLY, RCURLY)
        fillMap(keys, kOperators, OPERATOR)
        fillMap(keys, IDENTIFIER, LOWER, ModuleName)
        fillMap(keys, KEYWORD, STRING_ESCAPED)
        fillMap(keys, STRING_GAP, org.purescript.parser.STRING_GAP)
        fillMap(keys, ERRORS_ATTRIBUTES, STRING_ERROR, BAD_CHARACTER)
        keys[EQ] = PS_EQ
        keys[COMMA] = PS_COMMA
        keys[DOT] = PS_DOT
        keys[DCOLON] = PS_DCOLON
        keys[ARROW] = PS_ARROW
        keys[LARROW] = PS_ARROW
        keys[FLOAT] = NUMBER
    }

    override fun getHighlightingLexer() = PSHighlightLexer()
    override fun getTokenHighlights(token: IElementType) = pack(keys[token])
}
