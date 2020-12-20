package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSStringLiteral

class PSStringLiteralImpl(node: ASTNode) : PSPsiElement(node), PSStringLiteral