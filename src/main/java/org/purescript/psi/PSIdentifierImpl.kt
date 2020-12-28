package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import org.purescript.file.PSFile
import java.util.*

// PsiNamedElement is only here so that the editor can find the the
// Identifier when it is in a parameter
class PSIdentifierImpl(node: ASTNode) : PSPsiElement(node), ContainsIdentifier,
    PsiNamedElement {
    override fun getName(): String {
        return text
    }

    override fun setName(name: String): PsiElement? {
        return null
    }


    override fun getReference(): PsiReference? {
        if (this.node.elementType == PSElements.TypeConstructor) {
            return null
        }
        return object : PsiReferenceBase<PSIdentifierImpl?>(
            this,
            TextRange.allOf(this.name)
        ) {
            override fun getVariants(): Array<PsiNamedElement> {
                val containingFile = containingFile as PSFile
                return (containingFile
                    .topLevelValueDeclarations
                    .values
                    + containingValueDeclaration
                    .map {it.declaredIdentifiersInParameterList}
                    .map {it.values.asSequence()}
                    .orElse(emptySequence<PSIdentifierImpl>())
                    )
                    .map { it as PsiNamedElement }
                    .toTypedArray()
            }

            override fun resolve(): PsiElement? {
                return parameterReference
                    .orElseGet { null }
            }

            private val parameterReference: Optional<PsiElement?>
                private get() = containingValueDeclaration
                    .flatMap { Optional.ofNullable(it.declaredIdentifiersInParameterList[myElement!!.name]) }
                    .map { psi: PSIdentifierImpl? -> psi }
            private val containingValueDeclaration: Optional<PSValueDeclaration>
                private get() {
                    val firstParent = PsiTreeUtil.getParentOfType(
                        myElement,
                        PSValueDeclaration::class.java
                    )
                    return Optional.ofNullable(firstParent)
                }

        }
    }

    override val identifiers
        get() = mapOf<String?, PSIdentifierImpl>(Pair(this.name, this))
}