package org.purescript.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class PureParser : PsiParser {

    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val mark = builder.mark()
        var success = module.parse(builder)
        while (!builder.eof()) {
            if (!success) {
                builder.mark().error(
                    if (builder.tokenType != null) "Unexpected ${builder.tokenType}, while parsing"
                    else "Parsing failed"
                )
            }
            builder.advanceLexer()
            success = moduleBody.parse(builder)
        }
        mark.done(root)
        return builder.treeBuilt
    }
}