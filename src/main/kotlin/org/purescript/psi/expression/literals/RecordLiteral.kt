package org.purescript.psi.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.expression.ExpressionAtom

class RecordLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom