package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSQualifiedModuleName

class PSQualifiedModuleNameImpl(node: ASTNode) : PSPsiElement(node),
    PSQualifiedModuleName