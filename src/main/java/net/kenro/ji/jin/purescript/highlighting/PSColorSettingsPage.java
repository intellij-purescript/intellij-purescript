package net.kenro.ji.jin.purescript.highlighting;


import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import net.kenro.ji.jin.purescript.icons.PSIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static net.kenro.ji.jin.purescript.highlighting.PSSyntaxHighlighter.*;

public class PSColorSettingsPage implements ColorSettingsPage {

    @NonNls
    private static final Map<String, TextAttributesKey> TAG_HIGHLIGHTING_MAP = new HashMap<String, TextAttributesKey>();

    static {
        TAG_HIGHLIGHTING_MAP.put("import_ref", IMPORT_REF);
        TAG_HIGHLIGHTING_MAP.put("type_variable", TYPE_VARIABLE);
        TAG_HIGHLIGHTING_MAP.put("type_name", TYPE_NAME);
        TAG_HIGHLIGHTING_MAP.put("type_annotation_name", TYPE_ANNOTATION_NAME);
    }

    private static final AttributesDescriptor[] ATTRIBS = {
            new AttributesDescriptor("Keyword", KEYWORD),
            new AttributesDescriptor("Number", NUMBER),
            new AttributesDescriptor("String", STRING),
            new AttributesDescriptor("Operator", OPERATOR),
            new AttributesDescriptor("Type", TYPE_NAME),
            new AttributesDescriptor("Type Variable", TYPE_VARIABLE),
            new AttributesDescriptor("Type Annotation//Name", TYPE_ANNOTATION_NAME),
            new AttributesDescriptor("Punctuation//Arrows", PS_ARROW),
            new AttributesDescriptor("Punctuation//Parentheses", PS_PARENTHESIS),
            new AttributesDescriptor("Punctuation//Braces", PS_BRACES),
            new AttributesDescriptor("Punctuation//Brackets", PS_BRACKETS),
            new AttributesDescriptor("Punctuation//Comma", PS_COMMA),
            new AttributesDescriptor("Punctuation//Dot", PS_DOT),
            new AttributesDescriptor("Punctuation//Equals", PS_EQ),
            new AttributesDescriptor("Import Reference", IMPORT_REF),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return PSIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new PSSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {

        return "module Main (Todo) where" +
                "\n" +
                "import Data.Html (div, h1)";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return TAG_HIGHLIGHTING_MAP;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRIBS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Purescript";
    }
}
