@file:Suppress("ComplexRedundantLet")

package org.purescript.lexer

import com.intellij.lexer.DelegateLexer
import com.intellij.lexer.Lexer
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.TokenSet
import org.purescript.lexer.token.SourcePos
import org.purescript.lexer.token.SourceToken
import org.purescript.parser.*
import org.purescript.psi.PSElementType


data class Lexeme(
    val token: SourceToken,
    val trailingWhitespace: List<SourceToken>
) {
    val tokens get() = listOf(token) + trailingWhitespace
    val start get() = token.start
    val end get() = trailingWhitespace.lastOrNull()?.end ?: token.end
    val value = token.value
}

data class SuperToken(
    val qualified: List<Lexeme>,
    val token: Lexeme,
) {
    val tokens get() = qualified.flatMap { it.tokens } + token.tokens
    val start get() = qualified.firstOrNull()?.start ?: token.start
    val end get() = token.end
    val value = token.value
}

enum class LayoutDelimiter(val isIndent: Boolean) {
    Root(false),
    TopDecl(false),
    TopDeclHead(false),
    DeclGuard(false),
    Case(false),
    CaseBinders(false),
    CaseGuard(false),
    LambdaBinders(false),
    Paren(false),
    Brace(false),
    Square(false),
    If(false),
    Then(false),
    Property(false),
    Forall(false),
    Tick(false),
    Let(true),
    LetStmt(true),
    Where(true),
    Of(true),
    Do(true),
    Ado(true),
}

fun isTopDecl(tokPos: SourcePos, stk: LayoutStack?): Boolean = when {
    stk?.tail == null || stk.tail.tail != null -> false
    stk.tail.layoutDelimiter != LayoutDelimiter.Root -> false
    stk.layoutDelimiter != LayoutDelimiter.Where -> false
    else -> tokPos.column == stk.sourcePos.column
}

fun toSuper(token: Lexeme): SuperToken = SuperToken(emptyList(), token)

fun lytToken(pos: SourcePos, value: PSElementType) =
    toSuper(SourceToken(value, pos, pos).asLexeme)

fun <A> snoc(acc: List<A>, pair: A): List<A> {
    val acc2 = acc.toMutableList()
    acc2 += pair
    return acc2
}

fun offsideP(tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter) =
    lyt.isIndent && tokPos.column < lytPos.column

fun sepP(tokPos: SourcePos, lytPos: SourcePos): Boolean =
    tokPos.column == lytPos.column && tokPos.line != lytPos.line

fun identSepP(tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter) =
    lyt.isIndent && sepP(tokPos, lytPos)


tailrec fun find(stack: LayoutStack?, filter: (LayoutStack) -> Boolean)
    : LayoutStack? = when {
    stack == null -> null
    filter(stack) -> stack
    else -> find(stack.tail, filter)
}


inline fun insertKwProperty(
    src: SuperToken,
    tokPos: SourcePos,
    state: LayoutState,
    k: (LayoutState) -> LayoutState
): LayoutState {
    val state2 = state.insertDefault(src, tokPos)
    return if (state.stack?.layoutDelimiter == LayoutDelimiter.Property) {
        LayoutState(state2.stack?.tail, state2.acc)
    } else {
        k(state2)
    }
}

fun insertStart(
    nextPos: SourcePos, lyt: LayoutDelimiter, state: LayoutState
): LayoutState {
    val stk = state.stack
    val (pos, _, _) = find(stk) { stack: LayoutStack ->
        stack.layoutDelimiter.isIndent
    } ?: return state.pushStack(nextPos, lyt)
        .insertToken(lytToken(nextPos, LAYOUT_START))
    return if (nextPos.column <= pos.column) {
        state
    } else {
        state.pushStack(nextPos, lyt)
            .insertToken(lytToken(nextPos, LAYOUT_START))
    }
}

fun offsideEndP(
    tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter
): Boolean {
    return lyt.isIndent && tokPos.column <= lytPos.column
}

fun insertLayout(src: SuperToken, nextPos: SourcePos, stack: LayoutStack?)
    : LayoutState {
    val tokenValue = src.value
    val tokPos = src.start
    val state = LayoutState(stack, emptyList())
    val (stk, acc) = state

    return when (tokenValue) {
        LOWER, TYPE -> state
            .let { it.insertDefault(src, tokPos) }
            .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property } }

        OPERATOR -> state
            .let {
                it.collapse(tokPos) { tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter ->
                    offsideP(
                        tokPos,
                        lytPos,
                        lyt
                    )
                }
            }
            .let { it.insertSep(tokPos) }
            .let { it.insertToken(src) }

        EQ -> {
            val (stk2, acc2) = state.collapse(tokPos) { lyt: LayoutDelimiter ->
                when (lyt) {
                    LayoutDelimiter.Where -> true
                    LayoutDelimiter.Let -> true
                    LayoutDelimiter.LetStmt -> true
                    else -> false
                }
            }
            when (stk2?.layoutDelimiter) {
                LayoutDelimiter.DeclGuard -> LayoutState(stk2.tail, acc2)
                    .let { it.insertToken(src) }

                else -> state.insertDefault(src, tokPos)
            }
        }

        COMMA -> {
            val state2 = state.collapse(tokPos) { lyt: LayoutDelimiter ->
                lyt
                    .isIndent
            }
            if (state2.stack?.layoutDelimiter == LayoutDelimiter.Brace) {
                state2
                    .let { it.insertToken(src) }
                    .let { it.pushStack(tokPos, LayoutDelimiter.Property) }
            } else {
                state2
                    .let { it.insertToken(src) }
            }
        }

        DOT -> {
            val state2 = state.insertDefault(src, tokPos)
            if (state2.stack?.layoutDelimiter == LayoutDelimiter.Forall) {
                LayoutState(state2.stack.tail, state2.acc)
            } else {
                state2
                    .let { it.pushStack(tokPos, LayoutDelimiter.Property) }
            }
        }

        ARROW -> {

            state
                .collapse(tokPos) { tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter ->
                    when (lyt) {
                        LayoutDelimiter.Do -> true
                        LayoutDelimiter.Of -> false
                        else -> offsideEndP(tokPos, lytPos, lyt)
                    }
                }
                .let {
                    it.popStack { it: LayoutDelimiter ->
                        when (it) {
                            LayoutDelimiter.CaseBinders -> true
                            LayoutDelimiter.CaseGuard -> true
                            LayoutDelimiter.LambdaBinders -> true
                            else -> false
                        }
                    }
                }
                .let { it.insertToken(src) }
        }


        FORALL -> insertKwProperty(src, tokPos, state) {
            it.pushStack(tokPos, LayoutDelimiter.Forall)
        }

        DATA -> {
            val state2 = state.insertDefault(src, tokPos)
            if (isTopDecl(tokPos, state2.stack)) {
                state2.pushStack(tokPos, LayoutDelimiter.TopDecl)
            } else {
                state2.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
            }
        }

        CLASS -> {
            val state2 = state.insertDefault(src, tokPos)
            if (isTopDecl(tokPos, state2.stack)) {
                state2.pushStack(tokPos, LayoutDelimiter.TopDeclHead)
            } else {
                state2.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
            }
        }

        WHERE -> when (stk?.layoutDelimiter) {
            LayoutDelimiter.TopDeclHead ->
                LayoutState(stk.tail, acc)
                    .let { it.insertToken(src) }
                    .let {
                        insertStart(
                            nextPos,
                            LayoutDelimiter.Where,
                            it
                        )
                    }

            LayoutDelimiter.Property ->
                LayoutState(stk.tail, acc).insertToken(src)

            else ->
                state
                    .let {
                        it.collapse(tokPos) { tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter ->
                            if (lyt == LayoutDelimiter.Do) true
                            else offsideEndP(tokPos, lytPos, lyt)
                        }
                    }
                    .let { it.insertToken(src) }
                    .let {
                        insertStart(
                            nextPos,
                            LayoutDelimiter.Where,
                            it
                        )
                    }
        }

        LPAREN -> state
            .let { it.insertDefault(src, tokPos) }
            .let { it.pushStack(tokPos, LayoutDelimiter.Paren) }

        LCURLY -> state
            .let { it.insertDefault(src, tokPos) }
            .let { it.pushStack(tokPos, LayoutDelimiter.Brace) }
            .let { it.pushStack(tokPos, LayoutDelimiter.Property) }

        RPAREN -> state
            .let {
                it.collapse(tokPos) { lyt: LayoutDelimiter -> lyt.isIndent }
            }
            .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Paren } }
            .let { it.insertToken(src) }

        RCURLY -> state
            .let {
                it.collapse(tokPos) { lyt: LayoutDelimiter -> lyt.isIndent }
            }
            .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property } }
            .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Brace } }
            .let { it.insertToken(src) }

        LBRACK -> state
            .let { it.insertDefault(src, tokPos) }
            .let { it.pushStack(tokPos, LayoutDelimiter.Square) }

        RBRACK -> state
            .let {
                it.collapse(tokPos) { lyt: LayoutDelimiter -> lyt.isIndent }
            }
            .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Square } }
            .let { it.insertToken(src) }

        IN -> {
            val (stk1, acc2) = state.collapse(tokPos) { lyt: LayoutDelimiter ->
                when (lyt) {
                    LayoutDelimiter.Let -> false
                    LayoutDelimiter.Ado -> false
                    else -> lyt.isIndent
                }
            }
            val (_, lyt, stk2) = stk1 ?: return state
                .let { it.insertDefault(src, tokPos) }
                .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property } }
            if (lyt == LayoutDelimiter.LetStmt && stk2?.layoutDelimiter == LayoutDelimiter.Ado) {
                return LayoutState(stk2.tail, acc2)
                    .let { it.insertToken(lytToken(tokPos, LAYOUT_END)) }
                    .let { it.insertToken(lytToken(tokPos, LAYOUT_END)) }
                    .let { it.insertToken(src) }
            } else if (lyt.isIndent) {
                return LayoutState(stk2, acc2)
                    .let { it.insertToken(lytToken(tokPos, LAYOUT_END)) }
                    .let { it.insertToken(src) }
            } else {
                return state
                    .let { it.insertDefault(src, tokPos) }
                    .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property } }
            }
        }

        LET -> {
            fun next(state: LayoutState): LayoutState {
                val (p, lyt, _) = state.stack ?: return insertStart(
                    nextPos,
                    LayoutDelimiter.Let,
                    state
                )
                return when {
                    lyt == LayoutDelimiter.Do && p.column == tokPos.column ->
                        insertStart(nextPos, LayoutDelimiter.LetStmt, state)

                    lyt == LayoutDelimiter.Ado && p.column == tokPos.column ->
                        insertStart(nextPos, LayoutDelimiter.LetStmt, state)

                    else -> insertStart(nextPos, LayoutDelimiter.Let, state)
                }
            }

            return insertKwProperty(src, tokPos, state, ::next)
        }

        DO ->
            insertKwProperty(
                src,
                tokPos,
                state
            ) { insertStart(nextPos, LayoutDelimiter.Do, it) }

        ADO ->
            insertKwProperty(
                src,
                tokPos,
                state
            ) { insertStart(nextPos, LayoutDelimiter.Ado, it) }

        CASE ->
            insertKwProperty(
                src,
                tokPos,
                state
            ) { it.pushStack(tokPos, LayoutDelimiter.Case) }

        OF -> {
            val state2 = state.collapse(tokPos) { lyt: LayoutDelimiter ->
                lyt
                    .isIndent
            }
            return if (state2.stack?.layoutDelimiter == LayoutDelimiter.Case) {
                LayoutState(state2.stack.tail, state2.acc)
                    .let { it.insertToken(src) }
                    .let { insertStart(nextPos, LayoutDelimiter.Of, it) }
                    .let {
                        it
                            .pushStack(
                                nextPos,
                                LayoutDelimiter.CaseBinders
                            )
                    }
            } else {
                state2
                    .let { it.insertDefault(src, tokPos) }
                    .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property } }
            }
        }

        BACKSLASH -> state
            .let { it.insertDefault(src, tokPos) }
            .let { it.pushStack(tokPos, LayoutDelimiter.LambdaBinders) }

        PIPE -> {
            val state2 =
                state.collapse(tokPos) { tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter ->
                    offsideEndP(
                        tokPos,
                        lytPos,
                        lyt
                    )
                }
            val (stk2, _) = state2
            when (stk2?.layoutDelimiter) {
                LayoutDelimiter.Of -> state2
                    .let {
                        it.pushStack(tokPos, LayoutDelimiter.CaseGuard)
                    }
                    .let { it.insertToken(src) }

                LayoutDelimiter.Let -> state2
                    .let {
                        it.pushStack(tokPos, LayoutDelimiter.DeclGuard)
                    }
                    .let { it.insertToken(src) }

                LayoutDelimiter.LetStmt -> state2
                    .let {
                        it.pushStack(tokPos, LayoutDelimiter.DeclGuard)
                    }
                    .let { it.insertToken(src) }

                LayoutDelimiter.Where -> state2
                    .let {
                        it.pushStack(tokPos, LayoutDelimiter.DeclGuard)
                    }
                    .let { it.insertToken(src) }

                else -> state.let { it.insertDefault(src, tokPos) }
            }
        }

        TICK -> {
            val state2 = state.collapse(tokPos) { lyt: LayoutDelimiter ->
                lyt
                    .isIndent
            }
            if (state2.stack?.layoutDelimiter == LayoutDelimiter.Tick) {
                LayoutState(state2.stack.tail, state2.acc)
                    .let { it.insertToken(src) }
            } else {
                state
                    .let { it.insertDefault(src, tokPos) }
                    .let { it.pushStack(tokPos, LayoutDelimiter.Tick) }
            }
        }

        STRING -> state
            .let { it.insertDefault(src, tokPos) }
            .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property } }

        IF ->
            insertKwProperty(
                src,
                tokPos,
                state
            ) { it.pushStack(tokPos, LayoutDelimiter.If) }

        THEN -> {
            val state2 = state.collapse(tokPos) { lyt: LayoutDelimiter ->
                lyt
                    .isIndent
            }
            if (state2.stack?.layoutDelimiter == LayoutDelimiter.If) {
                LayoutState(state2.stack.tail, state2.acc)
                    .let { it.insertToken(src) }
                    .let { it.pushStack(tokPos, LayoutDelimiter.Then) }
            } else {
                state
                    .insertDefault(src, tokPos)
                    .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property } }
            }
        }

        ELSE -> {
            val state2 =
                state.collapse(tokPos) { lyt: LayoutDelimiter -> lyt.isIndent }
            if (state2.stack?.layoutDelimiter == LayoutDelimiter.Then) {
                LayoutState(state2.stack.tail, state2.acc)
                    .let { it.insertToken(src) }
            } else {
                val state3 =
                    state.collapse(tokPos) { tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter ->
                        offsideP(
                            tokPos,
                            lytPos,
                            lyt
                        )
                    }
                if (isTopDecl(tokPos, state3.stack)) {
                    state3.insertToken(src)
                } else {
                    state3
                        .insertSep(tokPos)
                        .let { it.insertToken(src) }
                        .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property } }
                }
            }
        }

        else -> state.insertDefault(src, tokPos)
    }
}

fun lex(tokens: List<SuperToken>): List<SuperToken> {
    val sourcePos = SourcePos(0, 0, 0)
    var stack: LayoutStack? = LayoutStack(
        sourcePos,
        LayoutDelimiter.Root,
        null
    )
    val acc = mutableListOf<SuperToken>()
    var startPos = sourcePos
    for (posToken in tokens) {
        val nextStart = posToken.end
        val (nextStack, toks) = insertLayout(posToken, nextStart, stack)
        val ts = toks.map { it.first }
        acc += ts
        stack = nextStack
        startPos = nextStart
    }
    if (stack != null) {
        val layoutEnd = lytToken(startPos, LAYOUT_END)
        acc += List(stack.count { it.isIndent }) { layoutEnd }
    }
    return acc
}

fun correctLineAndColumn(source: CharSequence) =
    { previous: SourceToken, current: SourceToken ->
        val start = previous.end
        val end = current.end
        // might be expensive
        val subSequence = source.subSequence(start.offset, end.offset)
        val newlineIndex = subSequence.lastIndexOf('\n')
        val tokenLength = end.offset - start.offset
        val newEnd = when (newlineIndex) {
            -1 -> SourcePos(start.line, start.column + tokenLength, end.offset)

            else -> SourcePos(
                start.line + subSequence.count { it == '\n' },
                tokenLength - newlineIndex - 1,
                end.offset
            )
        }
        SourceToken(current.value, start, newEnd)
    }

fun posFromOffset(offset: Int) = SourcePos(0, 0, offset)

fun getTokens(lexer: Lexer) = generateSequence {
    val sourceToken: SourceToken? = lexer.tokenType?.let { value ->
        SourceToken(
            value,
            posFromOffset(lexer.tokenStart),
            posFromOffset(lexer.tokenEnd)
        )
    }
    lexer.advance()
    sourceToken
}

class LayoutLexer(delegate: Lexer) : DelegateLexer(delegate) {

    private var tokens: List<SourceToken> = listOf()
    private var index = 0
    private val root =
        SourceToken(WHITE_SPACE, posFromOffset(0), posFromOffset(0))

    override fun start(buffer: CharSequence, start: Int, end: Int, state: Int) {
        require(start == 0) { "does not support incremental lexing: startOffset must be 0" }
        require(state == 0) { "does not support incremental lexing: initialState must be 0" }

        super.start(buffer, start, end, state)
        this.tokens = getTokens(delegate)
            .runningFold(root, correctLineAndColumn(buffer))
            .drop(1)
            .toList()
            .let { toSupers(it) }
            .let(::lex)
            .flatMap { it.tokens }
        index = 0
    }

    private val trailingTokens =
        TokenSet.create(WHITE_SPACE, MLCOMMENT, SLCOMMENT, DOC_COMMENT)

    private fun toLexemes(sourceTokens: List<SourceToken>): List<Lexeme> {
        if (sourceTokens.isEmpty()) return emptyList()
        var trailing = ArrayList<SourceToken>(4)
        val lexemes = ArrayList<Lexeme>(sourceTokens.size)
        var token: SourceToken = sourceTokens.first()
        for (t in sourceTokens.subList(1, sourceTokens.size)) when {
            t.value in trailingTokens ->
                trailing.add(t)

            else -> {
                lexemes.add(Lexeme(token, trailing))
                token = t
                trailing = ArrayList(4)
            }
        }
        lexemes.add(Lexeme(token, trailing))
        return lexemes
    }

    private fun toSupers(sourceTokens: List<SourceToken>): List<SuperToken> {
        val lexemes = toLexemes(sourceTokens)
        var qualified = mutableListOf<Lexeme>()
        var lexeme: Lexeme? = null
        val superTokens = mutableListOf<SuperToken>()
        for (l in lexemes) when {
            lexeme == null -> lexeme = l
            lexeme.value == PROPER_NAME &&
                l.value == DOT &&
                l.trailingWhitespace.isEmpty() &&
                lexeme.trailingWhitespace.isEmpty() -> {
                // <Proper Name><.>
                qualified.add(lexeme)
                qualified.add(l)
                lexeme = null
            }

            else -> {
                superTokens.add(SuperToken(qualified, lexeme))
                qualified = mutableListOf()
                lexeme = l
            }
        }
        if (lexeme != null) superTokens.add(SuperToken(qualified, lexeme))
        return superTokens
    }

    override fun advance() {
        index++
    }

    override fun getTokenType() = tokens.getOrNull(index)?.value
    override fun getTokenEnd() = tokens[index].end.offset
    override fun getTokenStart() = tokens[index].start.offset
}