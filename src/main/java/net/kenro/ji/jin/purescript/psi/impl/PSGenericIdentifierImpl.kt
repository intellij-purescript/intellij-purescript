package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSGenericIdentifier

class PSGenericIdentifierImpl(node: ASTNode) : PSPsiElement(node),
    PSGenericIdentifier