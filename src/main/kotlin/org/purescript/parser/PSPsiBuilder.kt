package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.impl.PsiBuilderAdapter

class PSPsiBuilder(delegate: PsiBuilder):
    PsiBuilderAdapter(delegate) {
}