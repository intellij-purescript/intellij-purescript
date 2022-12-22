package org.purescript.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class PureParser : PsiParser {

    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val context = ParserContext(builder)
        val mark = context.start()
        val info = pureParsecParser.parseModule.optimize().compile().parse(context)
        var nextType: IElementType? = null
        if (!context.eof()) {
            var errorMarker: PsiBuilder.Marker? = null
            while (!context.eof()) {
                if (errorMarker == null && context.position >= info.position) {
                    errorMarker = context.start()
                    nextType = builder.tokenType
                }
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
        private val pureParsecParser = PureParsecParser()
    }
}