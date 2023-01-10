package org.purescript.psi.imports

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.name.PSProperName

class PSImportedDataMember(node: ASTNode) : PSPsiElement(node) {
    val properName: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name
}
