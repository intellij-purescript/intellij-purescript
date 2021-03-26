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
import org.purescript.parser.PSTokens.Companion.DO
import org.purescript.parser.PSTokens.Companion.DOT
import org.purescript.parser.PSTokens.Companion.ELSE
import org.purescript.parser.PSTokens.Companion.EQ
import org.purescript.parser.PSTokens.Companion.FALSE
import org.purescript.parser.PSTokens.Companion.FORALL
import org.purescript.parser.PSTokens.Companion.IF
import org.purescript.parser.PSTokens.Companion.IN
import org.purescript.parser.PSTokens.Companion.INFIX
import org.purescript.parser.PSTokens.Companion.LARROW
import org.purescript.parser.PSTokens.Companion.LDARROW
import org.purescript.parser.PSTokens.Companion.LPAREN
import org.purescript.parser.PSTokens.Companion.OF
import org.purescript.parser.PSTokens.Companion.PIPE
import org.purescript.parser.PSTokens.Companion.RPAREN
import org.purescript.parser.PSTokens.Companion.STRING
import org.purescript.parser.PSTokens.Companion.TICK
import org.purescript.parser.PSTokens.Companion.TRUE
import org.purescript.parser.PSTokens.Companion.WHERE

val `as` = token(AS)
val arrow = token(ARROW)
val backslash = token(BACKSLASH)
val case = token(CASE)
val char = token(CHAR)
val `class` = token(CLASS)
val data = token(DATA)
val darrow = token(DARROW)
val dcolon = token(DCOLON)
val ddot = token(DDOT)
val `do` = token(DO)
val dot = token(DOT)
val `else` = token(ELSE)
val eq = token(EQ)
val `false` = token(FALSE)
val forall = token(FORALL)
val `if` = token(IF)
val `in` = token(IN)
val infix = token(INFIX)
val larrow = token(LARROW)
val ldarrow = token(LDARROW)
val lparen = token(LPAREN)
val of = token(OF)
val pipe = token(PIPE)
val rparen = token(RPAREN)
val string = token(STRING)
val tick = token(TICK)
val `true` = token(TRUE)
val where = token(WHERE)

@Suppress("ObjectPropertyName")
val `@` = token("@")
@Suppress("ObjectPropertyName")
val `_` = token("_")

