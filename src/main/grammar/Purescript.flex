package org.purescript.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import static org.purescript.parser.PSTokensKt.*;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.psi.TokenType.BAD_CHARACTER;

%%

%public
%class _PSLexer
%implements FlexLexer
%function advance
%type IElementType
%column
%unicode

%{
  public _PSLexer() {
    this((java.io.Reader)null);
  }

  public int getColumn() {
      return this.yycolumn;
  }
%}

whitespace = [ \t\f\r\n]
opChars = [\:\!#\$%&*+./<=>?@\\\^|\-~]
// https://github.com/purescript/documentation/blob/master/language/Syntax.md#function-and-value-names
identStart = \p{Ll}|"_"
identLetter = \p{L}|\p{M}|\p{N}|[_\']
properStart = \p{Lu}
properName = {properStart}{identLetter}*

// http://www.regular-expressions.info/unicode.html#prop
uniCode = \p{S}

decimal = [0-9][0-9_]*
hexadecimal = [xX][0-9a-zA-Z]+
octal = [oO][0-7]+
stringChars = [^\"\\]+
fractExponent = {fraction} {exponent}? | {exponent}
fraction = "." {decimal}
exponent = [eE] [+\-]? {decimal}
escapeEmpty = "&"
escapeGap = {whitespace}*"\\"
escapeCode = {charEsc} | {charNum} | {charAscii} | {charControl}
charEsc = [abfnrtv\\\"\']
charNum = {decimal} | "x" [0-9a-zA-Z]+ | "o" [0-7]+
charAscii = "BS"|"HT"|"LF"|"VT"|"FF"|"CR"|"SO"|"SI"|"EM"|"FS"|"GS"|"RS"|"US"|"SP"|"NUL"|"SOH"|"STX"|"ETX"|"EOT"|"ENQ"|"ACK"|"BEL"|"DLE"|"DC1"|"DC2"|"DC3"|"DC4"|"NAK"|"SYN"|"ETB"|"CAN"|"SUB"|"ESC"|"DEL"
charControl = "^" [:uppercase:]

%x COMMENT, STRINGS, BLOCK_STRINGS

%{
   int comment_nesting = 0;
%}

%%

<COMMENT> {

"{-"                           { comment_nesting++; return MLCOMMENT; }
"-}"                           { comment_nesting--; if (comment_nesting == 0) yybegin(YYINITIAL); return MLCOMMENT; }
[^]                            { return MLCOMMENT; }

}

<STRINGS> {
"\""                           { yybegin(YYINITIAL); return STRING; }
{stringChars}                   { return STRING; }
"\\" {escapeCode}              { return STRING_ESCAPED; }
"\\" {escapeEmpty}             { return STRING_GAP; }
"\\" {escapeGap}               { return STRING_GAP; }
"\\"                           { return STRING_ERROR; }
[^]                            { return BAD_CHARACTER; }
}

<BLOCK_STRINGS> {
"\"\"\"" "\""*                 { yybegin(YYINITIAL); return STRING; }
[^]                            { return STRING; }
}

<YYINITIAL> {

{whitespace}+                  { return WHITE_SPACE; }

"{-"                           { yybegin(COMMENT); comment_nesting = 1; return MLCOMMENT; }
"--" " "* "|" [^\n]*           { return DOC_COMMENT; }
"--" [^\n]*                    { return SLCOMMENT; }

"_"                            { return WILDCARD; }
"data"                         { return DATA; }
"type"                         { return TYPE; }
"newtype"                      { return NEWTYPE; }
"role"                         { return ROLE; }
"foreign"                      { return FOREIGN; }
"import"                       { return IMPORT; }
"infixl"                       { return INFIXL; }
"infixr"                       { return INFIXR; }
"infix"                        { return INFIX; }
"class"                        { return CLASS; }
"instance"                     { return INSTANCE; }
"derive"                       { return DERIVE; }
"module"                       { return MODULE; }
"nominal"                      { return NOMINAL; }
"case"                         { return CASE; }
"of"                           { return OF; }
"if"                           { return IF; }
"then"                         { return THEN; }
"else"                         { return ELSE; }
"do"                           { return DO; }
"ado"                          { return ADO; }
"let"                          { return LET; }
"true"                         { return TRUE; }
"false"                        { return FALSE; }
"in"                           { return IN; }
"where"                        { return WHERE; }
"∀"                            { return FORALL; }
"forall"                       { return FORALL; }
"\\u2200"                      { return FORALL; }
"\u2200"                       { return FORALL; }
"hiding"                       { return HIDING; }
"as"                           { return AS; }

"=>"                           { return DARROW; }
"\\u21D2"                      { return DARROW; }
"\u21D2"                       { return DARROW; }
"<="                           { return LDARROW; }
"\\u21D0"                      { return LDARROW; }
"\u21D0"                       { return LDARROW; }
"->"                           { return ARROW; }
"\\u2192"                      { return ARROW; }
"\u2192"                       { return ARROW; }
"<-"                           { return LARROW; }
"\\u2190"                      { return LARROW; }
"\u2190"                       { return LARROW; }
"="                            { return EQ; }
"@"                            { return AT; }
"."                            { return DOT; }
"\\"                           { return BACKSLASH; }

":"                            { return COLON; }
"-"                            { return MINUS; }

";"                            { return SEMI; }
"::"                           { return DCOLON; }
"∷"                            { return DCOLON; }
"\u2237"                       { return DCOLON; }
"\\u2237"                      { return DCOLON; }
"`"                            { return TICK; }
"|"                            { return PIPE; }

","                            { return COMMA; }
"("                            { return LPAREN; }
")"                            { return RPAREN; }
"["                            { return LBRACK; }
"]"                            { return RBRACK; }
"{"                            { return LCURLY; }
"}"                            { return RCURLY; }
".."                           { return DDOT; }

"~>"                           {return OPTIMISTIC; }

"0"({hexadecimal}|{octal}|{decimal})|{decimal} { return NATURAL; }
{decimal}{fractExponent}                       { return FLOAT; }
"\"\"\""                                       { yybegin(BLOCK_STRINGS); return STRING; }
"\""                                           { yybegin(STRINGS); return STRING; }
"'" ( "\\" {escapeCode} | . ) "'"               { return CHAR; }

"?"{identStart}{identLetter}*     { return HOLE; }
{identStart}{identLetter}*     { return LOWER; }
({properName}".")+             { return MODULE_PREFIX; }
{properName}                   { return PROPER_NAME; }
{uniCode}+                     { return OPERATOR; }
{opChars}+                     { return OPERATOR; }

.                              { return BAD_CHARACTER; }
}
