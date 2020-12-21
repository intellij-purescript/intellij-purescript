package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSObjectLiteral

class PSObjectLiteralImpl(node: ASTNode) : PSPsiElement(node), PSObjectLiteral