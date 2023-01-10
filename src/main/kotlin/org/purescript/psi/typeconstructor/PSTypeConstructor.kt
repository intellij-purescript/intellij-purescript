package org.purescript.psi.typeconstructor

import com.intellij.lang.ASTNode
import org.purescript.psi.*
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.declaration.PSSignature
import org.purescript.psi.name.PSModuleName
import org.purescript.psi.name.PSQualifiedProperName
import org.purescript.psi.newtype.PSNewTypeDeclaration

/**
 * A type constructor is a PSI element that references one of the following PSI elements:
 *  - [PSDataDeclaration]
 *  - [PSNewTypeDeclaration]
 *  - [PSSignature]
 *
 * It can appear in many places, for example in one of the following PSI elements:
 *  - [PSSignature]
 *  - [PSDataConstructor]
 *  - [PSTypeAtom]
 */
class PSTypeConstructor(node: ASTNode) : PSPsiElement(node) {
    /**
     * @return the [PSQualifiedProperName] identifying this type constructor
     */
    private val identifier: PSQualifiedProperName
        get() = findNotNullChildByClass(PSQualifiedProperName::class.java)

    val moduleName get() = identifier.moduleName
    override fun getName(): String = identifier.name
    override fun getReference() = TypeConstructorReference(this)
}
