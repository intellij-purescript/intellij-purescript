package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import net.kenro.ji.jin.purescript.file.PSFile
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier
import java.util.*

// PsiNamedElement is only here so that the editor can find the the
// Identifier when it is in a parameter
class PSIdentifierImpl(node: ASTNode) : PSPsiElement(node), ContainsIdentifier,
    PsiNamedElement {
    override fun getName(): String {
        return text.trim { it <= ' ' }
    }

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getReference(): PsiReference? {
        return object : PsiReferenceBase<PSIdentifierImpl?>(
            this,
            TextRange.allOf(this.name)
        ) {
            override fun resolve(): PsiElement? {
                return parameterReference
                    .orElseGet { topLevelValueDeclarationReference }
            }

            private val topLevelValueDeclarationReference: PSValueDeclarationImpl?
                private get() {
                    val containingFile = containingFile as PSFile
                    return containingFile
                        .topLevelValueDeclarations[myElement!!.name]
                }
            private val parameterReference: Optional<PsiElement?>
                private get() = containingValueDeclaration
                    .flatMap { Optional.ofNullable(it.declaredIdentifiersInParameterList[myElement!!.name]) }
                    .map { psi: PSIdentifierImpl? -> psi }
            private val containingValueDeclaration: Optional<PSValueDeclarationImpl>
                private get() {
                    val firstParent = PsiTreeUtil.getParentOfType(
                        myElement,
                        PSValueDeclarationImpl::class.java
                    )
                    return Optional.ofNullable(firstParent)
                }

        }
    }

    override val identifiers
        get() = mapOf<String?, PSIdentifierImpl>(Pair(this.name, this))
}