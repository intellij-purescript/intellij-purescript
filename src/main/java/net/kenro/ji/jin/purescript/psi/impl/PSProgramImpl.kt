package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.PSProgram

class PSProgramImpl(node: ASTNode) : PSPsiElement(node), PSProgram {
    val module: PSModuleImpl
        get() = this.firstChild as PSModuleImpl
}