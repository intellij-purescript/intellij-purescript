package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSFixity

class PSFixityImpl(node: ASTNode) : PSPsiElement(node), PSFixity