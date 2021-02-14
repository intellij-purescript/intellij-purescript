package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings

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
        return importDeclarations
            .asSequence()
            .find { it.name ?: "" == name }
    }

    val importDeclarations: Array<PSImportDeclarationImpl> get() =
        findChildrenByClass(PSImportDeclarationImpl::class.java)

    val valueDeclarations get() = PsiTreeUtil
        .findChildrenOfType(this, PSValueDeclaration::class.java)
        .asSequence()
        .filterNotNull()

    val valueDeclarationsByName: Map<String, List<PSValueDeclaration>> get() =
        valueDeclarations.groupBy { it.name }

    val exportedValueDeclarationsByName: Map<String, List<PSValueDeclaration>> get() =
        valueDeclarationsByName.filterKeys { it in exportedNames }

    val exportedValueDeclarations get() = exportedValueDeclarationsByName.values.flatten()

    val exportedNames: List<String> get() =
        findChildrenByClass(PSPositionedDeclarationRefImpl::class.java)
            .asSequence()
            .map { it.text.trim() }
            .toList()

    val docComments: List<PsiComment>
        get() = parent.siblings(forward = false, withSelf = false)
            .filter { it.elementType == PSTokens.DOC_COMMENT }
            .filterIsInstance(PsiComment::class.java)
            .toList()
            .reversed()

}
