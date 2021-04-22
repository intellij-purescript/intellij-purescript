package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.SyntaxTraverser
import org.purescript.features.DocCommentOwner
import org.purescript.psi.expression.PSExpressionWhere
import org.purescript.psi.name.PSIdentifier
import javax.swing.Icon

class PSValueDeclaration(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner,
    DocCommentOwner
{

    override fun getName(): String {
        return findChildByClass(PSIdentifier::class.java)!!
            .name
    }
    override fun setName(name: String): PsiElement? {
        this
            .module
            ?.valueDeclarations
            ?.filter { it != this && it.name == this.name }
            ?.forEach { it.rawSetName(name) }
        return rawSetName(name)
    }

    private fun rawSetName(name: String): PSValueDeclaration? {
        val properName = PSPsiFactory(project).createIdentifier(name)
            ?: return null
        nameIdentifier.replace(properName)
        return this
    }

    override fun getTextOffset(): Int = nameIdentifier.textOffset

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

    override fun getNameIdentifier(): PSIdentifier {
        return findNotNullChildByClass(PSIdentifier::class.java)
    }

    override val docComments:List<PsiComment>
        get() = this.getDocComments()

    val varBindersInParameters: Map<String, PSVarBinderImpl>
        get() = SyntaxTraverser.psiTraverser(this)
            .filterIsInstance(PSVarBinderImpl::class.java)
            .asSequence()
            .filterNotNull()
            .map { Pair(it.name, it) }
            .toMap()

    val where: PSExpressionWhere? get() = findChildByClass(PSExpressionWhere::class.java)
}
