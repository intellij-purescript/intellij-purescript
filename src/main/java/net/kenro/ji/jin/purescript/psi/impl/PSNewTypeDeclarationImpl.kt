package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSNewTypeDeclaration

class PSNewTypeDeclarationImpl(node: ASTNode) : PSPsiElement(node),
    PSNewTypeDeclaration