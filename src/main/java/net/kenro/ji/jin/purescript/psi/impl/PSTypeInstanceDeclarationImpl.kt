package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSTypeInstanceDeclaration

class PSTypeInstanceDeclarationImpl(node: ASTNode) : PSPsiElement(node),
    PSTypeInstanceDeclaration