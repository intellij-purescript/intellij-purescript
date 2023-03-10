package org.purescript.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

/**
 * A operator in parenthesis without qualifier, example `(+)`
 */
class PSSymbol(node: ASTNode) : PSPsiElement(node) {
    val operator get() = findNotNullChildByClass(PSOperatorName::class.java)
    override fun getName(): String = operator.name
    fun nameMatches(name: String) = operator.nameMatches(name)
}
