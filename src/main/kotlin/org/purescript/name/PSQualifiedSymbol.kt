package org.purescript.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

/**
 * A qualified symbol, i.e. an optional qualifier followed by
 * an identifier, e.g.
 * ```
 * Some.Qualifier.(+)
 * ```
 * or just
 * ```
 * (+)
 * ```
 */
class PSQualifiedSymbol(node: ASTNode) : PSPsiElement(node) {

    val moduleName: PSModuleName?
        get() = findChildByClass(PSModuleName::class.java)

    val symbol: PSSymbol
        get() = findNotNullChildByClass(PSSymbol::class.java)

    override fun getName(): String =
        symbol.name
}
