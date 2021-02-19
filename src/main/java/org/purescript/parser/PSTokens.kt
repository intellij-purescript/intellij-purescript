package org.purescript.parser

import com.intellij.psi.tree.TokenSet
import org.purescript.psi.PSElementType

interface PSTokens {
    companion object {
        @kotlin.jvm.JvmField
        val ERROR = PSElementType("error")
        @kotlin.jvm.JvmField
        val WS = PSElementType("whitespace")
        @kotlin.jvm.JvmField
        val MLCOMMENT = PSElementType("block comment")
        @kotlin.jvm.JvmField
        val SLCOMMENT = PSElementType("line comment")
        @kotlin.jvm.JvmField
        val DOC_COMMENT = PSElementType("doc comment")
        @kotlin.jvm.JvmField
        val DATA = PSElementType("data")
        @kotlin.jvm.JvmField
        val NEWTYPE = PSElementType("newtype")
        @kotlin.jvm.JvmField
        val TYPE = PSElementType("type")
        @kotlin.jvm.JvmField
        val FOREIGN = PSElementType("foreign")
        @kotlin.jvm.JvmField
        val IMPORT = PSElementType("import")
        @kotlin.jvm.JvmField
        val INFIXL = PSElementType("infixl")
        @kotlin.jvm.JvmField
        val INFIXR = PSElementType("infixr")
        @kotlin.jvm.JvmField
        val INFIX = PSElementType("infix")
        @kotlin.jvm.JvmField
        val CLASS = PSElementType("class")
        @kotlin.jvm.JvmField
        val INSTANCE = PSElementType("instance")
        @kotlin.jvm.JvmField
        val DERIVE = PSElementType("derive")
        @kotlin.jvm.JvmField
        val MODULE = PSElementType("module")
        @kotlin.jvm.JvmField
        val CASE = PSElementType("case")
        @kotlin.jvm.JvmField
        val OF = PSElementType("of")
        @kotlin.jvm.JvmField
        val IF = PSElementType("if")
        @kotlin.jvm.JvmField
        val THEN = PSElementType("then")
        @kotlin.jvm.JvmField
        val ELSE = PSElementType("else")
        @kotlin.jvm.JvmField
        val DO = PSElementType("do")
        @kotlin.jvm.JvmField
        val LET = PSElementType("let")
        @kotlin.jvm.JvmField
        val TRUE = PSElementType("true")
        @kotlin.jvm.JvmField
        val FALSE = PSElementType("false")
        @kotlin.jvm.JvmField
        val IN = PSElementType("in")
        @kotlin.jvm.JvmField
        val WHERE = PSElementType("where")
        @kotlin.jvm.JvmField
        val FORALL = PSElementType("forall") // contextual keyword

        // TODO kings - I think qualified on import is gone now and can be removed
        @kotlin.jvm.JvmField
        val QUALIFIED = PSElementType("qualified") // contextual keyword
        @kotlin.jvm.JvmField
        val HIDING = PSElementType("hiding") // contextual keyword
        @kotlin.jvm.JvmField
        val AS = PSElementType("as") // contextual keyword
        @kotlin.jvm.JvmField
        val DARROW = PSElementType("=>")
        @kotlin.jvm.JvmField
        val LDARROW = PSElementType("<=")
        @kotlin.jvm.JvmField
        val ARROW = PSElementType("->")
        @kotlin.jvm.JvmField
        val LARROW = PSElementType("<-")
        @kotlin.jvm.JvmField
        val EQ = PSElementType("=")
        @kotlin.jvm.JvmField
        val DOT = PSElementType(".")
        @kotlin.jvm.JvmField
        val DDOT = PSElementType("..") // contextual keyword
        @kotlin.jvm.JvmField
        val SEMI = PSElementType(";")
        @kotlin.jvm.JvmField
        val DCOLON = PSElementType("::")
        @kotlin.jvm.JvmField
        val TICK = PSElementType("`")
        @kotlin.jvm.JvmField
        val PIPE = PSElementType("|")
        @kotlin.jvm.JvmField
        val COMMA = PSElementType(",")
        @kotlin.jvm.JvmField
        val LPAREN = PSElementType("(")
        @kotlin.jvm.JvmField
        val RPAREN = PSElementType(")")
        @kotlin.jvm.JvmField
        val LBRACK = PSElementType("[")
        @kotlin.jvm.JvmField
        val RBRACK = PSElementType("]")
        @kotlin.jvm.JvmField
        val LCURLY = PSElementType("{")
        @kotlin.jvm.JvmField
        val RCURLY = PSElementType("}")
        @kotlin.jvm.JvmField
        val START = PSElementType("*")
        @kotlin.jvm.JvmField
        val BANG = PSElementType("!")
        @kotlin.jvm.JvmField
        val BACKSLASH = PSElementType("\\")
        @kotlin.jvm.JvmField
        val OPERATOR = PSElementType("operator")
        @kotlin.jvm.JvmField
        val PROPER_NAME = PSElementType("proper name")
        @kotlin.jvm.JvmField
        val IDENT = PSElementType("identifier")
        @kotlin.jvm.JvmField
        val STRING_ESCAPED = PSElementType("string escaping")
        @kotlin.jvm.JvmField
        val STRING_GAP = PSElementType("string escaping")
        @kotlin.jvm.JvmField
        val STRING_ERROR = PSElementType("string escaping error")
        @kotlin.jvm.JvmField
        val STRING = PSElementType("string")
        @kotlin.jvm.JvmField
        val NATURAL = PSElementType("natural")
        @kotlin.jvm.JvmField
        val FLOAT = PSElementType("float")
        @kotlin.jvm.JvmField
        val OPTIMISTIC = PSElementType("~>")
        @kotlin.jvm.JvmField
        val kKeywords = TokenSet.create(
            DATA,
            NEWTYPE,
            TYPE,
            FOREIGN,
            IMPORT,
            INFIXL,
            INFIXR,
            INFIX,
            CLASS,
            DERIVE,
            INSTANCE,
            MODULE,
            CASE,
            OF,
            IF,
            THEN,
            ELSE,
            DO,
            LET,
            TRUE,
            FALSE,
            IN,
            WHERE,
            FORALL,
            QUALIFIED,
            HIDING,
            AS,
            START,
            BANG
        )
        @kotlin.jvm.JvmField
        val kStrings = TokenSet.create(STRING)
        @kotlin.jvm.JvmField
        val kOperators = TokenSet.create(
            DARROW,
            LDARROW,
            ARROW,
            EQ,
            DOT,
            OPTIMISTIC,
            OPERATOR
        )
        @kotlin.jvm.JvmField
        val EOF = PSElementType("<<eof>>")
    }
}
