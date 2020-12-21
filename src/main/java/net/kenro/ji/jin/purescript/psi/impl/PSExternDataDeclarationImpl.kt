package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSExternDataDeclaration

class PSExternDataDeclarationImpl(node: ASTNode) : PSPsiElement(node),
    PSExternDataDeclaration