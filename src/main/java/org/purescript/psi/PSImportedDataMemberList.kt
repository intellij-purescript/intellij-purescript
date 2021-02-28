package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.purescript.parser.PSTokens

class PSImportedDataMemberList(node: ASTNode) : PSPsiElement(node) {
    val doubleDot: PsiElement? get() = findChildByType(PSTokens.DDOT)
    val dataMembers: Array<PSImportedDataMember> get() = findChildrenByClass(PSImportedDataMember::class.java)
}
