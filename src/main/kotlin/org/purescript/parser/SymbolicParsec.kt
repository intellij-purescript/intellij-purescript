package org.purescript.parser

import com.intellij.psi.tree.IElementType
import org.purescript.parser.Info.Failure

class SymbolicParsec(private val ref: Parsec, private val node: IElementType) :
    Parsec() {
    override fun parse(context: ParserContext): Info {
        val pack = context.start()
        return when (val info = ref.parse(context)) {
            Failure -> {
                pack.drop()
                info
            }
            else -> {
                pack.done(node)
                info
            }
        }
    }

}