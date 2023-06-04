package org.purescript.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import org.jetbrains.annotations.NonNls
import org.purescript.highlighting.PSSyntaxHighlighter.FUNCTION_CALL
import org.purescript.highlighting.PSSyntaxHighlighter.FUNCTION_DECLARATION
import org.purescript.highlighting.PSSyntaxHighlighter.GLOBAL_VARIABLE
import org.purescript.highlighting.PSSyntaxHighlighter.IDENTIFIER
import org.purescript.highlighting.PSSyntaxHighlighter.IMPORT_REF
import org.purescript.highlighting.PSSyntaxHighlighter.KEYWORD
import org.purescript.highlighting.PSSyntaxHighlighter.LOCAL_VARIABLE
import org.purescript.highlighting.PSSyntaxHighlighter.NUMBER
import org.purescript.highlighting.PSSyntaxHighlighter.OPERATOR
import org.purescript.highlighting.PSSyntaxHighlighter.PARAMETER
import org.purescript.highlighting.PSSyntaxHighlighter.PS_ARROW
import org.purescript.highlighting.PSSyntaxHighlighter.PS_BRACES
import org.purescript.highlighting.PSSyntaxHighlighter.PS_BRACKETS
import org.purescript.highlighting.PSSyntaxHighlighter.PS_COMMA
import org.purescript.highlighting.PSSyntaxHighlighter.PS_DOT
import org.purescript.highlighting.PSSyntaxHighlighter.PS_EQ
import org.purescript.highlighting.PSSyntaxHighlighter.PS_PARENTHESIS
import org.purescript.highlighting.PSSyntaxHighlighter.STRING
import org.purescript.highlighting.PSSyntaxHighlighter.TYPE_ANNOTATION_NAME
import org.purescript.highlighting.PSSyntaxHighlighter.TYPE_NAME
import org.purescript.highlighting.PSSyntaxHighlighter.TYPE_VARIABLE
import org.purescript.icons.PSIcons
import javax.swing.Icon

class PSColorSettingsPage : ColorSettingsPage {
    companion object {
        @NonNls
        private val TAG_HIGHLIGHTING_MAP: MutableMap<String, TextAttributesKey> = HashMap()
        private val ATTRIBS = arrayOf(
            AttributesDescriptor("Keyword", KEYWORD),
            AttributesDescriptor("Number", NUMBER),
            AttributesDescriptor("String//String text", STRING),
            AttributesDescriptor("Operator", OPERATOR),
            AttributesDescriptor("Braces and Operators//Operation sign", OPERATOR),
            AttributesDescriptor("Braces and Operators//Equals", PS_EQ),
            AttributesDescriptor("Braces and Operators//Arrows", PS_ARROW),
            AttributesDescriptor("Braces and Operators//Braces", PS_BRACES),
            AttributesDescriptor("Braces and Operators//Parentheses", PS_PARENTHESIS),
            AttributesDescriptor("Braces and Operators//Brackets", PS_BRACKETS),
            AttributesDescriptor("Braces and Operators//Comma", PS_COMMA),
            AttributesDescriptor("Braces and Operators//Dot", PS_DOT),
            AttributesDescriptor("Identifiers//Default", IDENTIFIER),
            AttributesDescriptor("Identifiers//Local variable", LOCAL_VARIABLE),
            AttributesDescriptor("Identifiers//Global variable", GLOBAL_VARIABLE),
            AttributesDescriptor("Identifiers//Function declaration", FUNCTION_DECLARATION),
            AttributesDescriptor("Identifiers//Function call", FUNCTION_CALL),
            AttributesDescriptor("Identifiers//Parameter", PARAMETER),
            AttributesDescriptor("Identifiers//Type", TYPE_NAME),
            AttributesDescriptor("Identifiers//Type variable", TYPE_VARIABLE),
            AttributesDescriptor("Identifiers//Type annotation//name", TYPE_ANNOTATION_NAME),
            AttributesDescriptor("Import reference", IMPORT_REF)
        )

        init {
            TAG_HIGHLIGHTING_MAP["import_ref"] = IMPORT_REF // blue
            TAG_HIGHLIGHTING_MAP["type_variable"] = TYPE_VARIABLE // red
            TAG_HIGHLIGHTING_MAP["type_name"] = TYPE_NAME // yellow
            TAG_HIGHLIGHTING_MAP["type_annotation_name"] = TYPE_ANNOTATION_NAME // blue
        }
    }

    override fun getIcon(): Icon = PSIcons.FILE
    override fun getHighlighter(): SyntaxHighlighter = PSSyntaxHighlighter
    override fun getDemoText(): String = """
module DemoText.View where

import Prelude hiding (div)
import UserManagement.Models (Model(..), User(..), class Cool)
import UserManagement.Query
import Data.Functor (map)

-- This is a line comment

{- 
 This is a block comment
-}

newtype X = X Int

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
collatz2 = \<type_variable>x</type_variable> <type_variable>y</type_variable> -> case x of
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
            """

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = 
        TAG_HIGHLIGHTING_MAP

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = ATTRIBS
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getDisplayName(): String = "Purescript"
}