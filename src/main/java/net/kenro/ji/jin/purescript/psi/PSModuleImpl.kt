package net.kenro.ji.jin.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil

class PSModuleImpl(node: ASTNode) : PSPsiElement(node){
    val topLevelValueDeclarations: Map<String, PSValueDeclarationImpl>
        get() = PsiTreeUtil.findChildrenOfType(this, PSValueDeclarationImpl::class.java)
            .asSequence()
            .map {Pair(it.name, it)}
            .toMap()
}