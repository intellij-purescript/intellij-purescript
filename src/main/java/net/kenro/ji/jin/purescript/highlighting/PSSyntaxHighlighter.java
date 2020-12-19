package net.kenro.ji.jin.purescript.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.ui.JBColor;
import net.kenro.ji.jin.purescript.lexer.PSHighlightLexer;
import net.kenro.ji.jin.purescript.psi.PSElements;
import net.kenro.ji.jin.purescript.psi.PSTokens;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;


public class PSSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> keys = new HashMap<IElementType, TextAttributesKey>();

    public static final TextAttributesKey LINE_COMMENT = createKey("PS_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);

    public static final TextAttributesKey BLOCK_COMMENT = createKey("PS_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);

    public static final TextAttributesKey KEYWORD = createKey("PS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey STRING = createKey("PS_STRING", DefaultLanguageHighlighterColors.STRING);


    public static final TextAttributesKey STRING_GAP = createTextAttributesKey(
        "PS_STRING_GAP",
        STRING
    );

    public static final TextAttributesKey NUMBER = createKey("PS_NUMBER", DefaultLanguageHighlighterColors.NUMBER);

    public static final TextAttributesKey PS_BRACKETS = createKey("PS_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey PS_BRACES = createKey("PS_BRACES", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey PS_PARENTHESIS = createKey("PS_PARENTHESIS", DefaultLanguageHighlighterColors.BRACKETS);

    public static final TextAttributesKey OPERATOR = createKey("PS_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey VARIABLE = createKey("PS_VARIABLE", CodeInsightColors.INSTANCE_FIELD_ATTRIBUTES);

    public static final TextAttributesKey MODULE_NAME = createKey("PS_MODULE_NAME", CodeInsightColors.ANNOTATION_NAME_ATTRIBUTES);

    public static final TextAttributesKey METHOD_DECLARATION = createKey("PS_METHOD_DECLARATION", CodeInsightColors.METHOD_CALL_ATTRIBUTES);

    public static final TextAttributesKey PS_EQ = createKey("PS_EQ", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey PS_COMMA = createKey("PS_COMMA", DefaultLanguageHighlighterColors.COMMA);

    public static final TextAttributesKey PS_DOT = createKey("PS_DOT", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey PS_DCOLON = createKey("PS_DCOLON", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey PS_ARROW = createKey("PS_ARROW", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey PS_UNDERSCORE = createKey("PS_UNDERSCORE", DefaultLanguageHighlighterColors.OPERATION_SIGN);


    // annotation highlighting

    // 'log' in 'import Control.Monad.Eff.Console (log)'
    public static final TextAttributesKey IMPORT_REF = createKey("PS_IMPORT_REF", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

    // 'String' in 'foo :: String -> String'
    public static final TextAttributesKey TYPE_NAME = createKey("PS_TYPE_NAME", CodeInsightColors.ANNOTATION_NAME_ATTRIBUTES);

    // 'foo' in 'foo :: String -> String'
    public static final TextAttributesKey TYPE_ANNOTATION_NAME = createKey("PS_TYPE_ANNOTATION_NAME", CodeInsightColors.ANNOTATION_NAME_ATTRIBUTES);

    // 'a' in 'foo:: forall a. a -> a -> String'
    public static final TextAttributesKey TYPE_VARIABLE = createKey("PS_TYPE_VARIABLE", CodeInsightColors.ANNOTATION_NAME_ATTRIBUTES);

    static {
        fillMap(keys, TokenSet.create(PSTokens.SLCOMMENT), LINE_COMMENT);
        fillMap(keys, TokenSet.create(PSTokens.MLCOMMENT), BLOCK_COMMENT);
        fillMap(keys, PSTokens.kKeywords, KEYWORD);
        fillMap(keys, TokenSet.create(PSTokens.NATURAL), NUMBER);
        fillMap(keys, PSTokens.kStrings, STRING);
        fillMap(keys, TokenSet.create(PSTokens.LPAREN, PSTokens.RPAREN), PS_PARENTHESIS);
        fillMap(keys, TokenSet.create(PSTokens.LBRACK, PSTokens.RBRACK), PS_BRACKETS);
        fillMap(keys, TokenSet.create(PSTokens.LCURLY, PSTokens.RCURLY), PS_BRACES);
        fillMap(keys, PSTokens.kOperators, OPERATOR);
        fillMap(keys, TokenSet.create(PSTokens.IDENT, PSTokens.OPERATOR), VARIABLE);
        fillMap(keys, TokenSet.create(PSTokens.PROPER_NAME), METHOD_DECLARATION);
        fillMap(keys, TokenSet.create(PSElements.pModuleName), TYPE_NAME);
        fillMap(keys, TokenSet.create(PSTokens.STRING_ESCAPED), KEYWORD);
        fillMap(keys, TokenSet.create(PSTokens.STRING_GAP), STRING_GAP);
        fillMap(keys, TokenSet.create(PSTokens.STRING_ERROR), CodeInsightColors.ERRORS_ATTRIBUTES);
        fillMap(keys, TokenSet.create(PSTokens.ERROR), CodeInsightColors.ERRORS_ATTRIBUTES);
        keys.put(PSTokens.EQ, PS_EQ);
        keys.put(PSTokens.COMMA, PS_COMMA);
        keys.put(PSTokens.DOT, PS_DOT);
        keys.put(PSTokens.DCOLON, PS_DCOLON);
        keys.put(PSTokens.ARROW, PS_ARROW);
        keys.put(PSTokens.LARROW, PS_ARROW);
        keys.put(PSTokens.FLOAT, NUMBER);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new PSHighlightLexer();
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(final IElementType tokenType) {
        return pack(keys.get(tokenType));
    }

    private static TextAttributesKey createKey(final String externalName, final TextAttributesKey fallbackAttrs) {
        return createTextAttributesKey(externalName, fallbackAttrs);
    }
}
