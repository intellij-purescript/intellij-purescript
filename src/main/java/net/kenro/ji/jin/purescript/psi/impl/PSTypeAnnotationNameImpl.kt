package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSTypeAnnotationName

class PSTypeAnnotationNameImpl(node: ASTNode) : PSPsiElement(node),
    PSTypeAnnotationName