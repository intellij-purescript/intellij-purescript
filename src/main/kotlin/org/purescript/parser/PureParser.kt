package org.purescript.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class PureParser : PsiParser {

    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val mark = builder.mark()
        val info = parser.parse(builder)
        if (!builder.eof()) {
            var nextType: IElementType? = null
            var errorMarker: PsiBuilder.Marker? = null
            if (info == Info.Failure) {
                errorMarker = builder.mark()
                nextType = builder.tokenType
            }
            while (!builder.eof()) {
                builder.advanceLexer()
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
        private val parser = PureParsecParser().parseModule
    }
}