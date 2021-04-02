package org.purescript.lexer

import com.intellij.lexer.DelegateLexer
import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType
import org.purescript.lexer.token.SourcePos
import org.purescript.lexer.token.SourceRange
import org.purescript.lexer.token.SourceToken
import org.purescript.parser.PSTokens
import org.purescript.psi.PSElementType

data class LayoutStack(
    val sourcePos: SourcePos,
    val layoutDelimiter: LayoutDelimiter,
    val tail: LayoutStack?
)

data class LayoutState(
    val stack: LayoutStack?,
    val acc: List<Pair<SourceToken, LayoutStack?>>
)

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

fun currentIndent(stk: LayoutStack?): SourcePos? {
    return stk?.let {
        if (isIndented(it.layoutDelimiter)) {
            it.sourcePos
        } else {
            currentIndent(it.tail)
        }
    }
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
    stk?.tail == null || stk.tail.tail != null -> {
        false
    }
    stk.tail.layoutDelimiter != LayoutDelimiter.Root -> {
        false
    }
    stk.layoutDelimiter != LayoutDelimiter.Where -> {
        false
    }
    else -> {
        tokPos.column == stk.sourcePos.column
    }
}

fun lytToken(pos: SourcePos, value: PSElementType): SourceToken = SourceToken(
    range = SourceRange(pos, pos),
    value = value
)

fun insertLayout(
    src: SourceToken,
    nextPos: SourcePos,
    stack: LayoutStack?
): LayoutState {
    val range = src.range
    val tok = src.value
    val tokPos = range.start
    fun offsideP(lytPos: SourcePos, lyt: LayoutDelimiter): Boolean {
        return isIndented(lyt) && tokPos.column < lytPos.column
    }

    fun collapse(
        p: (SourcePos, LayoutDelimiter) -> Boolean,
        state: LayoutState
    ): LayoutState {
        val (stack, acc) = state
        if (stack != null) {
            val (lytPos, lyt, tail) = stack
            if (p(lytPos, lyt)) {
                return collapse(
                    p,
                    LayoutState(
                        tail,
                        if (isIndented(lyt)) {
                            val acc2 = acc.toMutableList()
                            acc2 += lytToken(
                                tokPos,
                                PSTokens.LAYOUT_END
                            ) to tail
                            acc2
                        } else {
                            acc
                        }
                    )
                )
            } else {
                return state
            }
        } else {
            return state
        }
    }

    fun sepP(lytPos: SourcePos): Boolean =
        tokPos.column == lytPos.column && tokPos.line != lytPos.line

    fun insertToken(token: SourceToken, state: LayoutState): LayoutState {
        val (stk, acc) = state
        val acc2 = acc.toMutableList()
        acc2 += token to stk
        return LayoutState(stk, acc2)
    }

    fun pushStack(
        lytPos: SourcePos,
        lyt: LayoutDelimiter,
        state: LayoutState
    ): LayoutState =
        LayoutState(LayoutStack(lytPos, lyt, state.stack), state.acc)

    fun identSepP(lytPos: SourcePos, lyt: LayoutDelimiter): Boolean =
        isIndented(lyt) && sepP(lytPos)

    fun insertSep(state: LayoutState): LayoutState {
        val (stk, acc) = state
        val (lytPos, lyt, tail) = stk ?: return state
        val sepTok = lytToken(tokPos, PSTokens.LAYOUT_SEP)
        return when {
            LayoutDelimiter.TopDecl == lyt && sepP(lytPos) ->
                insertToken(sepTok, LayoutState(tail, acc))
            LayoutDelimiter.TopDeclHead == lyt && sepP(lytPos) ->
                insertToken(sepTok, LayoutState(tail, acc))
            identSepP(lytPos, lyt) -> when (lyt) {
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

    fun insertDefault(state: LayoutState): LayoutState {
        return insertToken(src, insertSep(collapse(::offsideP, state)))
    }


    fun popStack(
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

    fun find(
        stack: LayoutStack?,
        function: (LayoutStack) -> Boolean
    ): LayoutStack? {
        if (stack == null) {
            return null
        } else if (function(stack)) {
            return stack
        } else {
            return find(stack.tail, function)
        }
    }

    fun insertStart(lyt: LayoutDelimiter, state: LayoutState): LayoutState {
        val stk = state.stack
        val (pos, _, _) = find(stk) { stack: LayoutStack ->
            isIndented(stack.layoutDelimiter)
        } ?: return insertToken(
            lytToken(nextPos, PSTokens.LAYOUT_START),
            pushStack(nextPos, lyt, state)
        )
        return if (nextPos.column <= pos.column) {
            state
        } else {
            insertToken(
                lytToken(nextPos, PSTokens.LAYOUT_START),
                pushStack(nextPos, lyt, state)
            )
        }
    }

    fun insertEnd(indent: Int, state: LayoutState): LayoutState =
        insertToken(lytToken(tokPos, PSTokens.LAYOUT_END), state)

    fun insertKwProperty(
        k: (LayoutState) -> LayoutState,
        state: LayoutState
    ): LayoutState {
        val state2 = insertDefault(state)
        return if (state.stack?.layoutDelimiter == LayoutDelimiter.Property) {
            LayoutState(state2.stack?.tail, state2.acc)
        } else {
            k(state2)
        }
    }

    fun insert(state: LayoutState): LayoutState {
        val (stk, acc) = state
        fun offsideEndP(lytPos: SourcePos, lyt: LayoutDelimiter): Boolean {
            return isIndented(lyt) && tokPos.column <= lytPos.column
        }

        fun indentedP(ignore: SourcePos, lyt: LayoutDelimiter): Boolean =
            isIndented(lyt)

        return when (tok) {
            PSTokens.DATA -> {
                val state2 = insertDefault(state)
                if (isTopDecl(tokPos, state.stack)) {
                    pushStack(tokPos, LayoutDelimiter.TopDecl, state2)
                } else {
                    popStack(state2) { it == LayoutDelimiter.Property }
                }
            }

            PSTokens.CLASS -> {
                val state2 = insertDefault(state)
                if (isTopDecl(tokPos, state2.stack)) {
                    pushStack(tokPos, LayoutDelimiter.TopDeclHead, state2)
                } else {
                    popStack(state2) { it == LayoutDelimiter.Property }
                }
            }

            PSTokens.WHERE -> {
                fun whereP(lytPos: SourcePos, lyt: LayoutDelimiter): Boolean =
                    if (lyt == LayoutDelimiter.Do) true
                    else offsideEndP(lytPos, lyt)
                val (_, lyt, stk2) = stk ?: return state
                when (lyt) {
                    LayoutDelimiter.TopDeclHead ->
                        LayoutState(stk2, acc)
                            .let { insertToken(src, it) }
                            .let { insertStart(LayoutDelimiter.Where, it) }
                    LayoutDelimiter.Property ->
                        insertToken(src, LayoutState(stk2, acc))
                    else ->
                        state
                            .let { collapse(::whereP, it) }
                            .let { insertToken(src, it) }
                            .let { insertStart(LayoutDelimiter.Where, it) }
                }
            }

            PSTokens.IN -> {
                fun inP(ignore: SourcePos, lyt: LayoutDelimiter): Boolean =
                    when (lyt) {
                        LayoutDelimiter.Let -> false
                        LayoutDelimiter.Ado -> false
                        else -> isIndented(lyt)
                    }

                val (stk, acc2) = collapse(::inP, state)
                val (pos1, lyt, stk2) = stk ?: return state
                    .let(::insertDefault)
                    .let { popStack(it) { it == LayoutDelimiter.Property } }
                if (lyt == LayoutDelimiter.LetStmt && stk2?.layoutDelimiter == LayoutDelimiter.Ado) {
                    return LayoutState(stk2.tail, acc2)
                        .let { insertEnd(pos1.column, it) }
                        .let { insertEnd(stk2.sourcePos.column, it) }
                        .let { insertToken(src, it) }
                } else if (isIndented(lyt)) {
                    return LayoutState(stk2, acc2)
                        .let { insertEnd(pos1.column, it) }
                        .let { insertToken(src, it) }
                } else {
                    return state
                        .let(::insertDefault)
                        .let { popStack(it) { it == LayoutDelimiter.Property } }
                }
            }

            PSTokens.LET -> {
                fun next(state: LayoutState): LayoutState {
                    val (p, lyt, _) = state.stack ?: return insertStart(
                        LayoutDelimiter.Let,
                        state
                    )
                    return when {
                        lyt == LayoutDelimiter.Do && p.column == tokPos.column ->
                            insertStart(LayoutDelimiter.LetStmt, state)
                        lyt == LayoutDelimiter.Ado && p.column == tokPos.column ->
                            insertStart(LayoutDelimiter.LetStmt, state)
                        else -> insertStart(LayoutDelimiter.Let, state)
                    }
                }

                return insertKwProperty(::next, state)
            }

            PSTokens.DO ->
                insertKwProperty({ insertStart(LayoutDelimiter.Do, it) }, state)

            PSTokens.ADO ->
                insertKwProperty(
                    { insertStart(LayoutDelimiter.Ado, it) },
                    state
                )

            PSTokens.CASE ->
                insertKwProperty(
                    { pushStack(tokPos, LayoutDelimiter.Case, it) },
                    state
                )

            PSTokens.OF -> {
                val state2 = collapse(::indentedP, state)
                return if (state2.stack?.layoutDelimiter == LayoutDelimiter.Case) {
                    LayoutState(state2.stack.tail, state2.acc)
                        .let { insertToken(src, it) }
                        .let { insertStart(LayoutDelimiter.Of, it) }
                        .let {
                            pushStack(
                                nextPos,
                                LayoutDelimiter.CaseBinders,
                                it
                            )
                        }
                } else {
                    state2
                        .let(::insertDefault)
                        .let { popStack(it) { it == LayoutDelimiter.Property } }
                }
            }

            PSTokens.IF ->
                insertKwProperty(
                    { pushStack(tokPos, LayoutDelimiter.If, it) },
                    state
                )

            PSTokens.THEN -> {
                val state2 = collapse(::indentedP, state)
                if (state2.stack?.layoutDelimiter == LayoutDelimiter.If) {
                    LayoutState(state2.stack.tail, state2.acc)
                        .let { insertToken(src, it) }
                        .let { pushStack(tokPos, LayoutDelimiter.Then, it) }
                } else {
                    insertDefault(state)
                        .let { popStack(it) { it == LayoutDelimiter.Property } }
                }
            }

            PSTokens.ELSE -> {
                val state2 = collapse(::indentedP, state)
                if (state2.stack?.layoutDelimiter == LayoutDelimiter.Then) {
                    LayoutState(state2.stack.tail, state2.acc)
                        .let { insertToken(src, it) }
                } else {
                    val state3 = collapse(::offsideP, state)
                    if (isTopDecl(tokPos, state3.stack)) {
                        insertToken(src, state3)
                    } else {
                        insertSep(state3)
                            .let { insertToken(src, it) }
                            .let { popStack(it) { it == LayoutDelimiter.Property } }
                    }
                }
            }

            PSTokens.FORALL -> state
                .let { insertDefault(it) }
                .let { pushStack(tokPos, LayoutDelimiter.LambdaBinders, it) }

            PSTokens.BACKSLASH -> state
                .let { insertDefault(it) }
                .let { pushStack(tokPos, LayoutDelimiter.LambdaBinders, it) }

            PSTokens.ARROW -> {
                fun arrowP(lytPos: SourcePos, lyt: LayoutDelimiter): Boolean =
                    when (lyt) {
                        LayoutDelimiter.Do -> true
                        LayoutDelimiter.Of -> false
                        else -> offsideEndP(lytPos, lyt)
                    }

                fun guardP(lyt: LayoutDelimiter): Boolean = when (lyt) {
                    LayoutDelimiter.CaseBinders -> true
                    LayoutDelimiter.CaseGuard -> true
                    LayoutDelimiter.LambdaBinders -> true
                    else -> false
                }
                state
                    .let { collapse(::arrowP, it) }
                    .let { popStack(it, ::guardP) }
                    .let { insertToken(src, it) }
            }

            PSTokens.EQ -> {
                fun equalsP(ignore: SourcePos, lyt: LayoutDelimiter): Boolean =
                    when (lyt) {
                        LayoutDelimiter.Where -> true
                        LayoutDelimiter.Let -> true
                        LayoutDelimiter.LetStmt -> true
                        else -> false
                    }
                val (stk2, acc2) = collapse(::equalsP, state)
                when (stk2?.layoutDelimiter) {
                    LayoutDelimiter.DeclGuard -> LayoutState(stk2.tail, acc2)
                        .let { insertToken(src, it) }
                    else -> insertDefault(state)
                }
            }

            PSTokens.PIPE -> {
                val state2 = collapse(::offsideEndP, state)
                val (stk, _) = state2
                when (stk?.layoutDelimiter) {
                    LayoutDelimiter.Of -> state2
                        .let {
                            pushStack(
                                tokPos,
                                LayoutDelimiter.CaseGuard,
                                it
                            )
                        }
                        .let { insertToken(src, it) }
                    LayoutDelimiter.Let -> state2
                        .let {
                            pushStack(
                                tokPos,
                                LayoutDelimiter.DeclGuard,
                                it
                            )
                        }
                        .let { insertToken(src, it) }
                    LayoutDelimiter.LetStmt -> state2
                        .let {
                            pushStack(
                                tokPos,
                                LayoutDelimiter.DeclGuard,
                                it
                            )
                        }
                        .let { insertToken(src, it) }
                    LayoutDelimiter.Where -> state2
                        .let {
                            pushStack(
                                tokPos,
                                LayoutDelimiter.DeclGuard,
                                it
                            )
                        }
                        .let { insertToken(src, it) }
                    else -> state.let { insertDefault(it) }
                }
            }

            PSTokens.TICK -> {
                val state2 = collapse(::indentedP, state)
                if (state2.stack?.layoutDelimiter == LayoutDelimiter.Tick) {
                    LayoutState(state2.stack.tail, state2.acc)
                        .let { insertToken(src, it) }
                } else {
                    state
                        .let { insertDefault(it) }
                        .let { pushStack(tokPos, LayoutDelimiter.Tick, it) }
                }
            }

            PSTokens.COMMA -> {
                val state2 = collapse(::indentedP, state)
                if (state2.stack?.layoutDelimiter == LayoutDelimiter.Brace) {
                    state2
                        .let { insertToken(src, it) }
                        .let { pushStack(tokPos, LayoutDelimiter.Property, it) }
                } else {
                    state2
                        .let { insertToken(src, it) }
                }
            }

            PSTokens.DOT -> {
                val state2 = insertDefault(state)
                if (state2.stack?.layoutDelimiter == LayoutDelimiter.Forall) {
                    LayoutState(state2.stack.tail, state2.acc)
                } else {
                    state2
                        .let { pushStack(tokPos, LayoutDelimiter.Property, it) }
                }
            }

            PSTokens.LPAREN -> state
                .let { insertDefault(it) }
                .let { pushStack(tokPos, LayoutDelimiter.Paren, it) }

            PSTokens.LCURLY -> state
                .let { insertDefault(it) }
                .let { pushStack(tokPos, LayoutDelimiter.Brace, it) }
                .let { pushStack(tokPos, LayoutDelimiter.Property, it) }

            PSTokens.LBRACK -> state
                .let { insertDefault(it) }
                .let { pushStack(tokPos, LayoutDelimiter.Square, it) }

            PSTokens.RPAREN -> state
                .let { collapse(::indentedP, it) }
                .let { popStack(it) { it == LayoutDelimiter.Paren } }
                .let { insertToken(src, it) }

            PSTokens.RCURLY -> state
                .let { collapse(::indentedP, it) }
                .let { popStack(it) { it == LayoutDelimiter.Property } }
                .let { popStack(it) { it == LayoutDelimiter.Brace } }
                .let { insertToken(src, it) }

            PSTokens.RBRACK -> state
                .let { collapse(::indentedP, it) }
                .let { popStack(it) { it == LayoutDelimiter.Square } }
                .let { insertToken(src, it) }

            PSTokens.STRING -> state
                .let { insertDefault(it) }
                .let { popStack(it) { it == LayoutDelimiter.Property } }

            PSTokens.IDENT -> state
                .let { insertDefault(it) }
                .let { popStack(it) { it == LayoutDelimiter.Property } }

            PSTokens.OPERATOR -> state
                .let { collapse(::offsideP, it) }
                .let { insertSep(it) }
                .let { insertToken(src, it) }

            else -> insertDefault(state)
        }
    }
    return insert(LayoutState(stack, emptyList()))
}

data class TokenStep(
    val token: SourceToken,
    val pos: SourcePos,
    val stack: LayoutStack?
)

typealias TokenStream = Sequence<TokenStep>

fun unwindLayout(
    pos: SourcePos,
    stkIn: LayoutStack?
): TokenStream {
    return sequence {
        var stk = stkIn;
        while (stk != null) {
            var (_, lyt, tl) = stk
            if (lyt == LayoutDelimiter.Root) break
            if (isIndented(lyt)) {
                val token = lytToken(pos, PSTokens.LAYOUT_END)
                val tokenStep = TokenStep(token, pos, tl)
                yield(tokenStep)
            }
            stk = tl
        }
    }
}


fun tokensToTokenStep(
    tokens: List<Pair<SourceToken, LayoutStack?>>,
    lastPos: SourcePos
): List<TokenStep> {
    val positions = tokens
        .map { it.first.range.start }
        .drop(1) + listOf(lastPos)
    return tokens
        .zip(positions)
        .map { (token, start) -> TokenStep(token.first, start, token.second) }
}

fun lex(
    tokens: List<SourceToken>
): List<SourceToken> {
    val sourcePos = SourcePos(0, 0, 0)
    var stack: LayoutStack? = LayoutStack(
        sourcePos,
        LayoutDelimiter.Root,
        null
    )

    fun go(
        stack: LayoutStack?,
        startPos: SourcePos,
        tokens: Iterator<SourceToken>
    ): TokenStream {
        if (!tokens.hasNext()) {
            return unwindLayout(startPos, stack)
        } else {
            val posToken = tokens.next()
            val nextStart = posToken.range.end
            val (nextStack, toks) = insertLayout(posToken, nextStart, stack)
            return sequence {
                yieldAll(tokensToTokenStep(toks, nextStart))
                yieldAll(go(nextStack, nextStart, tokens))
            }
        }
    }
    return go(stack, sourcePos, tokens.iterator()).map { it.token }.toList()
}

fun correctLineAndColumn(
    source: CharSequence
): (SourceToken, SourceToken) -> SourceToken {
    fun go(
        previous: SourceToken,
        current: SourceToken,
    ): SourceToken {
        val (_, start) = previous.range
        val (_, end) = current.range
        // might be expensive
        val subSequence = source
            .subSequence(start.offset, end.offset)
        val newlineIndex = subSequence
            .lastIndexOf("\n")
        val noNewline = newlineIndex == -1
        val tokenLength = end.offset - start.offset
        val newEnd = if (noNewline) {
            SourcePos(
                start.line,
                start.column + tokenLength,
                end.offset
            )
        } else {
            SourcePos(
                start.line + subSequence.count { it == '\n' },
                tokenLength - newlineIndex - 1,
                end.offset
            )
        }
        return SourceToken(
            range = SourceRange(start, newEnd),
            value = current.value
        )
    }
    return ::go
}

fun posFromOffset(offset: Int): SourcePos {
    return SourcePos(0, 0, offset)
}

fun rangeFromOffsets(start: Int, end: Int): SourceRange {
    return SourceRange(posFromOffset(start), posFromOffset(end))
}

fun getTokens(lexer: Lexer): Sequence<SourceToken> {
    return generateSequence {
        val sourceToken: SourceToken? = lexer.tokenType?.let { value ->
            SourceToken(
                range = rangeFromOffsets(
                    lexer.tokenStart,
                    lexer.tokenEnd
                ),
                value = value
            )
        }
        lexer.advance()
        sourceToken
    }
}

class LayoutLexer(delegate: Lexer) : DelegateLexer(delegate) {

    private var tokens: List<SourceToken> = listOf<SourceToken>()
    private var index = 0;
    private val root = SourceToken(rangeFromOffsets(0, 0), PSTokens.WS)

    override fun start(
        buffer: CharSequence,
        startOffset: Int,
        endOffset: Int,
        initialState: Int
    ) {
        super.start(buffer, startOffset, endOffset, initialState)
        this.tokens = getTokens(delegate)
            .runningFold(root, correctLineAndColumn(buffer))
            .drop(1)
            .toList()
            .let(::lex)
        index = 0
    }

    override fun advance() {
        index ++
    }

    override fun getTokenType(): IElementType? {
        return tokens.getOrNull(index)?.value
    }

    override fun getTokenEnd(): Int {
        return  tokens[index].range.end.offset
    }

    override fun getTokenStart(): Int {
        return tokens[index].range.start.offset
    }
}