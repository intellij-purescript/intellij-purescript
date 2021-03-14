package org.purescript.psi.exports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.purescript.parser.PSTokens
import org.purescript.psi.PSPsiElement

class PSExportedDataMemberList(node: ASTNode) : PSPsiElement(node) {
    val doubleDot: PsiElement? get() = findChildByType(PSTokens.DDOT)
    val dataMembers: Array<PSExportedDataMember> get() = findChildrenByClass(PSExportedDataMember::class.java)
}
