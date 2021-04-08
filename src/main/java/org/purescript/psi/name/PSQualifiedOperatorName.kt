package org.purescript.psi.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSQualifiedOperatorName(node: ASTNode) : PSPsiElement(node) {

    val moduleName: PSModuleName?
        get() = findChildByClass(PSModuleName::class.java)

    val operatorName: PSOperatorName
        get() = findNotNullChildByClass(PSOperatorName::class.java)
}
