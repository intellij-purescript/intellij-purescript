package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSTypeClassDeclaration

class PSTypeClassDeclarationImpl(node: ASTNode) : PSPsiElement(node),
    PSTypeClassDeclaration