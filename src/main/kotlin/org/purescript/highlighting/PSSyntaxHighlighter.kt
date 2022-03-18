package org.purescript.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.purescript.lexer.PSHighlightLexer
import org.purescript.parser.ModuleName
import org.purescript.parser.PSTokens

class PSSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        private val keys: MutableMap<IElementType, TextAttributesKey> = HashMap()
        val LINE_COMMENT = createKey(
            "PS_LINE_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )
        val BLOCK_COMMENT = createKey(
            "PS_BLOCK_COMMENT",
            DefaultLanguageHighlighterColors.BLOCK_COMMENT
        )
        val KEYWORD =
            createKey("PS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING =
            createKey("PS_STRING", DefaultLanguageHighlighterColors.STRING)
        val STRING_GAP = TextAttributesKey.createTextAttributesKey(
            "PS_STRING_GAP",
            STRING
        )
        val NUMBER =
            createKey("PS_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val PS_BRACKETS =
            createKey("PS_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val PS_BRACES =
            createKey("PS_BRACES", DefaultLanguageHighlighterColors.BRACKETS)
        val PS_PARENTHESIS = createKey(
            "PS_PARENTHESIS",
            DefaultLanguageHighlighterColors.BRACKETS
        )
        val OPERATOR = createKey(
            "PS_OPERATOR",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )
        val VARIABLE = createKey(
            "PS_VARIABLE",
            DefaultLanguageHighlighterColors.INSTANCE_FIELD
        )
        val METHOD_DECLARATION = createKey(
            "PS_METHOD_DECLARATION",
            DefaultLanguageHighlighterColors.FUNCTION_CALL
        )
        val PS_EQ =
            createKey("PS_EQ", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val PS_COMMA =
            createKey("PS_COMMA", DefaultLanguageHighlighterColors.COMMA)
        val PS_DOT =
            createKey("PS_DOT", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val PS_DCOLON = createKey(
            "PS_DCOLON",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )
        val PS_ARROW = createKey(
            "PS_ARROW",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )

        // annotation highlighting
        // 'log' in 'import Control.Monad.Eff.Console (log)'
        val IMPORT_REF = createKey(
            "PS_IMPORT_REF",
            DefaultLanguageHighlighterColors.LOCAL_VARIABLE
        )

        // 'String' in 'foo :: String -> String'
        val TYPE_NAME = createKey(
            "PS_TYPE_NAME",
            DefaultLanguageHighlighterColors.METADATA
        )

        // 'foo' in 'foo :: String -> String'
        val TYPE_ANNOTATION_NAME = createKey(
            "PS_TYPE_ANNOTATION_NAME",
            DefaultLanguageHighlighterColors.METADATA
        )

        // 'a' in 'foo:: forall a. a -> a -> String'
        val TYPE_VARIABLE = createKey(
            "PS_TYPE_VARIABLE",
            DefaultLanguageHighlighterColors.METADATA
        )

        private fun createKey(
            externalName: String,
            fallbackAttrs: TextAttributesKey
        ): TextAttributesKey {
            return TextAttributesKey.createTextAttributesKey(
                externalName,
                fallbackAttrs
            )
        }

        init {
            fillMap(keys, TokenSet.create(PSTokens.SLCOMMENT), LINE_COMMENT)
            fillMap(keys, TokenSet.create(PSTokens.MLCOMMENT), BLOCK_COMMENT)
            fillMap(keys, PSTokens.kKeywords, KEYWORD)
            fillMap(keys, TokenSet.create(PSTokens.NATURAL), NUMBER)
            fillMap(keys, PSTokens.kStrings, STRING)
            fillMap(
                keys,
                TokenSet.create(PSTokens.LPAREN, PSTokens.RPAREN),
                PS_PARENTHESIS
            )
            fillMap(
                keys,
                TokenSet.create(PSTokens.LBRACK, PSTokens.RBRACK),
                PS_BRACKETS
            )
            fillMap(
                keys,
                TokenSet.create(PSTokens.LCURLY, PSTokens.RCURLY),
                PS_BRACES
            )
            fillMap(keys, PSTokens.kOperators, OPERATOR)
            fillMap(
                keys,
                TokenSet.create(PSTokens.IDENT, PSTokens.OPERATOR),
                VARIABLE
            )
            fillMap(
                keys,
                TokenSet.create(PSTokens.PROPER_NAME),
                METHOD_DECLARATION
            )
            fillMap(keys, TokenSet.create(ModuleName), TYPE_NAME)
            fillMap(keys, TokenSet.create(PSTokens.STRING_ESCAPED), KEYWORD)
            fillMap(keys, TokenSet.create(PSTokens.STRING_GAP), STRING_GAP)
            fillMap(
                keys,
                TokenSet.create(PSTokens.STRING_ERROR),
                CodeInsightColors.ERRORS_ATTRIBUTES
            )
            fillMap(
                keys,
                TokenSet.create(PSTokens.ERROR),
                CodeInsightColors.ERRORS_ATTRIBUTES
            )
            keys[PSTokens.EQ] = PS_EQ
            keys[PSTokens.COMMA] = PS_COMMA
            keys[PSTokens.DOT] = PS_DOT
            keys[PSTokens.DCOLON] = PS_DCOLON
            keys[PSTokens.ARROW] = PS_ARROW
            keys[PSTokens.LARROW] = PS_ARROW
            keys[PSTokens.FLOAT] = NUMBER
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return PSHighlightLexer()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return pack(keys[tokenType])
    }
}
