package org.purescript.psi.exports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.util.parentOfType
import org.purescript.psi.name.PSProperName
import org.purescript.psi.PSPsiElement

/**
 * An exported data member in an [ExportedData.Psi], e.g.
 *
 * ```
 * Nothing
 * ```
 * in
 * ```
 * module Data.Maybe (Maybe(Nothing)) where
 * ```
 */
class PSExportedDataMember(node: ASTNode) : PSPsiElement(node) {
    /**
     * @return the identifier of this element
     */
    internal val properName: PSProperName
        get() =
            findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [ExportedData.Psi] element containing this member
     */
    internal val exportedData: ExportedData.Psi?
        get() = parentOfType()

    override fun getName(): String = properName.name

    override fun getReference(): PsiReference =
        ExportedDataMemberReference(this)
}
