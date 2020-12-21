package net.kenro.ji.jin.purescript.parser

import com.intellij.psi.tree.IElementType
import java.util.*

class SymbolicParsec(private val ref: Parsec, private val node: IElementType) :
    Parsec() {
    override fun parse(context: ParserContext): ParserInfo {
        val startPosition = context.position
        val pack = context.start()
        var info = ref.parse(context)
        if (info.success) {
            pack.done(node)
        } else {
            pack.drop()
        }
        if (startPosition == info.position) {
            info = ParserInfo(info.position, this, info.success)
        }
        return info
    }

    public override fun calcName(): String {
        return node.toString()
    }

    override fun calcExpectedName(): HashSet<String?> {
        val result = HashSet<String?>()
        result.add(node.toString())
        return result
    }

    override fun canStartWith(type: IElementType): Boolean {
        return ref.canStartWith(type)
    }

    public override fun calcCanBeEmpty(): Boolean {
        return ref.canBeEmpty()
    }
}