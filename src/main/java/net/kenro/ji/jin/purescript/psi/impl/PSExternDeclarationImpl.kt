package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSExternDeclaration

class PSExternDeclarationImpl(node: ASTNode) : PSPsiElement(node),
    PSExternDeclaration