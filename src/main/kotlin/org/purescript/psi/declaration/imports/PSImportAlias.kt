package org.purescript.psi.declaration.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.name.PSModuleName

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

    private val moduleName: PSModuleName
        get() =
            findNotNullChildByClass(PSModuleName::class.java)

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PsiElement = moduleName

    override fun getTextOffset(): Int = moduleName.textOffset

    override fun getName(): String = moduleName.name
}
