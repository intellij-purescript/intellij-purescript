package net.kenro.ji.jin.purescript.psi

import com.intellij.lang.ASTNode

class PSBinderImpl(node: ASTNode?) : PSPsiElement(node!!), DeclaresIdentifiers