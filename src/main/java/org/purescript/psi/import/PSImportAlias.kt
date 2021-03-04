package org.purescript.psi.import

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.purescript.psi.PSProperName
import org.purescript.psi.PSPsiElement

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

    private val properName: PSProperName
        get() =
        findNotNullChildByClass(PSProperName::class.java)

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getName(): String = properName.name
}
