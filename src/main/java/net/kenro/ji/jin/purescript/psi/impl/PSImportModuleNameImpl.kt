package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSImportModuleName

class PSImportModuleNameImpl(node: ASTNode) : PSPsiElement(node),
    PSImportModuleName