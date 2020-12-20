package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSLocalIdentifier

class PSLocalIdentifierImpl(node: ASTNode) : PSPsiElement(node),
    PSLocalIdentifier