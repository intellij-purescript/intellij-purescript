package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import net.kenro.ji.jin.purescript.psi.PSModule

class PSModuleImpl(node: ASTNode) : PSPsiElement(node), PSModule {
    val topLevelValueDeclarations: Map<String, PSValueDeclarationImpl>
        get() = PsiTreeUtil.findChildrenOfType(this, PSValueDeclarationImpl::class.java)
            .asSequence()
            .map {Pair(it.name, it)}
            .toMap()
}