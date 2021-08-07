package org.purescript.lexer.token

import com.intellij.psi.tree.IElementType

data class SourceToken(
    val range: SourceRange,
    val value: IElementType,
)