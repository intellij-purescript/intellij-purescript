package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSValueRef

class PSValueRefImpl(node: ASTNode) : PSPsiElement(node), PSValueRef