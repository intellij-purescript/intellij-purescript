package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.Prim

class PSCharLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun checkType() = Prim.char
    override fun checkReferenceType() = Prim.char
    override fun infer(scope: Scope): Type = Type.Char
    override fun checkUsageType() = Prim.char
}