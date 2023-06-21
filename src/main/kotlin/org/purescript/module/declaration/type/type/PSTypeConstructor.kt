package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.Signature
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.module.declaration.type.TypeDecl
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
    override fun infer(scope: Scope): Type = when (name) {
        "Int" -> Type.Int
        "Number" -> Type.Number
        "String" -> Type.String
        "Boolean" -> Type.Boolean
        "Char" -> Type.Char
        "Function" -> Type.Function
        else -> when(val ref = reference.resolve()){
            is TypeDecl -> ref.type?.infer(scope)
            else -> null
        } ?: Type.Constructor(name)
    }
}
