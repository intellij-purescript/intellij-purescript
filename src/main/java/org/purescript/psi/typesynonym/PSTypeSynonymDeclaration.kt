package org.purescript.psi.typesynonym

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.PSProperName
import org.purescript.psi.PSPsiElement

/**
 * A type synonym declaration, e.g.
 * ```
 * type GlobalEvents r = ( onContextMenu :: Event | r )
 * ```
 */
class PSTypeSynonymDeclaration(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner {

    /**
     * @return the [PSProperName] identifying this declaration
     */
    private val identifier: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PsiElement = identifier

    override fun getName(): String = identifier.name

    override fun getTextOffset(): Int = identifier.textOffset

}
