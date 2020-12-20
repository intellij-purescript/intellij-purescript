package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSStringBinder

class PSStringBinderImpl(node: ASTNode) : PSPsiElement(node), PSStringBinder