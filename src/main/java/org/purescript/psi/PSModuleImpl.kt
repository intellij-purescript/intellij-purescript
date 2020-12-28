package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

class PSModuleImpl(node: ASTNode) : PSPsiElement(node), PsiNameIdentifierOwner {
    override fun getName(): String {
        return nameIdentifier.name
    }

    override fun setName(name: String): PsiElement? {
        return null;
    }

    override fun getNameIdentifier(): PSProperNameImpl {
        return findChildByClass(PSProperNameImpl::class.java)!!
    }


    val topLevelValueDeclarations: Map<String, PSValueDeclarationImpl>
        get() = PsiTreeUtil
            .findChildrenOfType(this, PSValueDeclarationImpl::class.java)
            .asSequence()
            .map { Pair(it.name, it) }
            .toMap()
}