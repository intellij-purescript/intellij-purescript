package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSVar

class PSVarImpl(node: ASTNode) : PSPsiElement(node), PSVar