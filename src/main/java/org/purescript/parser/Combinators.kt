package org.purescript.parser

import com.intellij.psi.tree.IElementType

object Combinators {

    private fun strings(name1: String): HashSet<String?> {
        val result: HashSet<String?> = LinkedHashSet()
        result.add(name1)
        return result
    }

    private fun strings(names1: HashSet<String?>, names2: HashSet<String?>): HashSet<String?> {
        val result: HashSet<String?> = LinkedHashSet()
        result.addAll(names1)
        result.addAll(names2)
        return result
    }

    fun token(tokenType: IElementType): Parsec {
        return object : Parsec() {
            override fun parse(context: ParserContext): ParserInfo {
                return if (context.eat(tokenType)) {
                    ParserInfo(context.position, this, true)
                } else ParserInfo(context.position, this, false)
            }

            public override fun calcName(): String {
                return tokenType.toString()
            }

            override fun calcExpectedName(): HashSet<String?> {
                return strings(tokenType.toString())
            }

            override fun canStartWith(type: IElementType): Boolean {
                return type === tokenType
            }

            public override fun calcCanBeEmpty(): Boolean {
                return false
            }
        }
    }

    fun token(token: String): Parsec {
        return object : Parsec() {
            override fun parse(context: ParserContext): ParserInfo {
                if (context.text() == token) {
                    context.advance()
                    return ParserInfo(context.position, this, true)
                }
                return ParserInfo(context.position, this, false)
            }

            public override fun calcName(): String {
                return "\"" + token + "\""
            }

            override fun calcExpectedName(): HashSet<String?> {
                return strings("\"" + token + "\"")
            }

            override fun canStartWith(type: IElementType): Boolean {
                return true
            }

            public override fun calcCanBeEmpty(): Boolean {
                return false
            }
        }
    }

    fun seq(p1: Parsec, p2: Parsec): Parsec {
        return object : Parsec() {
            override fun parse(context: ParserContext): ParserInfo {
                val info = p1.parse(context)
                if (info.success) {
                    val info2 = p2.parse(context)
                    return ParserInfo.merge(
                        info,
                        info2,
                        info2.success
                    )
                }
                return info
            }

            public override fun calcName(): String {
                val name1 = p1.name
                val name2 = p2.name
                return "$name1 $name2"
            }

            override fun calcExpectedName(): HashSet<String?> {
                return if (p1.canBeEmpty()) {
                    strings(p1.expectedName!!, p2.expectedName!!)
                } else p1.expectedName!!
            }

            override fun canStartWith(type: IElementType): Boolean {
                return if (p1.canBeEmpty()) {
                    p1.canStartWith(type) || p2.canStartWith(type)
                } else p1.canStartWith(type)
            }

            public override fun calcCanBeEmpty(): Boolean {
                return p1.canBeEmpty() && p2.canBeEmpty()
            }
        }
    }

    fun choice(head: Parsec, vararg tail: Parsec): Parsec {
        return object : Parsec() {
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
                    val info2: ParserInfo = if (p2.canBeEmpty() || p2.canStartWith(context.peek())) {
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

            override fun calcExpectedName(): HashSet<String?> {
                val result: HashSet<String?> = LinkedHashSet()
                result.addAll(head.expectedName!!)
                for (parsec in tail) {
                    result.addAll(parsec.expectedName!!)
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
    }

    fun manyOrEmpty(p: Parsec): Parsec {
        return object : Parsec() {
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

            public override fun calcName(): String {
                return "(" + p.name + ")*"
            }

            override fun calcExpectedName(): HashSet<String?> {
                return p.expectedName!!
            }

            override fun canStartWith(type: IElementType): Boolean {
                return p.canStartWith(type)
            }

            public override fun calcCanBeEmpty(): Boolean {
                return true
            }
        }
    }

    fun many1(p: Parsec): Parsec {
        return p.then(manyOrEmpty(p))
    }

    fun optional(p: Parsec): Parsec {
        return object : Parsec() {
            override fun parse(context: ParserContext): ParserInfo {
                return try {
                    context.enterOptional()
                    val position = context.position
                    val info1 = p.parse(context)
                    if (info1.success) {
                        info1
                    } else ParserInfo(
                        info1.position,
                        info1,
                        context.position == position
                    )
                } finally {
                    context.exitOptional()
                }
            }

            public override fun calcName(): String {
                // TODO: avoid unnecessary parentheses.
                return "(" + p.name + ")?"
            }

            override fun calcExpectedName(): HashSet<String?> {
                return p.expectedName!!
            }

            override fun canStartWith(type: IElementType): Boolean {
                return p.canStartWith(type)
            }

            public override fun calcCanBeEmpty(): Boolean {
                return true
            }
        }
    }

    fun attempt(p: Parsec): Parsec {
        return object : Parsec() {
            override fun parse(context: ParserContext): ParserInfo {
                if (!p.canBeEmpty() && !p.canStartWith(context.peek())) {
                    return ParserInfo(context.position, p, false)
                }
                val start = context.position
                val pack = context.start()
                val inAttempt = context.isInAttempt
                context.isInAttempt = true
                val info = p.parse(context)
                context.isInAttempt = inAttempt
                if (info.success) {
                    pack.drop()
                    return info
                }
                pack.rollbackTo()
                return ParserInfo(start, info, false)
            }

            public override fun calcName(): String {
                // TODO: avoid unnecessary parentheses.
                return "try(" + p.name + ")"
            }

            override fun calcExpectedName(): HashSet<String?> {
                return p.expectedName!!
            }

            override fun canStartWith(type: IElementType): Boolean {
                return p.canStartWith(type)
            }

            public override fun calcCanBeEmpty(): Boolean {
                return p.canBeEmpty()
            }
        }
    }

    fun parens(pInit: Parsec): Parsec {
        var p = pInit
        p = until(p, PSTokens.RPAREN)
        return token(PSTokens.LPAREN).then(indented(p))
            .then(indented(token(PSTokens.RPAREN)))
    }

    fun squares(pInit: Parsec): Parsec {
        var p = pInit
        p = until(p, PSTokens.RPAREN)
        return token(PSTokens.LBRACK).then(indented(p))
            .then(indented(token(PSTokens.RBRACK)))
    }

    fun braces(pInit: Parsec): Parsec {
        var p = pInit
        p = until(p, PSTokens.RPAREN)
        return token(PSTokens.LCURLY).then(indented(p))
            .then(indented(token(PSTokens.RCURLY)))
    }

    fun indented(p: Parsec): Parsec {
        return object : Parsec() {
            override fun parse(context: ParserContext): ParserInfo {
                return if (context.column > context.indentationLevel.peek()) {
                    p.parse(context)
                } else ParserInfo(context.position, this, false)
            }

            public override fun calcName(): String {
                return "indented (" + p.name + ")"
            }

            override fun calcExpectedName(): HashSet<String?> {
                return p.expectedName!!
            }

            override fun canStartWith(type: IElementType): Boolean {
                return p.canStartWith(type)
            }

            public override fun calcCanBeEmpty(): Boolean {
                return p.canBeEmpty()
            }
        }
    }

    fun same(p: Parsec): Parsec {
        return object : Parsec() {
            override fun parse(context: ParserContext): ParserInfo {
                return if (context.column == context.indentationLevel.peek()) {
                    p.parse(context)
                } else ParserInfo(context.position, this, false)
            }

            public override fun calcName(): String {
                return "not indented (" + p.name + ")"
            }

            override fun calcExpectedName(): HashSet<String?> {
                return p.expectedName!!
            }

            override fun canStartWith(type: IElementType): Boolean {
                return p.canStartWith(type)
            }

            public override fun calcCanBeEmpty(): Boolean {
                return p.canBeEmpty()
            }
        }
    }

    fun mark(p: Parsec): Parsec {
        return object : Parsec() {
            override fun parse(context: ParserContext): ParserInfo {
                context.pushIndentationLevel()
                return try {
                    p.parse(context)
                } finally {
                    context.popIndentationLevel()
                }
            }

            public override fun calcName(): String {
                return "not indented (" + p.name + ")"
            }

            override fun calcExpectedName(): HashSet<String?> {
                return p.expectedName!!
            }

            override fun canStartWith(type: IElementType): Boolean {
                return p.canStartWith(type)
            }

            public override fun calcCanBeEmpty(): Boolean {
                return p.canBeEmpty()
            }
        }
    }

    fun sepBy1(p: Parsec, sep: IElementType): Parsec {
        var p = p
        p = until(p, sep)
        return p.then(attempt(manyOrEmpty(token(sep).then(p))))
    }

    fun commaSep1(p: Parsec): Parsec {
        return sepBy1(p, PSTokens.COMMA)
    }

    fun sepBy(p: Parsec, sep: Parsec): Parsec {
        return optional(p.then(manyOrEmpty(sep.then(p))))
    }

    fun sepBy1(p: Parsec, sep: Parsec): Parsec {
        return p.then(manyOrEmpty(sep.then(p)))
    }

    fun commaSep(p: Parsec): Parsec {
        return sepBy(p, token(PSTokens.COMMA))
    }

    fun ref(): ParsecRef {
        return ParsecRef()
    }

    fun until(p: Parsec, token: IElementType): Parsec {
        return object : Parsec() {
            override fun parse(context: ParserContext): ParserInfo {
                val startPosition = context.position
                val inAttempt = context.isInAttempt
                context.addUntilToken(token)
                context.isInAttempt = false
                val info = p.parse(context)
                context.isInAttempt = inAttempt
                if (info.success) {
                    return info
                }
                val start = context.start()
                while (!context.eof()) {
                    if (context.isUntilToken(context.peek())) {
                        break
                    }
                    context.advance()
                }
                context.removeUntilToken(token)
                if (!context.isInOptional() || startPosition != context.position) {
                    start.error(info.toString())
                } else {
                    start.drop()
                }
                return ParserInfo(info.position, info, !inAttempt)
            }

            public override fun calcName(): String {
                return p.name!!
            }

            override fun calcExpectedName(): HashSet<String?> {
                return p.expectedName!!
            }

            override fun canStartWith(type: IElementType): Boolean {
                return p.canStartWith(type)
            }

            public override fun calcCanBeEmpty(): Boolean {
                return p.canBeEmpty()
            }
        }
    }

    fun untilSame(p: Parsec): Parsec {
        return object : Parsec() {
            override fun parse(context: ParserContext): ParserInfo {
                val position = context.position
                val info = p.parse(context)
                if (info.success || position == context.position) {
                    return info
                }
                context.whiteSpace()
                if (context.column <= context.lastIndentationLevel) {
                    return info
                }
                val start = context.start()
                while (!context.eof()) {
                    if (context.column == context.indentationLevel.peek()) {
                        break
                    }
                    context.advance()
                }
                start.error(info.toString())
                return ParserInfo(context.position, info, true)
            }

            public override fun calcName(): String {
                return p.name!!
            }

            override fun calcExpectedName(): HashSet<String?> {
                return p.expectedName!!
            }

            override fun canStartWith(type: IElementType): Boolean {
                return p.canStartWith(type)
            }

            public override fun calcCanBeEmpty(): Boolean {
                return p.canBeEmpty()
            }
        }
    }

    fun guard(
        p: Parsec,
        predicate: (String?) -> Boolean,
        errorMessage: String
    ): Parsec {
        return object : Parsec() {
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

            public override fun calcName(): String {
                return p.name!!
            }

            override fun calcExpectedName(): HashSet<String?> {
                return p.expectedName!!
            }

            override fun canStartWith(type: IElementType): Boolean {
                return p.canStartWith(type)
            }

            public override fun calcCanBeEmpty(): Boolean {
                return p.canBeEmpty()
            }
        }
    }
}