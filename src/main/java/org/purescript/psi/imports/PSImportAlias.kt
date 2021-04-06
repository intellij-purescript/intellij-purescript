package org.purescript.psi.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.name.PSProperName
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
class PSImportAlias(node: ASTNode) : PSPsiElement(node), PsiNameIdentifierOwner {

    private val properName: PSProperName
        get() =
            findNotNullChildByClass(PSProperName::class.java)

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PsiElement = properName

    override fun getTextOffset(): Int = properName.textOffset

    override fun getName(): String = properName.name
}
