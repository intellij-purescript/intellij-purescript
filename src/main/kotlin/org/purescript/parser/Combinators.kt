package org.purescript.parser

import com.intellij.ide.navbar.impl.fromOldExtensions
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

object Combinators {
    fun token(tokenType: IElementType): Parsec = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo = ParserInfo(
            context.position,
            setOf(this),
            null,
            context.eat(tokenType)
        )


        public override fun calcName() = tokenType.toString()
        override fun calcExpectedName() = setOf(tokenType.toString())
        override val canStartWithSet: TokenSet get() = TokenSet.create(tokenType)
        public override fun calcCanBeEmpty() = false
    }
    fun token(token: String): Parsec = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo =
            if (context.text() == token) {
                context.advance()
                ParserInfo(context.position, setOf(this), null, true)
            } else {
                ParserInfo(context.position, setOf(this), null, false)
            }

        public override fun calcName() = "\"" + token + "\""
        override fun calcExpectedName() = setOf("\"" + token + "\"")
        override val canStartWithSet: TokenSet get() = TokenSet.ANY
        public override fun calcCanBeEmpty() = false
    }
    fun seq(first:Parsec, vararg ps: Parsec): Parsec = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo {
            return ps.fold(first.parse(context)) {info, p ->
                if (info.success) info.merge(p.parse(context))
                else info
            }
        }

        public override fun calcName() = 
            all().joinToString(" ") { it.name }
        override fun calcExpectedName(): Set<String> {
            var ret = emptySet<String>()
            for (p in all()) {
                ret = ret + p.expectedName
                if (!p.canBeEmpty) return ret
            }
            return ret
        }

        override val canStartWithSet: TokenSet
            by lazy {
                var ret = TokenSet.EMPTY
                for (p in all()) {
                    ret =TokenSet.orSet( ret, p.canStartWithSet)
                    if (!p.canBeEmpty) return@lazy ret
                }
                ret
            }

        private fun all() = sequenceOf(first, *ps)

        public override fun calcCanBeEmpty() = 
            all().all { it.canBeEmpty }
    }
    fun choice(head: Parsec, vararg tail: Parsec) = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo {
            val start = context.position
            val headInfo: ParserInfo = head.tryToParse(context)
            if (start < context.position || headInfo.success) return headInfo
            val failed = mutableListOf(headInfo)
            for (p in tail) {
                val info = p.tryToParse(context)
                if (start < context.position || info.success) return info
                else failed.add(info)
            }
            return failed.reduce { acc, parserInfo ->
                when {
                    acc.position < parserInfo.position -> parserInfo
                    parserInfo.position < acc.position -> acc
                    else -> ParserInfo(
                        acc.position,
                        acc.expected + parserInfo.expected,
                        if (acc.errorMessage == null) parserInfo.errorMessage
                        else acc.errorMessage + ";" + parserInfo.errorMessage,
                        false
                    )
                }
            }
        }

        public override fun calcName(): String = buildString {
            append("(${head.name})")
            for (parsec in tail) append(" | (${parsec.name})")
        }

        override fun calcExpectedName(): Set<String> =
            tail.fold(head.expectedName) { acc, parsec -> acc + parsec.expectedName }

        override val canStartWithSet: TokenSet
            by lazy {
                TokenSet.orSet(
                    head.canStartWithSet,
                    *tail.map { it.canStartWithSet }.toTypedArray()
                )
            }


        public override fun calcCanBeEmpty(): Boolean {
            if (!head.canBeEmpty) {
                return false
            }
            for (parsec in tail) {
                if (!parsec.canBeEmpty) {
                    return false
                }
            }
            return true
        }
    }
    fun noneOrMore(p: Parsec) = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo {
            var info = ParserInfo(context.position, setOf(p), null, true)
            while (!context.eof()) {
                val position = context.position
                info = p.parse(context)
                if (info.success) {
                    if (position == context.position) {
                        // TODO: this should not be allowed.
                        val info2 = ParserInfo(
                            context.position,
                            info.expected,
                            null,
                            false
                        )
                        return info.merge(info2)
                    }
                } else {
                    return if (position == context.position) {
                        val info2 = ParserInfo(
                            context.position,
                            info.expected,
                            null,
                            true
                        )
                        info.merge(info2)
                    } else {
                        info
                    }
                }
            }
            return info
        }

        public override fun calcName() = "(" + p.name + ")*"
        override fun calcExpectedName() = p.expectedName
        override val canStartWithSet: TokenSet get() = p.canStartWithSet
        public override fun calcCanBeEmpty() = true
    }
    fun optional(p: Parsec) = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo {
            val position = context.position
            val info1 = p.parse(context)
            return if (info1.success) info1
            else ParserInfo(
                info1.position,
                info1.expected,
                null,
                context.position == position
            )
        }

        public override fun calcName() = "(" + p.name + ")?"
        override fun calcExpectedName() = p.expectedName
        override val canStartWithSet: TokenSet get() = p.canStartWithSet
        public override fun calcCanBeEmpty() = true
    }
    fun withRollback(p: Parsec) = object : Parsec() {
            override fun parse(context: ParserContext) =
                if (!p.canParse(context)) {
                    ParserInfo(context.position, setOf(p), null, false)
                } else {
                    val start = context.position
                    val pack = context.start()
                    val info = p.parse(context)
                    if (info.success) {
                        pack.drop()
                        info
                    } else {
                        pack.rollbackTo()
                        ParserInfo(start, info.expected, null, false)
                    }
                }
    
            public override fun calcName() = "try(" + p.name + ")"
            override fun calcExpectedName() = p.expectedName
            override val canStartWithSet: TokenSet get() = p.canStartWithSet
            public override fun calcCanBeEmpty(): Boolean = p.canBeEmpty
        }

    fun parens(p: Parsec) = token(LPAREN) + p + token(RPAREN)
    fun squares(p: Parsec) = token(LBRACK) + p + token(RBRACK)
    fun braces(p: Parsec) = token(LCURLY) + p + token(RCURLY)
    fun ref(init: Parsec.() -> Parsec) = ParsecRef(init)
    fun guard(
        p: Parsec,
        errorMessage: String,
        predicate: (String?) -> Boolean
    ) = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo {
            val pack = context.start()
            val start = context.position
            val info1 = p.parse(context)
            return if (info1.success) {
                val end = context.position
                val text = context.getText(start, end)
                if (!predicate.invoke(text)) {
                    ParserInfo(context.position, setOf(), errorMessage, false)
                } else {
                    pack.drop()
                    info1
                }
            } else {
                pack.rollbackTo()
                info1
            }
        }

        public override fun calcName() = p.name
        override fun calcExpectedName() = p.expectedName
        override val canStartWithSet: TokenSet get() = p.canStartWithSet
        public override fun calcCanBeEmpty() = p.canBeEmpty
    }
}