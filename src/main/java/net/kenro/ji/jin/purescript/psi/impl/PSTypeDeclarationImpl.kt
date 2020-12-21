package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSTypeDeclaration

class PSTypeDeclarationImpl(node: ASTNode) : PSPsiElement(node),
    PSTypeDeclaration