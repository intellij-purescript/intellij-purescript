package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.anyDescendantOfType
import com.intellij.psi.util.collectDescendantsOfType
import com.intellij.psi.util.findDescendantOfType

class PSImportDeclarationImpl(node: ASTNode) : PSPsiElement(node) {

    override fun getName() = importName?.name

    val namedImports: List<String> get() =
        findChildrenByClass(PSPositionedDeclarationRefImpl::class.java)
            .asSequence()
            .map { it.text.trim() }
            .toList()

    val isHiding: Boolean get() =
        anyDescendantOfType<LeafPsiElement>({ it !is PSPositionedDeclarationRefImpl })
            { it.text.trim() == "hiding"}


    val importName get() = findChildByClass(PSProperName::class.java)

    override fun getReference(): PsiReference {
        return ModuleReference(this)
    }
}