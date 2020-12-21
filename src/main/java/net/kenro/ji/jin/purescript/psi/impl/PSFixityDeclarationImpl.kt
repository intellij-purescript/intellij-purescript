package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSFixityDeclaration

class PSFixityDeclarationImpl(node: ASTNode) : PSPsiElement(node),
    PSFixityDeclaration