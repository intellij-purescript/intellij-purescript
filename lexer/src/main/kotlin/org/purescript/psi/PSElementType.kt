package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import org.purescript.PSLanguage
import org.jetbrains.annotations.NonNls

open class PSElementType(@NonNls debugName: String) :
    IElementType(debugName, PSLanguage.INSTANCE) {
    class WithPsi(
        @NonNls debugName: String,
        val constructor: (ASTNode) -> PsiElement
    ) :
        PSElementType(debugName)
}