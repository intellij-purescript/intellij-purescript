package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.collectDescendantsOfType
import javax.swing.Icon

class PSValueDeclaration(node: ASTNode) : PSPsiElement(node),
    PsiNameIdentifierOwner {
    override fun getName(): String {
        return findChildByClass(PSIdentifierImpl::class.java)!!
            .name
    }

    override fun getPresentation(): ItemPresentation {
        val name = this.name
        val fileName = this.containingFile.name
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                return name
            }

            override fun getLocationString(): String {
                return fileName
            }

            override fun getIcon(unused: Boolean): Icon? {
                return null
            }
        }
    }

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PsiElement? {
        return findChildByClass(PSIdentifierImpl::class.java)
    }

    val varBindersInParameters: Map<String, PSVarBinderImpl>
        get() {
            return collectDescendantsOfType<PSVarBinderImpl>()
                .asSequence()
                .filterNotNull()
                .map { Pair(it.name, it) }
                .toMap()
        }
}