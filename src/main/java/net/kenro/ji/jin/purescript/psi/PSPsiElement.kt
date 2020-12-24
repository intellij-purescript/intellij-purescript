package net.kenro.ji.jin.purescript.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PSPsiElement(node: ASTNode) : ASTWrapperPsiElement(node)