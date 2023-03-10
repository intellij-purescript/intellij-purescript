package org.purescript.module.declaration.type.typeconstructor

import com.intellij.lang.ASTNode
import org.purescript.psi.*
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.signature.PSSignature
import org.purescript.name.PSQualifiedProperName
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.module.declaration.value.expression.Qualified

/**
 * A type constructor is a PSI element that references one of the following PSI elements:
 *  - [DataDeclaration.Psi]
 *  - [NewtypeDecl]
 *  - [PSSignature]
 *
 * It can appear in many places, for example in one of the following PSI elements:
 *  - [PSSignature]
 *  - [DataConstructor.PSDataConstructor]
 *  - [PSTypeAtom]
 */
class PSTypeConstructor(node: ASTNode) : PSPsiElement(node), Qualified {
    /**
     * @return the [PSQualifiedProperName] identifying this type constructor
     */
    private val identifier: PSQualifiedProperName
        get() = findNotNullChildByClass(PSQualifiedProperName::class.java)

    val moduleName get() = identifier.moduleName
    override fun getName(): String = identifier.name
    override val qualifierName: String? get() = moduleName?.name
    override fun getReference() = TypeConstructorReference(this)
}
