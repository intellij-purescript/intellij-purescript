package org.purescript.parser

import com.intellij.lang.PsiBuilder

interface Parser {
    fun parse(b: PSPsiBuilder): Boolean
}