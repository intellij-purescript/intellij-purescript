package org.purescript.psi.exports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.purescript.parser.DDOT
import org.purescript.psi.PSPsiElement

/**
 * The exported member list in an [PSExportedData], e.g.
 *
 * ```
 * (Nothing, Just)
 * ```
 * in
 * ```
 * module Data.Maybe (Maybe(Nothing, Just)) where
 * ```
 */
class PSExportedDataMemberList(node: ASTNode) : PSPsiElement(node) {
    val doubleDot: PsiElement? get() = findChildByType(DDOT)
    val dataMembers: Array<PSExportedDataMember> get() = findChildrenByClass(PSExportedDataMember::class.java)
}
