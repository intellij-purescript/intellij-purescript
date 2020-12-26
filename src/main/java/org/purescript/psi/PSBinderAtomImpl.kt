package org.purescript.psi

import com.intellij.lang.ASTNode

class PSBinderAtomImpl(node: ASTNode?) : PSPsiElement(node!!), DeclaresIdentifiers