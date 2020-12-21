package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSGuard

class PSGuardImpl(node: ASTNode) : PSPsiElement(node), PSGuard