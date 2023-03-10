package org.purescript.module.declaration.value.binder

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.expression.identifier.ConstructorReference
import org.purescript.module.declaration.value.expression.Qualified
import org.purescript.name.PSQualifiedProperName


/**
 * The node `M.Box` in the code
 * 
 * ```purescript
 * f (M.Box a) = a
 * ```
 */
class ConstructorBinder(node: ASTNode) : Binder(node), Qualified {
    /**
     * @return the [PSQualifiedProperName] identifying this constructor
     */
    internal val qualifiedProperName: PSQualifiedProperName
        get() = findNotNullChildByClass(PSQualifiedProperName::class.java)

    override fun getName(): String = qualifiedProperName.name
    override val qualifierName: String? get() = qualifiedProperName.moduleName?.name
    override fun getReference(): ConstructorReference = ConstructorReference(this, this.qualifiedProperName)
}