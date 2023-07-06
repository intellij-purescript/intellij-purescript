@file:Suppress("ObjectPropertyName")

package org.purescript.parser

val `'derive'` = DERIVE.dsl
val `'foreign'`  = FOREIGN.dsl
val `'hiding'`  = HIDING.dsl
val `'import'` = IMPORT.dsl
val `'instance'` = INSTANCE.dsl
val `'newtype'` = NEWTYPE.dsl
val `'role'` = "role".dsl
val `'type'` = TYPE.dsl
val `'as'` = AS.dsl
val `'class'` = CLASS.dsl
val `'do'` = DO.dsl
val `'else'` = ELSE.dsl
val `'false'` = FALSE.dsl
val `'if'` = IF.dsl
val `'in'` = IN.dsl
val `'true'` = TRUE.dsl
val `'ado'` = ADO.dsl
val arrow = ARROW.dsl
val backslash = BACKSLASH.dsl
val `'case'` = CASE.dsl
val char = CHAR.dsl
val darrow = DARROW.dsl
val `'data'` = DATA.dsl
val dcolon = DCOLON.dsl
val hole = HOLE.dsl
val colon = COLON.dsl
val ddot = DDOT.dsl
val dot = DOT.dsl
val eq = EQ.dsl
val `'forall'` = FORALL.dsl
val `'infix'` = INFIX.dsl
val `'infixl'` = INFIXL.dsl
val `'infixr'` = INFIXR.dsl
val larrow = LARROW.dsl
val ldarrow = LDARROW.dsl
val `'let'` = LET.dsl
val `'module'` = MODULE.dsl
val `'nominal'` = "nominal".dsl
val `'of'` = OF.dsl
val phantom = "phantom".dsl
val `|` = PIPE.dsl
val `-` = MINUS.dsl
val representational = "representational".dsl
val string = STRING.dsl
val `'then'` = THEN.dsl 
val tick = TICK.dsl
val `'where'` = WHERE.dsl
val `,` = COMMA.dsl

@Suppress("ObjectPropertyName")
val `@` = "@".dsl
@Suppress("ObjectPropertyName")
val `_` = WILDCARD.dsl

val `(` = LPAREN.dsl
val `)` = RPAREN.dsl

val `L{` = LAYOUT_START.dsl
val `L-sep` = LAYOUT_SEP.dsl
val `L}` = LAYOUT_END.dsl