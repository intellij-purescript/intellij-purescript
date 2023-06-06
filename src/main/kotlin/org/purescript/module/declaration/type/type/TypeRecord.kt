package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.type.Labeled
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class TypeRecord(node: ASTNode) : PSPsiElement(node), PSType {
    private val labels get() = findChildrenByClass(Labeled::class.java)
    override fun checkType(): TypeCheckerType =TypeCheckerType.TypeApp(
        TypeCheckerType.TypeConstructor("Prim.Record"),
        TypeCheckerType.Row(labels.mapNotNull { it.name to it.type?.checkType() }.toList())
    )
}