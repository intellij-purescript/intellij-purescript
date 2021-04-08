package org.purescript.psi.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSQualifiedProperName(node: ASTNode) : PSPsiElement(node) {

    val moduleName: PSModuleName?
        get() = findChildByClass(PSModuleName::class.java)

    val properName: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name
}
