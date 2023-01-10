package org.purescript.psi.name

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement

/**
 * A qualified identifier, i.e. an optional qualifier followed by
 * an identifier, e.g.
 * ```
 * Some.Qualifier.someIdentifier
 * ```
 * or just
 * ```
 * someIdentifier
 * ```
 */
class PSQualifiedIdentifier(node: ASTNode) : PSPsiElement(node) {

    val moduleName: PSModuleName?
        get() = findChildByClass(PSModuleName::class.java)

    val identifier: PSIdentifier
        get() = findNotNullChildByClass(PSIdentifier::class.java)

    override fun getName(): String =
        identifier.name
}
