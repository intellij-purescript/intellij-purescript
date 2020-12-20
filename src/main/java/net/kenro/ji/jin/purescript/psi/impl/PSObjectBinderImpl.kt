package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSObjectBinder

class PSObjectBinderImpl(node: ASTNode) : PSPsiElement(node), PSObjectBinder