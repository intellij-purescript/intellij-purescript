package net.kenro.ji.jin.purescript.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import net.kenro.ji.jin.purescript.icons.PSIcons
import org.jetbrains.annotations.NonNls
import java.util.*
import javax.swing.Icon

class PSColorSettingsPage : ColorSettingsPage {
    companion object {
        @NonNls
        private val TAG_HIGHLIGHTING_MAP: MutableMap<String, TextAttributesKey> =
            HashMap()
        private val ATTRIBS = arrayOf(
            AttributesDescriptor(
                "Keyword",
                PSSyntaxHighlighter.Companion.KEYWORD
            ),
            AttributesDescriptor(
                "Number",
                PSSyntaxHighlighter.Companion.NUMBER
            ),
            AttributesDescriptor(
                "String",
                PSSyntaxHighlighter.Companion.STRING
            ),
            AttributesDescriptor(
                "Operator",
                PSSyntaxHighlighter.Companion.OPERATOR
            ),
            AttributesDescriptor(
                "Type",
                PSSyntaxHighlighter.Companion.TYPE_NAME
            ),
            AttributesDescriptor(
                "Type Variable",
                PSSyntaxHighlighter.Companion.TYPE_VARIABLE
            ),
            AttributesDescriptor(
                "Type Annotation//Name",
                PSSyntaxHighlighter.Companion.TYPE_ANNOTATION_NAME
            ),
            AttributesDescriptor(
                "Punctuation//Arrows",
                PSSyntaxHighlighter.Companion.PS_ARROW
            ),
            AttributesDescriptor(
                "Punctuation//Parentheses",
                PSSyntaxHighlighter.Companion.PS_PARENTHESIS
            ),
            AttributesDescriptor(
                "Punctuation//Braces",
                PSSyntaxHighlighter.Companion.PS_BRACES
            ),
            AttributesDescriptor(
                "Punctuation//Brackets",
                PSSyntaxHighlighter.Companion.PS_BRACKETS
            ),
            AttributesDescriptor(
                "Punctuation//Comma",
                PSSyntaxHighlighter.Companion.PS_COMMA
            ),
            AttributesDescriptor(
                "Punctuation//Dot",
                PSSyntaxHighlighter.Companion.PS_DOT
            ),
            AttributesDescriptor(
                "Punctuation//Equals",
                PSSyntaxHighlighter.Companion.PS_EQ
            ),
            AttributesDescriptor(
                "Import Reference",
                PSSyntaxHighlighter.Companion.IMPORT_REF
            )
        )

        init {
            TAG_HIGHLIGHTING_MAP["import_ref"] = PSSyntaxHighlighter.Companion.IMPORT_REF // blue
            TAG_HIGHLIGHTING_MAP["type_variable"] = PSSyntaxHighlighter.Companion.TYPE_VARIABLE // red
            TAG_HIGHLIGHTING_MAP["type_name"] = PSSyntaxHighlighter.Companion.TYPE_NAME // yellow
            TAG_HIGHLIGHTING_MAP["type_annotation_name"] = PSSyntaxHighlighter.Companion.TYPE_ANNOTATION_NAME // blue
        }
    }

    override fun getIcon(): Icon {
        return PSIcons.FILE
    }

    override fun getHighlighter(): SyntaxHighlighter {
        return PSSyntaxHighlighter()
    }

    override fun getDemoText(): String {
        return """module DemoText.View where

import Prelude hiding (<import_ref>div</import_ref>)
import UserManagement.Models (<type_name>Model(..)</type_name>, <type_name>User(..)</type_name>, <type_name>class Cool</type_name>)
import UserManagement.Query
import Data.Functor (<import_ref>map</import_ref>)

-- This is a line comment

{- 
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
            = a + b"""
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? {
        return TAG_HIGHLIGHTING_MAP
    }

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
        return ATTRIBS
    }

    override fun getColorDescriptors(): Array<ColorDescriptor> {
        return ColorDescriptor.EMPTY_ARRAY
    }

    override fun getDisplayName(): String {
        return "Purescript"
    }
}