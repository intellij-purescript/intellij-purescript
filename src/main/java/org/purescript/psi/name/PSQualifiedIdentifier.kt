package org.purescript.psi.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSQualifiedIdentifier(node: ASTNode) : PSPsiElement(node) {

    val moduleName: PSModuleName?
        get() = findChildByClass(PSModuleName::class.java)

    val identifier: PSIdentifier
        get() = findNotNullChildByClass(PSIdentifier::class.java)
}
