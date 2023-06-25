package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class ExpressionWildcard(node: ASTNode) : PSPsiElement(node), ExpressionAtom