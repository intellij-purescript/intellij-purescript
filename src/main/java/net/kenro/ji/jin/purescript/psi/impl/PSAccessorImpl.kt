package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSAccessor

class PSAccessorImpl(node: ASTNode) : PSPsiElement(node), PSAccessor