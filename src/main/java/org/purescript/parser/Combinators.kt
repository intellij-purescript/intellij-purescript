package org.purescript.parser

import com.intellij.psi.tree.IElementType

object Combinators {

    fun token(tokenType: IElementType): Parsec = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo =
            if (context.eat(tokenType)) {
                ParserInfo(context.position, this, true)
            } else {
                ParserInfo(context.position, this, false)
            }

        public override fun calcName() = tokenType.toString()
        override fun calcExpectedName() = setOf(tokenType.toString())
        override fun canStartWith(type: IElementType) = type === tokenType
        public override fun calcCanBeEmpty() = false
    }

    fun token(token: String): Parsec = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo =
            if (context.text() == token) {
                context.advance()
                ParserInfo(context.position, this, true)
            } else {
                ParserInfo(context.position, this, false)
            }

        public override fun calcName() = "\"" + token + "\""
        override fun calcExpectedName() = setOf("\"" + token + "\"")

        override fun canStartWith(type: IElementType) = true
        public override fun calcCanBeEmpty() = false
    }

    fun seq(p1: Parsec, p2: Parsec): Parsec = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo {
            val info = p1.parse(context)
            return if (info.success) {
                val info2 = p2.parse(context)
                ParserInfo.merge(info, info2, info2.success)
            } else {
                info
            }
        }
        public override fun calcName() = "${p1.name} ${p2.name}"
        override fun calcExpectedName() =
            if (p1.canBeEmpty()) {
                p1.expectedName!! + p2.expectedName!!
            } else {
                p1.expectedName!!
            }
        override fun canStartWith(type: IElementType) =
            if (p1.canBeEmpty()) {
                p1.canStartWith(type) || p2.canStartWith(type)
            } else {
                p1.canStartWith(type)
            }
        public override fun calcCanBeEmpty() =
            p1.canBeEmpty() && p2.canBeEmpty()
    }
    fun choice(head: Parsec, vararg tail: Parsec) = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo {
            val position = context.position
            var info: ParserInfo
            info =
                if (head.canBeEmpty() || head.canStartWith(context.peek())) {
                    head.parse(context)
                } else {
                    ParserInfo(position, head, false)
                }
            if (context.position > position || info.success) {
                return info
            }
            for (p2 in tail) {
                val info2: ParserInfo =
                    if (p2.canBeEmpty() || p2.canStartWith(context.peek())) {
                        p2.parse(context)
                    } else {
                        ParserInfo(position, p2, false)
                    }
                info =
                    ParserInfo.merge(info, info2, info2.success)
                if (context.position > position || info.success) {
                    return info
                }
            }
            return info
        }

        public override fun calcName(): String {
            // TODO: avoid unnecessary parentheses.
            val sb = StringBuilder()
            sb.append("(").append(head.name).append(")")
            for (parsec in tail) {
                sb.append(" | (").append(parsec.name).append(")")
            }
            return sb.toString()
        }

        override fun calcExpectedName(): Set<String> {
            var result = head.expectedName!!
            for (parsec in tail) {
                result = result + parsec.expectedName!!
            }
            return result
        }

        override fun canStartWith(type: IElementType): Boolean {
            if (head.canStartWith(type)) {
                return true
            }
            for (parsec in tail) {
                if (parsec.canStartWith(type)) {
                    return true
                }
            }
            return false
        }

        public override fun calcCanBeEmpty(): Boolean {
            if (!head.canBeEmpty()) {
                return false
            }
            for (parsec in tail) {
                if (!parsec.canBeEmpty()) {
                    return false
                }
            }
            return true
        }
    }

    fun manyOrEmpty(p: Parsec): Parsec = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo {
            var info = ParserInfo(context.position, p, true)
            while (!context.eof()) {
                val position = context.position
                info = p.parse(context)
                if (info.success) {
                    if (position == context.position) {
                        // TODO: this should not be allowed.
                        return ParserInfo.merge(
                            info,
                            ParserInfo(context.position, info, false),
                            false
                        )
                    }
                } else return if (position == context.position) {
                    ParserInfo.merge(
                        info,
                        ParserInfo(context.position, info, true),
                        true
                    )
                } else {
                    info
                }
            }
            return info
        }
        public override fun calcName() = "(" + p.name + ")*"
        override fun calcExpectedName() = p.expectedName!!
        override fun canStartWith(type: IElementType) = p.canStartWith(type)
        public override fun calcCanBeEmpty() = true
    }
    fun many1(p: Parsec) = p.then(manyOrEmpty(p))
    fun optional(p: Parsec) = object : Parsec() {
        override fun parse(context: ParserContext) = try {
            context.enterOptional()
            val position = context.position
            val info1 = p.parse(context)
            if (info1.success) {
                info1
            } else {
                val success = context.position == position
                ParserInfo(info1.position, info1, success)
            }
        } finally {
            context.exitOptional()
        }
        public override fun calcName() = "(" + p.name + ")?"
        override fun calcExpectedName() = p.expectedName!!
        override fun canStartWith(type: IElementType) = p.canStartWith(type)
        public override fun calcCanBeEmpty() = true
    }

    fun attempt(p: Parsec): Parsec = object : Parsec() {
        override fun parse(context: ParserContext) =
            if (!p.canBeEmpty() && !p.canStartWith(context.peek())) {
                ParserInfo(context.position, p, false)
            } else {
                val start = context.position
                val pack = context.start()
                val inAttempt = context.isInAttempt
                context.isInAttempt = true
                val info = p.parse(context)
                context.isInAttempt = inAttempt
                if (info.success) {
                    pack.drop()
                    info
                } else {
                    pack.rollbackTo()
                    ParserInfo(start, info, false)
                }
            }
        public override fun calcName() = "try(" + p.name + ")"
        override fun calcExpectedName() = p.expectedName!!
        override fun canStartWith(type: IElementType) = p.canStartWith(type)
        public override fun calcCanBeEmpty(): Boolean = p.canBeEmpty()
    }

    fun parens(p: Parsec) =
        token(PSTokens.LPAREN).then(p).then(token(PSTokens.RPAREN))

    fun squares(p: Parsec) =
        token(PSTokens.LBRACK).then(p).then(token(PSTokens.RBRACK))

    fun braces(p: Parsec) =
        token(PSTokens.LCURLY).then(p).then(token(PSTokens.RCURLY))

    fun sepBy1(p: Parsec, sep: IElementType) =
        p.then(attempt(manyOrEmpty(token(sep).then(p))))

    fun commaSep1(p: Parsec) = sepBy1(p, PSTokens.COMMA)
    fun sepBy(p: Parsec, sep: Parsec) =
        optional(p.then(manyOrEmpty(sep.then(p))))

    fun sepBy1(p: Parsec, sep: Parsec) = p.then(manyOrEmpty(sep.then(p)))
    fun commaSep(p: Parsec) = sepBy(p, token(PSTokens.COMMA))
    fun ref() = ParsecRef()
    fun guard(
        p: Parsec,
        predicate: (String?) -> Boolean,
        errorMessage: String
    ) = object : Parsec() {
        override fun parse(context: ParserContext): ParserInfo {
            val pack = context.start()
            val start = context.position
            val info1 = p.parse(context)
            if (info1.success) {
                val end = context.position
                val text = context.getText(start, end)
                if (!predicate.invoke(text)) {
                    return ParserInfo(context.position, errorMessage, false)
                }
                pack.drop()
                return info1
            }
            pack.rollbackTo()
            return info1
        }
        public override fun calcName() = p.name!!
        override fun calcExpectedName() = p.expectedName!!
        override fun canStartWith(type: IElementType) = p.canStartWith(type)
        public override fun calcCanBeEmpty() = p.canBeEmpty()
    }
}