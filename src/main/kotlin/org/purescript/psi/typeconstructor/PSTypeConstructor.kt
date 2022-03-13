package org.purescript.psi.typeconstructor

import com.intellij.lang.ASTNode
import org.purescript.psi.*
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.name.PSModuleName
import org.purescript.psi.name.PSProperName

/**
 * A type constructor is a PSI element that references one of the following PSI elements:
 *  - [PSDataDeclaration]
 *  - [PSNewTypeDeclaration]
 *  - [PSSignature]
 *
 * It can appear in many different places, for example in one of the following PSI elements:
 *  - [PSSignature]
 *  - [PSDataConstructor]
 *  - [PSTypeAtom]
 */
class PSTypeConstructor(node: ASTNode) : PSPsiElement(node) {

    val moduleName: PSModuleName?
        get() = identifier.moduleName 

    /**
     * @return the [PSProperName] identifying this type constructor
     */
    private val identifier: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java).identifier

    override fun getName(): String =
        identifier.name

    override fun getReference(): TypeConstructorReference =
        TypeConstructorReference(this)
}
