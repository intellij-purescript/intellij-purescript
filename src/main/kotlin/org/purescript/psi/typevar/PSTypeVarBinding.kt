package org.purescript.psi.typevar

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement

sealed class PSTypeVarBinding(node: ASTNode) : PSPsiElement(node)

class PSTypeVarName(node: ASTNode) : PSTypeVarBinding(node)

class PSTypeVarKinded(node: ASTNode) : PSTypeVarBinding(node)
