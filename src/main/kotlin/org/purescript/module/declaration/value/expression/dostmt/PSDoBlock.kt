package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class PSDoBlock(node: ASTNode) : PSPsiElement(node), Expression {
    val letDeclarations: Array<PSDoNotationLet>
        get() =
        findChildrenByClass(PSDoNotationLet::class.java)

    override val expressions: Sequence<Expression>
        get() = statements.flatMap { it.expressions }.asSequence()

    override fun checkType(): TypeCheckerType? {
        return null
    }

    val statements = childrenOfType<DoStatement>()

    val valueDeclarationGroups get () =
            letDeclarations
                .asSequence()
                .flatMap { it.valueDeclarationGroups.asSequence() }

    override fun infer(scope: Scope): Type {
        TODO("Implement infer for Do block")
    }
}