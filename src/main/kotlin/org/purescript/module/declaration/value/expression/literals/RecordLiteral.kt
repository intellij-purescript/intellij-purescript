package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.expression.ExpressionAtom

class RecordLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom