package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement

class RecordLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom