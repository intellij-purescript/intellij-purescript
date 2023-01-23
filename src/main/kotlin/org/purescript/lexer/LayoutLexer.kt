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


data class LayoutStack(
    val sourcePos: SourcePos,
    val layoutDelimiter: LayoutDelimiter,
    val tail: LayoutStack?
) {
    inline fun count(filter: (LayoutDelimiter) -> Boolean): Int {
        var tail = this.tail
        var count = if (filter(layoutDelimiter)) 1 else 0
        while (tail != null) {
            if (filter(tail.layoutDelimiter)) count++
            tail = tail.tail
        }
        return count
    }
}

data class LayoutState(
    val stack: LayoutStack?,
    val acc: List<Pair<SuperToken, LayoutStack?>>
)

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

enum class LayoutDelimiter {
    Root,
    TopDecl,
    TopDeclHead,
    DeclGuard,
    Case,
    CaseBinders,
    CaseGuard,
    LambdaBinders,
    Paren,
    Brace,
    Square,
    If,
    Then,
    Property,
    Forall,
    Tick,
    Let,
    LetStmt,
    Where,
    Of,
    Do,
    Ado,
}

fun isIndented(lyt: LayoutDelimiter): Boolean = when (lyt) {
    LayoutDelimiter.Let -> true
    LayoutDelimiter.LetStmt -> true
    LayoutDelimiter.Where -> true
    LayoutDelimiter.Of -> true
    LayoutDelimiter.Do -> true
    LayoutDelimiter.Ado -> true
    else -> false
}

fun isTopDecl(tokPos: SourcePos, stk: LayoutStack?): Boolean = when {
    stk?.tail == null || stk.tail.tail != null -> false
    stk.tail.layoutDelimiter != LayoutDelimiter.Root -> false
    stk.layoutDelimiter != LayoutDelimiter.Where -> false
    else -> tokPos.column == stk.sourcePos.column
}

fun toSuper(token: Lexeme): SuperToken = SuperToken(emptyList(), token)
fun toLexeme(token: SourceToken): Lexeme = Lexeme(token, emptyList())

fun lytToken(pos: SourcePos, value: PSElementType) =
    toSuper(toLexeme(SourceToken(value, pos, pos)))

fun <A> snoc(acc: List<A>, pair: A): List<A> {
    val acc2 = acc.toMutableList()
    acc2 += pair
    return acc2
}

inline fun collapse(
    tokPos: SourcePos,
    state: LayoutState,
    p: (SourcePos, SourcePos, LayoutDelimiter) -> Boolean
): LayoutState {
    var (stack, acc) = state
    while (stack != null) {
        val (lytPos, lyt, tail) = stack
        if (!p(tokPos, lytPos, lyt)) {
            return LayoutState(stack, acc)
        } else {
            val nextAcc = if (isIndented(lyt)) {
                val pair = lytToken(tokPos, LAYOUT_END) to tail
                snoc(acc, pair)
            } else {
                acc
            }
            stack = tail
            acc = nextAcc
        }
    }
    return LayoutState(stack, acc)
}

inline fun collapse(
    tokPos: SourcePos,
    state: LayoutState,
    p: (LayoutDelimiter) -> Boolean
): LayoutState {
    var (stack, acc) = state
    while (stack != null) {
        val (_, lyt, tail) = stack
        if (!p(lyt)) {
            return LayoutState(stack, acc)
        } else {
            if (isIndented(lyt)) {
                val pair = lytToken(tokPos, LAYOUT_END) to tail
                acc = snoc(acc, pair)
            }
            stack = tail
        }
    }
    return LayoutState(null, acc)
}

fun offsideP(tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter) =
    isIndented(lyt) && tokPos.column < lytPos.column

fun sepP(tokPos: SourcePos, lytPos: SourcePos): Boolean =
    tokPos.column == lytPos.column && tokPos.line != lytPos.line

fun insertToken(token: SuperToken, state: LayoutState): LayoutState {
    val (stk, acc) = state
    val acc2 = acc.toMutableList()
    acc2 += token to stk
    return LayoutState(stk, acc2)
}

fun pushStack(lytPos: SourcePos, lyt: LayoutDelimiter, state: LayoutState) =
    LayoutState(LayoutStack(lytPos, lyt, state.stack), state.acc)

fun identSepP(tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter) =
    isIndented(lyt) && sepP(tokPos, lytPos)

fun insertSep(tokPos: SourcePos, state: LayoutState): LayoutState {
    val (stk, acc) = state
    val (lytPos, lyt, tail) = stk ?: return state
    val sepTok = lytToken(tokPos, LAYOUT_SEP)
    return when {
        LayoutDelimiter.TopDecl == lyt && sepP(tokPos, lytPos) ->
            insertToken(sepTok, LayoutState(tail, acc))

        LayoutDelimiter.TopDeclHead == lyt && sepP(tokPos, lytPos) ->
            insertToken(sepTok, LayoutState(tail, acc))

        identSepP(tokPos, lytPos, lyt) -> when (lyt) {
            LayoutDelimiter.Of -> pushStack(
                tokPos,
                LayoutDelimiter.CaseBinders,
                insertToken(sepTok, state)
            )

            else -> insertToken(sepTok, state)
        }

        else -> state
    }
}


inline fun popStack(
    state: LayoutState,
    p: (LayoutDelimiter) -> Boolean
): LayoutState {
    val lyt = state.stack?.layoutDelimiter
    return if (lyt != null && p(lyt)) {
        LayoutState(state.stack.tail, state.acc)
    } else {
        state
    }
}

tailrec fun find(stack: LayoutStack?, filter: (LayoutStack) -> Boolean)
    : LayoutStack? = when {
    stack == null -> null
    filter(stack) -> stack
    else -> find(stack.tail, filter)
}


fun insertEnd(tokPos: SourcePos, state: LayoutState): LayoutState =
    insertToken(lytToken(tokPos, LAYOUT_END), state)


fun insertDefault(
    src: SuperToken, tokPos: SourcePos, state: LayoutState
): LayoutState {
    return insertToken(
        src,
        insertSep(tokPos, collapse(tokPos, state, ::offsideP))
    )
}

inline fun insertKwProperty(
    src: SuperToken,
    tokPos: SourcePos,
    state: LayoutState,
    k: (LayoutState) -> LayoutState
): LayoutState {
    val state2 = insertDefault(src, tokPos, state)
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
        isIndented(stack.layoutDelimiter)
    } ?: return insertToken(
        lytToken(nextPos, LAYOUT_START),
        pushStack(nextPos, lyt, state)
    )
    return if (nextPos.column <= pos.column) {
        state
    } else {
        insertToken(
            lytToken(nextPos, LAYOUT_START),
            pushStack(nextPos, lyt, state)
        )
    }
}

fun offsideEndP(
    tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter
): Boolean {
    return isIndented(lyt) && tokPos.column <= lytPos.column
}

fun insertLayout(src: SuperToken, nextPos: SourcePos, stack: LayoutStack?)
    : LayoutState {
    val tokenValue = src.value
    val tokPos = src.start
    val state = LayoutState(stack, emptyList())
    val (stk, acc) = state

    return when (tokenValue) {
        LOWER, TYPE -> state
            .let { insertDefault(src, tokPos, it) }
            .let { state1 -> popStack(state1) { it == LayoutDelimiter.Property } }

        OPERATOR -> state
            .let { collapse(tokPos, it, ::offsideP) }
            .let { insertSep(tokPos, it) }
            .let { insertToken(src, it) }

        EQ -> {
            val (stk2, acc2) = collapse(tokPos, state) { lyt ->
                when (lyt) {
                    LayoutDelimiter.Where -> true
                    LayoutDelimiter.Let -> true
                    LayoutDelimiter.LetStmt -> true
                    else -> false
                }
            }
            when (stk2?.layoutDelimiter) {
                LayoutDelimiter.DeclGuard -> LayoutState(stk2.tail, acc2)
                    .let { insertToken(src, it) }

                else -> insertDefault(src, tokPos, state)
            }
        }

        COMMA -> {
            val state2 = collapse(tokPos, state) { lyt -> isIndented(lyt) }
            if (state2.stack?.layoutDelimiter == LayoutDelimiter.Brace) {
                state2
                    .let { insertToken(src, it) }
                    .let { pushStack(tokPos, LayoutDelimiter.Property, it) }
            } else {
                state2
                    .let { insertToken(src, it) }
            }
        }

        DOT -> {
            val state2 = insertDefault(src, tokPos, state)
            if (state2.stack?.layoutDelimiter == LayoutDelimiter.Forall) {
                LayoutState(state2.stack.tail, state2.acc)
            } else {
                state2
                    .let { pushStack(tokPos, LayoutDelimiter.Property, it) }
            }
        }

        ARROW -> {

            collapse(tokPos, state) { tokPos, lytPos, lyt ->
                when (lyt) {
                    LayoutDelimiter.Do -> true
                    LayoutDelimiter.Of -> false
                    else -> offsideEndP(tokPos, lytPos, lyt)
                }
            }
                .let {
                    popStack(it) {
                        when (it) {
                            LayoutDelimiter.CaseBinders -> true
                            LayoutDelimiter.CaseGuard -> true
                            LayoutDelimiter.LambdaBinders -> true
                            else -> false
                        }
                    }
                }
                .let { insertToken(src, it) }
        }


        FORALL -> insertKwProperty(src, tokPos, state) {
            pushStack(tokPos, LayoutDelimiter.Forall, it)
        }

        DATA -> {
            val state2 = insertDefault(src, tokPos, state)
            if (isTopDecl(tokPos, state2.stack)) {
                pushStack(tokPos, LayoutDelimiter.TopDecl, state2)
            } else {
                popStack(state2) { it == LayoutDelimiter.Property }
            }
        }

        CLASS -> {
            val state2 = insertDefault(src, tokPos, state)
            if (isTopDecl(tokPos, state2.stack)) {
                pushStack(tokPos, LayoutDelimiter.TopDeclHead, state2)
            } else {
                popStack(state2) { it == LayoutDelimiter.Property }
            }
        }

        WHERE -> when (stk?.layoutDelimiter) {
            LayoutDelimiter.TopDeclHead ->
                LayoutState(stk.tail, acc)
                    .let { insertToken(src, it) }
                    .let {
                        insertStart(
                            nextPos,
                            LayoutDelimiter.Where,
                            it
                        )
                    }

            LayoutDelimiter.Property ->
                insertToken(src, LayoutState(stk.tail, acc))

            else ->
                state
                    .let {
                        collapse(tokPos, it) { tokPos, lytPos, lyt ->
                            if (lyt == LayoutDelimiter.Do) true
                            else offsideEndP(tokPos, lytPos, lyt)
                        }
                    }
                    .let { insertToken(src, it) }
                    .let {
                        insertStart(
                            nextPos,
                            LayoutDelimiter.Where,
                            it
                        )
                    }
        }

        LPAREN -> state
            .let { insertDefault(src, tokPos, it) }
            .let { pushStack(tokPos, LayoutDelimiter.Paren, it) }

        LCURLY -> state
            .let { insertDefault(src, tokPos, it) }
            .let { pushStack(tokPos, LayoutDelimiter.Brace, it) }
            .let { pushStack(tokPos, LayoutDelimiter.Property, it) }

        RPAREN -> state
            .let {
                collapse(tokPos, it) { lyt -> isIndented(lyt) }
            }
            .let { state1 -> popStack(state1) { it == LayoutDelimiter.Paren } }
            .let { insertToken(src, it) }

        RCURLY -> state
            .let {
                collapse(tokPos, it) { lyt -> isIndented(lyt) }
            }
            .let { state1 -> popStack(state1) { it == LayoutDelimiter.Property } }
            .let { state1 -> popStack(state1) { it == LayoutDelimiter.Brace } }
            .let { insertToken(src, it) }

        LBRACK -> state
            .let { insertDefault(src, tokPos, it) }
            .let { pushStack(tokPos, LayoutDelimiter.Square, it) }

        RBRACK -> state
            .let {
                collapse(tokPos, it) { lyt -> isIndented(lyt) }
            }
            .let { state1 -> popStack(state1) { it == LayoutDelimiter.Square } }
            .let { insertToken(src, it) }

        IN -> {
            val (stk1, acc2) = collapse(tokPos, state) { lyt ->
                when (lyt) {
                    LayoutDelimiter.Let -> false
                    LayoutDelimiter.Ado -> false
                    else -> isIndented(lyt)
                }
            }
            val (_, lyt, stk2) = stk1 ?: return state
                .let { insertDefault(src, tokPos, it) }
                .let { state1 -> popStack(state1) { it == LayoutDelimiter.Property } }
            if (lyt == LayoutDelimiter.LetStmt && stk2?.layoutDelimiter == LayoutDelimiter.Ado) {
                return LayoutState(stk2.tail, acc2)
                    .let { insertEnd(tokPos, it) }
                    .let { insertEnd(tokPos, it) }
                    .let { insertToken(src, it) }
            } else if (isIndented(lyt)) {
                return LayoutState(stk2, acc2)
                    .let { insertEnd(tokPos, it) }
                    .let { insertToken(src, it) }
            } else {
                return state
                    .let { insertDefault(src, tokPos, it) }
                    .let { state1 -> popStack(state1) { it == LayoutDelimiter.Property } }
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
            ) { pushStack(tokPos, LayoutDelimiter.Case, it) }

        OF -> {
            val state2 = collapse(tokPos, state) { lyt -> isIndented(lyt) }
            return if (state2.stack?.layoutDelimiter == LayoutDelimiter.Case) {
                LayoutState(state2.stack.tail, state2.acc)
                    .let { insertToken(src, it) }
                    .let { insertStart(nextPos, LayoutDelimiter.Of, it) }
                    .let {
                        pushStack(
                            nextPos,
                            LayoutDelimiter.CaseBinders,
                            it
                        )
                    }
            } else {
                state2
                    .let { insertDefault(src, tokPos, it) }
                    .let { state1 -> popStack(state1) { it == LayoutDelimiter.Property } }
            }
        }

        BACKSLASH -> state
            .let { insertDefault(src, tokPos, it) }
            .let { pushStack(tokPos, LayoutDelimiter.LambdaBinders, it) }

        PIPE -> {
            val state2 = collapse(tokPos, state, ::offsideEndP)
            val (stk2, _) = state2
            when (stk2?.layoutDelimiter) {
                LayoutDelimiter.Of -> state2
                    .let {
                        pushStack(tokPos, LayoutDelimiter.CaseGuard, it)
                    }
                    .let { insertToken(src, it) }

                LayoutDelimiter.Let -> state2
                    .let {
                        pushStack(tokPos, LayoutDelimiter.DeclGuard, it)
                    }
                    .let { insertToken(src, it) }

                LayoutDelimiter.LetStmt -> state2
                    .let {
                        pushStack(tokPos, LayoutDelimiter.DeclGuard, it)
                    }
                    .let { insertToken(src, it) }

                LayoutDelimiter.Where -> state2
                    .let {
                        pushStack(tokPos, LayoutDelimiter.DeclGuard, it)
                    }
                    .let { insertToken(src, it) }

                else -> state.let { insertDefault(src, tokPos, it) }
            }
        }

        TICK -> {
            val state2 = collapse(tokPos, state) { lyt -> isIndented(lyt) }
            if (state2.stack?.layoutDelimiter == LayoutDelimiter.Tick) {
                LayoutState(state2.stack.tail, state2.acc)
                    .let { insertToken(src, it) }
            } else {
                state
                    .let { insertDefault(src, tokPos, it) }
                    .let { pushStack(tokPos, LayoutDelimiter.Tick, it) }
            }
        }

        STRING -> state
            .let { insertDefault(src, tokPos, it) }
            .let { state1 -> popStack(state1) { it == LayoutDelimiter.Property } }

        IF ->
            insertKwProperty(
                src,
                tokPos,
                state
            ) { pushStack(tokPos, LayoutDelimiter.If, it) }

        THEN -> {
            val state2 = collapse(tokPos, state) { lyt -> isIndented(lyt) }
            if (state2.stack?.layoutDelimiter == LayoutDelimiter.If) {
                LayoutState(state2.stack.tail, state2.acc)
                    .let { insertToken(src, it) }
                    .let { pushStack(tokPos, LayoutDelimiter.Then, it) }
            } else {
                insertDefault(src, tokPos, state)
                    .let { state1 -> popStack(state1) { it == LayoutDelimiter.Property } }
            }
        }

        ELSE -> {
            val state2 =
                collapse(tokPos, state) { lyt -> isIndented(lyt) }
            if (state2.stack?.layoutDelimiter == LayoutDelimiter.Then) {
                LayoutState(state2.stack.tail, state2.acc)
                    .let { insertToken(src, it) }
            } else {
                val state3 = collapse(tokPos, state, ::offsideP)
                if (isTopDecl(tokPos, state3.stack)) {
                    insertToken(src, state3)
                } else {
                    insertSep(tokPos, state3)
                        .let { insertToken(src, it) }
                        .let { state1 -> popStack(state1) { it == LayoutDelimiter.Property } }
                }
            }
        }

        else -> insertDefault(src, tokPos, state)
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
        acc += List(stack.count(::isIndented)) { layoutEnd }
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