package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSDoNotationValue

class PSDoNotationValueImpl(node: ASTNode) : PSPsiElement(node),
    PSDoNotationValue