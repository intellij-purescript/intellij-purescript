package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSTypeHole(node: ASTNode) : PSPsiElement(node), ExpressionAtom