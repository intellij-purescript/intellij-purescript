package org.purescript.parser

import com.intellij.psi.tree.TokenSet
import org.purescript.psi.PSElementType

@JvmField val LAYOUT_START = PSElementType("layout start")
@JvmField val LAYOUT_SEP = PSElementType("layout separator")
@JvmField val LAYOUT_END = PSElementType("layout end")
@JvmField val MLCOMMENT = PSElementType("block comment")
@JvmField val SLCOMMENT = PSElementType("line comment")
@JvmField val DOC_COMMENT = PSElementType("doc comment")
@JvmField val DATA = PSElementType("data")
@JvmField val NEWTYPE = PSElementType("newtype")
@JvmField val TYPE = PSElementType("type")
@JvmField val FOREIGN = PSElementType("foreign")
@JvmField val IMPORT = PSElementType("import")
@JvmField val INFIXL = PSElementType("infixl")
@JvmField val INFIXR = PSElementType("infixr")
@JvmField val INFIX = PSElementType("infix")
@JvmField val CLASS = PSElementType("class")
@JvmField val INSTANCE = PSElementType("instance")
@JvmField val DERIVE = PSElementType("derive")
@JvmField val MODULE = PSElementType("module")
@JvmField val CASE = PSElementType("case")
@JvmField val OF = PSElementType("of")
@JvmField val IF = PSElementType("if")
@JvmField val THEN = PSElementType("then")
@JvmField val ELSE = PSElementType("else")
@JvmField val DO = PSElementType("do")
@JvmField val ADO = PSElementType("ado")
@JvmField val LET = PSElementType("let")
@JvmField val TRUE = PSElementType("true")
@JvmField val FALSE = PSElementType("false")
@JvmField val IN = PSElementType("in")
@JvmField val WHERE = PSElementType("where")
@JvmField val FORALL = PSElementType("forall") // contextual keyword

// TODO kings - I think qualified on import is gone now and can be removed
@JvmField val HIDING = PSElementType("hiding") // contextual keyword
@JvmField val AS = PSElementType("as") // contextual keyword
@JvmField val DARROW = PSElementType("=>")
@JvmField val LDARROW = PSElementType("<=")
@JvmField val ARROW = PSElementType("->")
@JvmField val LARROW = PSElementType("<-")
@JvmField val EQ = PSElementType("=")
@JvmField val DOT = PSElementType(".")
@JvmField val DDOT = PSElementType("..") // contextual keyword
@JvmField val SEMI = PSElementType(";")
@JvmField val DCOLON = PSElementType("::")
@JvmField val TICK = PSElementType("`")
@JvmField val PIPE = PSElementType("|")
@JvmField val COMMA = PSElementType(",")
@JvmField val LPAREN = PSElementType("(")
@JvmField val RPAREN = PSElementType(")")
@JvmField val LBRACK = PSElementType("[")
@JvmField val RBRACK = PSElementType("]")
@JvmField val LCURLY = PSElementType("{")
@JvmField val RCURLY = PSElementType("}")
@JvmField val START = PSElementType("*")
@JvmField val BANG = PSElementType("!")
@JvmField val BACKSLASH = PSElementType("\\")
@JvmField val OPERATOR = PSElementType("operator")
@JvmField val MODULE_PREFIX = PSElementType("module prefix")
@JvmField val PROPER_NAME = PSElementType("proper name")
@JvmField val LOWER = PSElementType("identifier")
@JvmField val STRING_ESCAPED = PSElementType("string escaping")
@JvmField val STRING_GAP = PSElementType("string escaping")
@JvmField val STRING_ERROR = PSElementType("string escaping error")
@JvmField val STRING = PSElementType("string")
@JvmField val CHAR = PSElementType("char")
@JvmField val NATURAL = PSElementType("natural")
@JvmField val FLOAT = PSElementType("float")
@JvmField val OPTIMISTIC = PSElementType("~>")
@JvmField val kKeywords = TokenSet.create(
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
    ADO,
    LET,
    TRUE,
    FALSE,
    IN,
    WHERE,
    FORALL,
    HIDING,
    AS,
    START,
    BANG
)
@JvmField val kStrings = TokenSet.create(STRING)
@JvmField val kOperators = TokenSet.create(
    DARROW,
    LARROW,
    LDARROW,
    ARROW,
    EQ,
    DOT,
    OPTIMISTIC,
    OPERATOR
)
