package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.Inferable
import org.purescript.inference.Scope
import org.purescript.inference.InferType
import org.purescript.psi.PSPsiElement

class TypeOperatorExpression(node: ASTNode) : PSPsiElement(node), PSType {
    private val types get() = findChildrenByClass(PSType::class.java)
    override fun infer(scope: Scope): InferType {
        val (first, second) = types
        val firstType = first.infer(scope)
        val secondType = second.infer(scope)
        val operatorType = (reference?.resolve() as Inferable).infer(scope)
        val funcType = scope.inferApp(operatorType, firstType)
        return scope.inferApp(funcType, secondType)
    }
}