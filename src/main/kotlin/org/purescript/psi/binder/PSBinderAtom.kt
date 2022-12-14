package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

sealed class PSBinderAtom(node: ASTNode?) : PSPsiElement(node!!)