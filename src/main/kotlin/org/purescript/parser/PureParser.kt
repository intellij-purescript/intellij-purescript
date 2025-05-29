package org.purescript.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class PureParser : PsiParser {

    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val mark = builder.mark()
        val psPsiBuilder = PSPsiBuilder(builder)
        var success = module.parse(psPsiBuilder)
        while (!psPsiBuilder.eof()) {
            if (!success) {
                psPsiBuilder.mark().error(
                    if (psPsiBuilder.tokenType != null) "Unexpected ${psPsiBuilder.tokenType}, while parsing"
                    else "Parsing failed"
                )
            }
            psPsiBuilder.advanceLexer()
            success = moduleBody.parse(psPsiBuilder)
        }
        mark.done(root)
        return builder.treeBuilt
    }
}