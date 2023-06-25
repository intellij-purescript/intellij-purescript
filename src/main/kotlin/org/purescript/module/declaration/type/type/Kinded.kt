package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class Kinded(node: ASTNode) : PSPsiElement(node), PSType 