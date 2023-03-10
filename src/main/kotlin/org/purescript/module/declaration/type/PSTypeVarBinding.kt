package org.purescript.module.declaration.type

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

sealed class PSTypeVarBinding(node: ASTNode) : PSPsiElement(node)

class PSTypeVarName(node: ASTNode) : PSTypeVarBinding(node)

class PSTypeVarKinded(node: ASTNode) : PSTypeVarBinding(node)
