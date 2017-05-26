package net.kenro.ji.jin.purescript.psi;

import com.intellij.psi.tree.TokenSet;

public interface PSTokens {
    PSElementType ERROR = new PSElementType("error");
    PSElementType WS = new PSElementType("whitespace");
    PSElementType MLCOMMENT = new PSElementType("block comment");
    PSElementType SLCOMMENT = new PSElementType("line comment");

    PSElementType DATA = new PSElementType("data");
    PSElementType NEWTYPE = new PSElementType("newtype");
    PSElementType TYPE = new PSElementType("type");
    PSElementType FOREIGN = new PSElementType("foreign");
    PSElementType IMPORT = new PSElementType("import");
    PSElementType INFIXL = new PSElementType("infixl");
    PSElementType INFIXR = new PSElementType("infixr");
    PSElementType INFIX = new PSElementType("infix");
    PSElementType CLASS = new PSElementType("class");
    PSElementType INSTANCE = new PSElementType("instance");
    PSElementType MODULE = new PSElementType("module");
    PSElementType CASE = new PSElementType("case");
    PSElementType OF = new PSElementType("of");
    PSElementType IF = new PSElementType("if");
    PSElementType THEN = new PSElementType("then");
    PSElementType ELSE = new PSElementType("else");
    PSElementType DO = new PSElementType("do");
    PSElementType LET = new PSElementType("let");
    PSElementType TRUE = new PSElementType("true");
    PSElementType FALSE = new PSElementType("false");
    PSElementType IN = new PSElementType("in");
    PSElementType WHERE = new PSElementType("where");

    PSElementType FORALL = new PSElementType("forall");  // contextual keyword

    // TODO kings - I think qualified on import is gone now and can be removed
    PSElementType QUALIFIED = new PSElementType("qualified");  // contextual keyword
    PSElementType HIDING = new PSElementType("hiding");  // contextual keyword
    PSElementType AS = new PSElementType("as");  // contextual keyword

    PSElementType DARROW = new PSElementType("=>");
    PSElementType ARROW = new PSElementType("->");
    PSElementType LARROW = new PSElementType("<-");
    PSElementType EQ = new PSElementType("=");
    PSElementType DOT = new PSElementType(".");
    PSElementType DDOT = new PSElementType("..");  // contextual keyword

    PSElementType SEMI = new PSElementType(";");
    PSElementType DCOLON = new PSElementType("::");
    PSElementType TICK = new PSElementType("`");
    PSElementType PIPE = new PSElementType("|");
    PSElementType COMMA = new PSElementType(",");
    PSElementType LPAREN = new PSElementType("(");
    PSElementType RPAREN = new PSElementType(")");
    PSElementType LBRACK = new PSElementType("[");
    PSElementType RBRACK = new PSElementType("]");
    PSElementType LCURLY = new PSElementType("{");
    PSElementType RCURLY = new PSElementType("}");

    PSElementType START = new PSElementType("*");
    PSElementType BANG = new PSElementType("!");

    PSElementType BACKSLASH = new PSElementType("\\");
    PSElementType OPERATOR = new PSElementType("operator");
    PSElementType PROPER_NAME = new PSElementType("proper name");
    PSElementType IDENT = new PSElementType("identifier");
    PSElementType STRING_ESCAPED = new PSElementType("string escaping");
    PSElementType STRING_GAP = new PSElementType("string escaping");
    PSElementType STRING_ERROR = new PSElementType("string escaping error");
    PSElementType STRING = new PSElementType("string");
    PSElementType NATURAL = new PSElementType("natural");
    PSElementType FLOAT = new PSElementType("float");

    TokenSet kKeywords = TokenSet.create(DATA, NEWTYPE, TYPE, FOREIGN, IMPORT, INFIXL, INFIXR, INFIX, CLASS, INSTANCE,
            MODULE, CASE, OF, IF, THEN, ELSE, DO, LET, TRUE, FALSE, IN, WHERE, FORALL, QUALIFIED, HIDING, AS, START,
            BANG);
    TokenSet kStrings = TokenSet.create(STRING);
    TokenSet kOperators = TokenSet.create(DARROW, ARROW, EQ, DOT, LPAREN, RPAREN, LBRACK, RBRACK, LCURLY, RCURLY);

    PSElementType EOF = new PSElementType("<<eof>>");
}

