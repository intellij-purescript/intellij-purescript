package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSValue

class PSValueImpl(node: ASTNode) : PSPsiElement(node), PSValue