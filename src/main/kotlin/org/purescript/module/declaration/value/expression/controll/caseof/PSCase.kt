package org.purescript.module.declaration.value.expression.controll.caseof

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class PSCase(node: ASTNode) : PSPsiElement(node), Expression