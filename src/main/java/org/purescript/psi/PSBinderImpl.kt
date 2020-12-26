package org.purescript.psi

import com.intellij.lang.ASTNode

class PSBinderImpl(node: ASTNode?) : PSPsiElement(node!!), DeclaresIdentifiers