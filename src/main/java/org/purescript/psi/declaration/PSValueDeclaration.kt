package org.purescript.psi.declaration

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.*
import org.purescript.features.DocCommentOwner
import org.purescript.psi.PSBinderImpl
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSVarBinderImpl
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
        val identifier = PSPsiFactory(project).createIdentifier(name)
            ?: return null
        nameIdentifier.replace(identifier)
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

    override fun getReference(): PsiReference? {
        val valueDeclarationSelfReference = ValueDeclarationSelfReference(this)
        if (valueDeclarationSelfReference.resolve() == this) {
            return null
        } else  {
            return valueDeclarationSelfReference
        }
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
