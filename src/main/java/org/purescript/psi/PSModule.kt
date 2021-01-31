package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

class PSModule(node: ASTNode) : PSPsiElement(node), PsiNameIdentifierOwner {
    override fun getName(): String {
        return nameIdentifier.name
    }

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PSProperName {
        return findChildByClass(PSProperName::class.java)!!
    }

    override fun getTextOffset(): Int {
        return this.nameIdentifier.textRangeInParent.startOffset
    }

    fun getImportDeclarationByName(name: String): PSImportDeclarationImpl? {
        return findChildrenByClass(PSImportDeclarationImpl::class.java)
            .asSequence()
            .find { it.name ?: "" == name }
    }


    val topLevelValueDeclarations: Map<String, List<PSValueDeclaration>>
        get() = PsiTreeUtil
            .findChildrenOfType(this, PSValueDeclaration::class.java)
            .asSequence()
            .filterNotNull()
            .groupBy { it.name }
}