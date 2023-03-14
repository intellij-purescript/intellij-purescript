package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.Similar
import org.purescript.psi.PSPsiElement

class PSAccessor(node: ASTNode) : PSPsiElement(node), Similar