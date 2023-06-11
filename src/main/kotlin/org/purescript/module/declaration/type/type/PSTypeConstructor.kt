package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.Signature
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.module.declaration.value.expression.Qualified
import org.purescript.name.PSQualifiedProperName
import org.purescript.psi.*
import org.purescript.typechecker.TypeCheckable
import org.purescript.typechecker.TypeCheckerType

/**
 * A type constructor is a PSI element that references one of the following PSI elements:
 *  - [DataDeclaration]
 *  - [NewtypeDecl]
 *  - [Signature]
 *
 * It can appear in many places, for example in one of the following PSI elements:
 *  - [Signature]
 *  - [DataConstructor.PSDataConstructor]
 */
class PSTypeConstructor(node: ASTNode) : PSPsiElement(node), Qualified, PSType {
    /**
     * @return the [PSQualifiedProperName] identifying this type constructor
     */
    val identifier: PSQualifiedProperName get() = findNotNullChildByClass(PSQualifiedProperName::class.java)
    val moduleName get() = identifier.moduleName
    override fun getName(): String = identifier.name
    override val qualifierName: String? get() = moduleName?.name
    override fun checkType(): TypeCheckerType? = (reference.resolve() as? TypeCheckable)?.checkType() 
    override fun getReference() = TypeConstructorReference(this)
}
