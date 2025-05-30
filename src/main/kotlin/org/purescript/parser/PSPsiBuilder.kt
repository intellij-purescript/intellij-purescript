package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.impl.PsiBuilderAdapter
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.lexer.DelegateLexer
import com.intellij.lexer.Lexer
import org.purescript.lexer.PSLexer

class PSPsiBuilder(delegate: PsiBuilder):
    PsiBuilderAdapter(delegate) {
    val indents = ArrayDeque<Int>().apply { addLast(-1) }
    var currentName: String = ""
    val column: Int get() = psLexer.getColumn(currentOffset)
    fun indent() = indents.addLast(column)
    fun dedent() = indents.removeLast()
    val offside: Int get() = indents.last()
}


val Lexer.psLexer: PSLexer
    get() = when(this) {
        is PSLexer -> this
        is DelegateLexer -> delegate.psLexer
        else -> error("Bad lexer type")
    }

val PsiBuilder.rootPsiBuilder: PsiBuilderImpl
    get() = when (this) {
        is PsiBuilderAdapter -> delegate.rootPsiBuilder
        is PsiBuilderImpl -> this
        else -> error("Unknown PsiBuilder implementation")
    } as PsiBuilderImpl

val PsiBuilder.psLexer: PSLexer get() = rootPsiBuilder.lexer.psLexer

