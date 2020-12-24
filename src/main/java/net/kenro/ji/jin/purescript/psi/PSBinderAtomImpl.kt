package net.kenro.ji.jin.purescript.psi

import com.intellij.lang.ASTNode

class PSBinderAtomImpl(node: ASTNode?) : PSPsiElement(node!!), DeclaresIdentifiers