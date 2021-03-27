package org.purescript.psi.typeconstructor

import com.intellij.lang.ASTNode
import org.purescript.psi.*
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration

/**
 * A type constructor is a PSI element that references one of the following PSI elements:
 *  - [PSDataDeclaration]
 *  - [PSNewTypeDeclarationImpl]
 *  - [PSTypeDeclarationImpl]
 *
 * It can appear in many different places, for example in one of the following PSI elements:
 *  - [PSTypeDeclarationImpl]
 *  - [PSDataConstructor]
 *  - [PSTypeAtomImpl]
 */
class PSTypeConstructor(node: ASTNode) : PSPsiElement(node) {

    /**
     * @return the [PSProperName] identifying this type constructor
     */
    private val identifier: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String =
        identifier.name

    override fun getReference(): TypeConstructorReference =
        TypeConstructorReference(this)
}
