package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.collectDescendantsOfType
import com.intellij.psi.util.elementType
import javax.swing.Icon

class PSValueDeclaration(node: ASTNode) : PSPsiElement(node),
    PsiNameIdentifierOwner {
    override fun getName(): String {
        return findChildByClass(PSIdentifierImpl::class.java)!!
            .name
    }

    override fun getPresentation(): ItemPresentation {
        val name = this.name
        val parameters = findChildrenByClass(PSBinderImpl::class.java)
        val parameterList = parameters
            .asSequence()
            .map { it.text.trim() }
            .joinToString(" ")
        val presentableText = "$name $parameterList"
        val fileName = this.containingFile.name
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                return presentableText
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

    val documentation: String get() =
        docComments
            .map{ it.text.trim()}
            .map {it.removePrefix("-- |")}
            .map {
                if (it.isBlank()) {
                    "<br/><br/>"
                } else {
                    it
                }
            }
            .joinToString(" ") {it.trim()}
    val docComments:List<PsiElement>
        get() = generateSequence(prevSibling) {
            when (it) {
                !is PSValueDeclaration -> it.prevSibling
                else -> null
            }
        }
            .filter { it.elementType == PSTokens.DOC_COMMENT}
            .toList()
            .reversed()

    val varBindersInParameters: Map<String, PSVarBinderImpl>
        get() {
            return collectDescendantsOfType<PSVarBinderImpl>()
                .asSequence()
                .filterNotNull()
                .map { Pair(it.name, it) }
                .toMap()
        }
}