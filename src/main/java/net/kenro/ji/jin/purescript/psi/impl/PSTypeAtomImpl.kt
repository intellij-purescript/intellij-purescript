package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSTypeAtom

class PSTypeAtomImpl(node: ASTNode) : PSPsiElement(node), PSTypeAtom