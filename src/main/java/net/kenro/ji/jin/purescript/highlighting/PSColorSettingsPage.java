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
        TAG_HIGHLIGHTING_MAP.put("import_ref", IMPORT_REF); // blue
        TAG_HIGHLIGHTING_MAP.put("type_variable", TYPE_VARIABLE); // red
        TAG_HIGHLIGHTING_MAP.put("type_name", TYPE_NAME); // yellow
        TAG_HIGHLIGHTING_MAP.put("type_annotation_name", TYPE_ANNOTATION_NAME); // blue
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

        return """
            module DemoText.View where

            import Prelude hiding (<import_ref>div</import_ref>)
            import UserManagement.Models (<type_name>Model(..)</type_name>, <type_name>User(..)</type_name>, <type_name>class Cool</type_name>)
            import UserManagement.Query
            import Data.Functor (<import_ref>map</import_ref>)

            -- This is a line comment

            {-\s
             This is a block comment
            -}

            newtype <type_name>X</type_name> = <type_name>X Int</type_name>

            <type_annotation_name>patternNewtype</type_annotation_name> :: <type_name>Boolean</type_name>
            patternNewtype =
              let <type_variable>X</type_variable> a = <type_variable>X</type_variable> 123
              in
               a == 123

            <type_annotation_name>patternDoNewtype</type_annotation_name> :: forall <type_variable>e</type_variable>. <type_name>Eff</type_name> <type_variable>e</type_variable> <type_name>Boolean</type_name>
            patternDoNewtype = do
              let <type_variable>X</type_variable> a = <type_variable>X</type_variable> 123
              pure $ a == 123

            data <type_name>Y</type_name> = <type_name>Y Int String Boolean</type_name>

            -- Guards have access to current scope
            collatz2 = \\<type_variable>x</type_variable> <type_variable>y</type_variable> -> case x of
              z | y > 0.0 -> z / 2.0
              z -> z * 3.0 + 1.0

            <type_annotation_name>min</type_annotation_name> :: forall <type_variable>a</type_variable>. <type_name>Ord</type_name> <type_variable>a</type_variable> => <type_variable>a</type_variable> -> <type_variable>a</type_variable> -> <type_variable>a</type_variable>
            min n m | n < m     = n
                    | otherwise = m

            <type_annotation_name>max</type_annotation_name> :: forall <type_variable>a</type_variable>. <type_name>Ord</type_name> <type_variable>a</type_variable> => <type_variable>a</type_variable> -> <type_variable>a</type_variable> -> <type_variable>a</type_variable>
            max n m = case unit of
              _ | m < n     -> n
                | otherwise -> m

            <type_annotation_name>testIndentation</type_annotation_name> :: <type_name>Number</type_name> -> <type_name>Number</type_name> -> <type_name>Number</type_name>
            testIndentation x y | x > 0.0
              = x + y
                                | otherwise
              = y - x

            -- pattern guard example with two clauses
            <type_annotation_name>clunky1</type_annotation_name> :: <type_name>Int</type_name> -> <type_name>Int</type_name> -> <type_name>Int</type_name>
            clunky1 a b | x <- max a b , x > 5 = x
            clunky1 a _ = a

            <type_annotation_name>clunky2</type_annotation_name> ::<type_name>Int</type_name> -> <type_name>Int</type_name> -> <type_name>Int</type_name>
            clunky2 a b | x <- max a b
                        , x > 5
                        = x
                        | otherwise
                        = a + b
            """;
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
