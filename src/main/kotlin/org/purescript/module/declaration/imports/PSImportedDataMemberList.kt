package org.purescript.module.declaration.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.purescript.parser.DDOT
import org.purescript.psi.PSPsiElement

class PSImportedDataMemberList(node: ASTNode) : PSPsiElement(node) {
    val doubleDot: PsiElement? get() = findChildByType(DDOT)
    val dataMembers: Array<PSImportedDataMember> get() = findChildrenByClass(
        PSImportedDataMember::class.java)
}
