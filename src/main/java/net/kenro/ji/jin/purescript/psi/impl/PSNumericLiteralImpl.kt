package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSNumericLiteral

class PSNumericLiteralImpl(node: ASTNode) : PSPsiElement(node), PSNumericLiteral