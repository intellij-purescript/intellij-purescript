package org.purescript.lexer.token

import com.intellij.psi.tree.IElementType

data class SourceToken(
    val rangeX: SourceRange,
    val value: IElementType,
) {
    val start get()= rangeX.start 
    val end get()= rangeX.end
}