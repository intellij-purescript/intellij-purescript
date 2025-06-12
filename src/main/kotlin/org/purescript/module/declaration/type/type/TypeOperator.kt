package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.file.PSFile
import org.purescript.inference.Inferable
import org.purescript.inference.inferType
import org.purescript.name.PSQualifiedOperatorName

import org.purescript.psi.PSPsiElement

class TypeOperator(node: ASTNode) : PSPsiElement(node), PSType, Inferable {

    override fun unify() {
        reference.inferType((module.containingFile as PSFile).typeSpace.replaceMap())?.let { unify(it) }
    }

    val associativity get() = reference.resolve()?.associativity
    val precedence get() = reference.resolve()?.precedence

    private val qualifiedOperator: PSQualifiedOperatorName
        get() = findNotNullChildByClass(PSQualifiedOperatorName::class.java)
    override fun getName(): String = qualifiedOperator.name
    override fun getReference() =
        TypeOperatorReference(
            this,
            qualifiedOperator.moduleName,
            qualifiedOperator.operator
        )
}