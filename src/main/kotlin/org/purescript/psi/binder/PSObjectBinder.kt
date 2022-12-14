package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSObjectBinder(node: ASTNode) : PSBinderAtom(node)