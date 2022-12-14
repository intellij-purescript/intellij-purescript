package org.purescript.psi.declaration

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.*
import org.purescript.features.DocCommentOwner
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSValue
import org.purescript.psi.binder.PSBinderAtom
import org.purescript.psi.binder.PSVarBinder
import org.purescript.psi.expression.PSExpressionWhere
import org.purescript.psi.name.PSIdentifier
import javax.swing.Icon

class PSValueDeclaration(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner,
    DocCommentOwner {

    val value get() = findChildByClass(PSValue::class.java)

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

    val signature: PSSignature? get() = doSignature(this.prevSibling)
    private fun doSignature(sibling: PsiElement?): PSSignature? =
        when (sibling) {
            is PsiWhiteSpace, is PsiComment -> doSignature(sibling.prevSibling)
            is PSValueDeclaration ->
                if (sibling.name == name) sibling.signature
                else null
            is PSSignature -> sibling
            else -> null
        }

    override fun getPresentation(): ItemPresentation {
        val name = this.name
        val parameters = findChildrenByClass(PSBinderAtom::class.java)
        val parameterList = parameters
            .asSequence()
            .map { " " + it.text.trim() }
            .joinToString("")
        val type = signature?.text?.substringAfter(name) ?: ""
        val presentableText =
            "$name$parameterList$type".replace(Regex("\\s+"), " ")
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
        } else {
            return valueDeclarationSelfReference
        }
    }

    override val docComments: List<PsiComment>
        get() = this.getDocComments()

    val varBindersInParameters: Map<String, PSVarBinder>
        get() = SyntaxTraverser.psiTraverser(this)
            .filterIsInstance(PSVarBinder::class.java)
            .asSequence()
            .map { Pair(it.name, it) }
            .toMap()

    val where: PSExpressionWhere? get() = findChildByClass(PSExpressionWhere::class.java)
}
