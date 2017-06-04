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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

import net.kenro.ji.jin.purescript.lexer.PSHighlightLexer;
import net.kenro.ji.jin.purescript.psi.PSElements;
import net.kenro.ji.jin.purescript.psi.PSTokens;
      

public class PSSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<IElementType, TextAttributesKey>();

    public static final TextAttributesKey LINE_COMMENT = createKey("PS_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);

    public static final TextAttributesKey BLOCK_COMMENT = createKey("PS_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);

    public static final TextAttributesKey KEYWORD = createKey("PS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey STRING = createKey("PS_STRING", DefaultLanguageHighlighterColors.STRING);

    private static final TextAttributes STRING_GAP_ATTR;

    static {
        STRING_GAP_ATTR = STRING.getDefaultAttributes().clone();
        STRING_GAP_ATTR.setForegroundColor(JBColor.GRAY);
    }

    public static final TextAttributesKey STRING_GAP = createTextAttributesKey("PS_STRING_GAP", STRING_GAP_ATTR);

    public static final TextAttributesKey NUMBER = createKey("PS_NUMBER", DefaultLanguageHighlighterColors.NUMBER);

    public static final TextAttributesKey BRACKET = createKey("PS_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);

    public static final TextAttributesKey OPERATOR = createKey("PS_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey TYPE_NAME = createKey("PS_TYPE_NAME", CodeInsightColors.ANNOTATION_NAME_ATTRIBUTES);

    public static final TextAttributesKey VARIABLE = createKey("PS_VARIABLE", CodeInsightColors.INSTANCE_FIELD_ATTRIBUTES);

    public static final TextAttributesKey MODULE_NAME = createKey("PS_MODULE_NAME", CodeInsightColors.ANNOTATION_NAME_ATTRIBUTES);

    public static final TextAttributesKey METHOD_DECLARATION = createKey("PS_METHOD_DECLARATION", CodeInsightColors.METHOD_CALL_ATTRIBUTES);

    static {
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.SLCOMMENT), LINE_COMMENT);
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.MLCOMMENT), BLOCK_COMMENT);
        fillMap(ATTRIBUTES, PSTokens.kKeywords, KEYWORD);
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.NATURAL), NUMBER);
        fillMap(ATTRIBUTES, PSTokens.kStrings, STRING);
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.LPAREN, PSTokens.RPAREN), BRACKET);
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.LBRACK, PSTokens.RBRACK), BRACKET);
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.LCURLY, PSTokens.RCURLY), BRACKET);
        fillMap(ATTRIBUTES, PSTokens.kOperators, OPERATOR);
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.IDENT, PSTokens.OPERATOR), VARIABLE);
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.PROPER_NAME), METHOD_DECLARATION);
        fillMap(ATTRIBUTES, TokenSet.create(PSElements.pModuleName), TYPE_NAME);
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.STRING_ESCAPED), KEYWORD);
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.STRING_GAP), STRING_GAP);
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.STRING_ERROR), CodeInsightColors.ERRORS_ATTRIBUTES);
        fillMap(ATTRIBUTES, TokenSet.create(PSTokens.ERROR), CodeInsightColors.ERRORS_ATTRIBUTES);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new PSHighlightLexer();
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTES.get(tokenType));
    }

    private static TextAttributesKey createKey(String externalName, TextAttributesKey fallbackAttrs) {
        return createTextAttributesKey(externalName, fallbackAttrs);
    }
}
