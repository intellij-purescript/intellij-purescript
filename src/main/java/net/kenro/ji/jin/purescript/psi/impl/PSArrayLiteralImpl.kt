package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSArrayLiteral

class PSArrayLiteralImpl(node: ASTNode) : PSPsiElement(node), PSArrayLiteral