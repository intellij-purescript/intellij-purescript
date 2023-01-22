package org.purescript.lexer.token

import com.intellij.psi.tree.IElementType

data class SourceToken(
    val value: IElementType,
    val start: SourcePos,
    val end: SourcePos,
) 