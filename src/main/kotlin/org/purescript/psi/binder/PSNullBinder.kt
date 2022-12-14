package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSNullBinder(node: ASTNode) : PSBinderAtom(node)