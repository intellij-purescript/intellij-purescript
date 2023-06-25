package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.HasTypeId
import org.purescript.inference.InferType
import org.purescript.inference.Unifiable
import org.purescript.module.declaration.Signature
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.module.declaration.value.expression.Qualified
import org.purescript.name.PSQualifiedProperName
import org.purescript.psi.*

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
    override fun getReference() = TypeConstructorReference(this)
    override fun unify() {
        unify(when (name) {
            "Int" -> InferType.Int
            "Number" -> InferType.Number
            "String" -> InferType.String
            "Boolean" -> InferType.Boolean
            "Char" -> InferType.Char
            "Function" -> InferType.Function
            "Record" -> InferType.Record
            "Union" -> InferType.Union
            else -> {
                val ref = reference.resolve()
                when {
                    ref is HasTypeId && ref is Unifiable -> ref.also { it.unify() }.substitutedType 
                    else -> InferType.Constructor(name)
                }
            }
        })
    }
}
