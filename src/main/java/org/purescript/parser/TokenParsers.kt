package org.purescript.parser

import org.purescript.parser.Combinators.token
import org.purescript.parser.PSTokens.Companion.ARROW
import org.purescript.parser.PSTokens.Companion.AS
import org.purescript.parser.PSTokens.Companion.BACKSLASH
import org.purescript.parser.PSTokens.Companion.CASE
import org.purescript.parser.PSTokens.Companion.CHAR
import org.purescript.parser.PSTokens.Companion.CLASS
import org.purescript.parser.PSTokens.Companion.DARROW
import org.purescript.parser.PSTokens.Companion.DATA
import org.purescript.parser.PSTokens.Companion.DCOLON
import org.purescript.parser.PSTokens.Companion.DDOT
import org.purescript.parser.PSTokens.Companion.DERIVE
import org.purescript.parser.PSTokens.Companion.DO
import org.purescript.parser.PSTokens.Companion.DOT
import org.purescript.parser.PSTokens.Companion.ELSE
import org.purescript.parser.PSTokens.Companion.EQ
import org.purescript.parser.PSTokens.Companion.FALSE
import org.purescript.parser.PSTokens.Companion.FORALL
import org.purescript.parser.PSTokens.Companion.IF
import org.purescript.parser.PSTokens.Companion.IN
import org.purescript.parser.PSTokens.Companion.INFIX
import org.purescript.parser.PSTokens.Companion.INFIXL
import org.purescript.parser.PSTokens.Companion.INFIXR
import org.purescript.parser.PSTokens.Companion.INSTANCE
import org.purescript.parser.PSTokens.Companion.LARROW
import org.purescript.parser.PSTokens.Companion.LDARROW
import org.purescript.parser.PSTokens.Companion.LPAREN
import org.purescript.parser.PSTokens.Companion.NEWTYPE
import org.purescript.parser.PSTokens.Companion.OF
import org.purescript.parser.PSTokens.Companion.PIPE
import org.purescript.parser.PSTokens.Companion.RPAREN
import org.purescript.parser.PSTokens.Companion.STRING
import org.purescript.parser.PSTokens.Companion.TICK
import org.purescript.parser.PSTokens.Companion.TRUE
import org.purescript.parser.PSTokens.Companion.TYPE
import org.purescript.parser.PSTokens.Companion.WHERE

val `'instance'` = token(INSTANCE)
val `'derive'` = token(DERIVE)
val `'newtype'` = token(NEWTYPE)
val `'role'` = token("role")
val `'type'` = token(TYPE)
val `as` = token(AS)
val `class` = token(CLASS)
val `do` = token(DO)
val `else` = token(ELSE)
val `false` = token(FALSE)
val `if` = token(IF)
val `in` = token(IN)
val `true` = token(TRUE)
val arrow = token(ARROW)
val backslash = token(BACKSLASH)
val case = token(CASE)
val char = token(CHAR)
val darrow = token(DARROW)
val data = token(DATA)
val dcolon = token(DCOLON)
val ddot = token(DDOT)
val dot = token(DOT)
val eq = token(EQ)
val forall = token(FORALL)
val infix = token(INFIX)
val infixl = token(INFIXL)
val infixr = token(INFIXR)
val larrow = token(LARROW)
val ldarrow = token(LDARROW)
val lparen = token(LPAREN)
val nominal = token("nominal")
val of = token(OF)
val phantom = token("phantom")
val pipe = token(PIPE)
val representational = token("representational")
val rparen = token(RPAREN)
val string = token(STRING)
val tick = token(TICK)
val where = token(WHERE)

@Suppress("ObjectPropertyName")
val `@` = token("@")
@Suppress("ObjectPropertyName")
val `_` = token("_")

val `L{` = token(PSTokens.LAYOUT_START)
val `L-sep` = token(PSTokens.LAYOUT_SEP)
val `L}` = token(PSTokens.LAYOUT_END)

