package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.util.elementType
import org.purescript.features.DocCommentOwner
import org.purescript.parser.PSTokens
import javax.swing.Icon

class PSValueDeclaration(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner,
    DocCommentOwner
{
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

    override val docComments:List<PsiComment>
        get() = generateSequence(prevSibling) {
            when (it) {
                !is PSValueDeclaration -> it.prevSibling
                else -> null
            }
        }
            .filter { it.elementType == PSTokens.DOC_COMMENT}
            .filterIsInstance(PsiComment::class.java)
            .toList()
            .reversed()

    val varBindersInParameters: Map<String, PSVarBinderImpl>
        get() = SyntaxTraverser.psiTraverser(this)
            .filterIsInstance(PSVarBinderImpl::class.java)
            .asSequence()
            .filterNotNull()
            .map { Pair(it.name, it) }
            .toMap()
}