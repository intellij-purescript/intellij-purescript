package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement

class PSStringLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom