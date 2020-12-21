package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSBooleanBinder

class PSBooleanBinderImpl(node: ASTNode) : PSPsiElement(node), PSBooleanBinder