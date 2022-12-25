package org.purescript.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class PureParser : PsiParser {

    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val context = ParserContext(builder)
        val mark = context.start()
        val info = parser.parse(context)
        if (!context.eof()) {
            var nextType: IElementType? = null
            var errorMarker: PsiBuilder.Marker? = null
            if (info is Info.Failure) {
                errorMarker = context.start()
                nextType = builder.tokenType
            }
            while (!context.eof()) {
                context.advance()
            }
            errorMarker?.error(
                if (nextType != null) "Unexpected $nextType. $info"
                else "$info"
            )
        }
        mark.done(root)
        return builder.treeBuilt
    }

    companion object {
        private val parser = PureParsecParser().parseModule.optimize.compile
    }
}