package org.purescript.psi.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.purescript.parser.PSTokens
import org.purescript.psi.PSPsiElement

class PSImportedDataMemberList(node: ASTNode) : PSPsiElement(node) {
    val doubleDot: PsiElement? get() = findChildByType(PSTokens.DDOT)
    val dataMembers: Array<PSImportedDataMember> get() = findChildrenByClass(PSImportedDataMember::class.java)
}
