package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

/**
 * The alias of an import declaration.
 *
 * Example:
 * `as FB`
 *
 * in
 *
 * ```import Foo.Bar as FB```
 */
class PSImportAlias(node: ASTNode) : PSPsiElement(node), PsiNamedElement {

    private val properName: PSProperName get() =
        findNotNullChildByClass(PSProperName::class.java)

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getName(): String = properName.name
}
