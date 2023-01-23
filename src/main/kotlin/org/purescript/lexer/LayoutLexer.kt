@file:Suppress("ComplexRedundantLet")

package org.purescript.lexer

import com.intellij.lexer.DelegateLexer
import com.intellij.lexer.Lexer
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.TokenSet
import org.purescript.lexer.LayoutDelimiter.*
import org.purescript.lexer.token.SourcePos
import org.purescript.lexer.token.SourceToken
import org.purescript.parser.*


data class Lexeme(
    val token: SourceToken,
    val trailingWhitespace: List<SourceToken>
) {
    val tokens get() = listOf(token) + trailingWhitespace
    val start get() = token.start
    val end get() = trailingWhitespace.lastOrNull()?.end ?: token.end
    val value = token.value
    val asSuper get() = SuperToken(emptyList(), this)
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

fun offsideP(tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter) =
    lyt.isIndent && tokPos.column < lytPos.column


fun insertLayout(src: SuperToken, nextPos: SourcePos, stack: LayoutStack?)
    : LayoutState {
    val tokenValue = src.value
    val tokPos = src.start
    val state = LayoutState(stack, emptyList())
    val (stk, acc) = state

    return when (tokenValue) {
        LOWER, TYPE -> state
            .insertDefault(src, tokPos)
            .popStack { it == Property }

        OPERATOR -> state
            .collapse(tokPos, ::offsideP)
            .insertSep(tokPos)
            .insertToken(src)

        EQ -> state.collapse(tokPos) { lyt ->
            when (lyt) {
                Where -> true
                LayoutDelimiter.Let -> true
                LetStmt -> true
                else -> false
            }
        }.let {
            when (it.stack?.layoutDelimiter) {
                DeclGuard -> it.popStack().insertToken(src)
                else -> state.insertDefault(src, tokPos)
            }
        }

        COMMA -> state.collapse(tokPos) { it -> it.isIndent }.let {
            when (it.stack?.layoutDelimiter) {
                Brace -> it.insertToken(src).pushStack(tokPos, Property)
                else -> it.insertToken(src)
            }
        }

        DOT -> state.insertDefault(src, tokPos).let {
            when (it.stack?.layoutDelimiter) {
                Forall -> it.popStack()
                else -> it.pushStack(tokPos, Property)
            }
        }

        ARROW -> state
            .collapse(tokPos) { tokPos, lytPos, lyt ->
                when (lyt) {
                    Do -> true
                    Of -> false
                    else -> lyt.isIndent && tokPos.column <= lytPos.column
                }
            }.popStack {
                when (it) {
                    CaseBinders -> true
                    CaseGuard -> true
                    LambdaBinders -> true
                    else -> false
                }
            }.insertToken(src)


        FORALL -> state.insertKwProperty(
            src,
            tokPos
        ) { it: LayoutState -> it.pushStack(tokPos, Forall) }

        DATA -> {
            val state2 = state.insertDefault(src, tokPos)
            if (state2.isTopDecl(tokPos)) {
                state2.pushStack(tokPos, TopDecl)
            } else {
                state2.popStack { it == Property }
            }
        }

        CLASS -> {
            val state2 = state.insertDefault(src, tokPos)
            if (state2.isTopDecl(tokPos)) {
                state2.pushStack(tokPos, TopDeclHead)
            } else {
                state2.popStack { it == Property }
            }
        }

        WHERE -> when (stk?.layoutDelimiter) {
            TopDeclHead -> LayoutState(stk.tail, acc).insertToken(src)
                .insertStart(nextPos, Where)

            Property -> LayoutState(stk.tail, acc).insertToken(src)

            else ->
                state
                    .let {
                        it.collapse(tokPos) { tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter ->
                            if (lyt == Do) true
                            else lyt.isIndent && tokPos.column <= lytPos.column
                        }
                    }.insertToken(src)
                    .insertStart(
                        nextPos,
                        Where
                    )
        }

        LPAREN -> state.insertDefault(src, tokPos).pushStack(tokPos, Paren)
        LCURLY -> state.insertDefault(src, tokPos).pushStack(tokPos, Brace)
            .pushStack(tokPos, Property)

        RPAREN -> state
            .collapse(tokPos) { lyt: LayoutDelimiter -> lyt.isIndent }
            .popStack { it: LayoutDelimiter -> it == Paren }
            .insertToken(src)

        RCURLY -> state
            .collapse(tokPos) { lyt: LayoutDelimiter -> lyt.isIndent }
            .popStack { it: LayoutDelimiter -> it == Property }
            .popStack { it: LayoutDelimiter -> it == Brace }
            .insertToken(src)

        LBRACK -> state.insertDefault(src, tokPos).pushStack(tokPos, Square)
        RBRACK -> state
            .collapse(tokPos) { lyt: LayoutDelimiter -> lyt.isIndent }
            .popStack { it: LayoutDelimiter -> it == Square }
            .insertToken(src)

        IN -> {
            val (stk1, acc2) = state.collapse(tokPos) { lyt: LayoutDelimiter ->
                when (lyt) {
                    LayoutDelimiter.Let -> false
                    Ado -> false
                    else -> lyt.isIndent
                }
            }
            val (_, lyt, stk2) = stk1 ?: return state.insertDefault(src, tokPos)
                .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == Property } }
            if (lyt == LetStmt && stk2?.layoutDelimiter == Ado) {
                return LayoutState(stk2.tail, acc2)
                    .insertToken(tokPos.asEnd)
                    .insertToken(tokPos.asEnd)
                    .insertToken(src)
            } else if (lyt.isIndent) {
                return LayoutState(stk2, acc2)
                    .insertToken(tokPos.asEnd)
                    .insertToken(src)
            } else {
                return state.insertDefault(src, tokPos)
                    .popStack { it: LayoutDelimiter -> it == Property }
            }
        }

        LET -> {
            fun next(state: LayoutState): LayoutState {
                val (p, lyt, _) = state.stack ?: return state
                    .insertStart(
                        nextPos,
                        LayoutDelimiter.Let
                    )
                return when {
                    lyt == Do && p.column == tokPos.column ->
                        state.insertStart(nextPos, LetStmt)

                    lyt == Ado && p.column == tokPos.column ->
                        state.insertStart(nextPos, LetStmt)

                    else -> state.insertStart(nextPos, LayoutDelimiter.Let)
                }
            }

            return state.insertKwProperty(src, tokPos) { next(it) }
        }

        DO ->
            state.insertKwProperty(src, tokPos) { it.insertStart(nextPos, Do) }

        ADO ->
            state.insertKwProperty(src, tokPos) { it.insertStart(nextPos, Ado) }

        CASE ->
            state
                .insertKwProperty(
                    src,
                    tokPos
                ) { it: LayoutState ->
                    it.pushStack(
                        tokPos,
                        LayoutDelimiter.Case
                    )
                }

        OF -> {
            val state2 = state.collapse(tokPos) { lyt: LayoutDelimiter ->
                lyt
                    .isIndent
            }
            return if (state2.stack?.layoutDelimiter == LayoutDelimiter.Case) {
                LayoutState(state2.stack.tail, state2.acc)
                    .insertToken(src)
                    .insertStart(nextPos, Of)
                    .pushStack(
                        nextPos,
                        CaseBinders
                    )
            } else {
                state2
                    .insertDefault(src, tokPos)
                    .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == Property } }
            }
        }

        BACKSLASH -> state
            .insertDefault(src, tokPos)
            .pushStack(tokPos, LambdaBinders)

        PIPE -> {
            val state2 =
                state.collapse(tokPos) { tokPos, lytPos, lyt -> lyt.isIndent && tokPos.column <= lytPos.column }
            when (state2.stack?.layoutDelimiter) {
                Of -> state2.pushStack(tokPos, CaseGuard).insertToken(src)
                LayoutDelimiter.Let -> state2
                    .pushStack(tokPos, DeclGuard)
                    .insertToken(src)

                LetStmt -> state2.pushStack(tokPos, DeclGuard).insertToken(src)
                Where -> state2.pushStack(tokPos, DeclGuard).insertToken(src)
                else -> state.insertDefault(src, tokPos)
            }
        }

        TICK -> {
            val state2 = state.collapse(tokPos) { lyt -> lyt.isIndent }
            if (state2.stack?.layoutDelimiter == Tick) {
                LayoutState(state2.stack.tail, state2.acc).insertToken(src)
            } else {
                state.insertDefault(src, tokPos).pushStack(tokPos, Tick)
            }
        }

        STRING -> state
            .insertDefault(src, tokPos)
            .popStack { it: LayoutDelimiter -> it == Property }

        IF -> state.insertKwProperty(
            src,
            tokPos
        ) { it: LayoutState -> it.pushStack(tokPos, If) }

        THEN -> {
            val state2 = state.collapse(tokPos) { lyt -> lyt.isIndent }
            if (state2.stack?.layoutDelimiter == If) {
                LayoutState(state2.stack.tail, state2.acc)
                    .insertToken(src)
                    .pushStack(tokPos, Then)
            } else {
                state
                    .insertDefault(src, tokPos)
                    .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == Property } }
            }
        }

        ELSE -> {
            val state2 = state.collapse(tokPos) { lyt -> lyt.isIndent }
            if (state2.stack?.layoutDelimiter == Then) {
                state2.popStack().insertToken(src)
            } else {
                val state3 = state.collapse(tokPos, ::offsideP)
                if (state3.isTopDecl(tokPos)) {
                    state3.insertToken(src)
                } else {
                    state3
                        .insertSep(tokPos)
                        .insertToken(src)
                        .popStack { it == Property }
                }
            }
        }

        else -> state.insertDefault(src, tokPos)
    }
}

fun lex(tokens: List<SuperToken>): List<SuperToken> {
    val sourcePos = SourcePos(0, 0, 0)
    var stack: LayoutStack? = LayoutStack(sourcePos, Root, null)
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
        val layoutEnd = startPos.asEnd
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