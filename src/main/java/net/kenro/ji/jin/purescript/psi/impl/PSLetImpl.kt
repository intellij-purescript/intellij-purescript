package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSLet

class PSLetImpl(node: ASTNode) : PSPsiElement(node), PSLet