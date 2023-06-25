package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class TypeWildcard(node: ASTNode) : PSPsiElement(node), PSType 