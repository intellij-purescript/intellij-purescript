package org.purescript.module.declaration.type

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.type.type.TypeVar
import org.purescript.module.declaration.type.type.TypeVarName
import org.purescript.psi.PSPsiElement

class TypeParameters(node: ASTNode) : PSPsiElement(node) {
    val typeNames get() = childrenOfType<TypeVarName>().asSequence()
    val typeVars get() = findChildrenByClass(TypeVar::class.java)
}