package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Scope
import org.purescript.inference.InferType
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class PSDoBlock(node: ASTNode) : PSPsiElement(node), Expression {
    val letDeclarations: Array<PSDoNotationLet>
        get() =
        findChildrenByClass(PSDoNotationLet::class.java)

    override val expressions: Sequence<Expression>
        get() = statements.flatMap { it.expressions }.asSequence()
    val statements = childrenOfType<DoStatement>()
    val valueDeclarationGroups get () =
            letDeclarations
                .asSequence()
                .flatMap { it.valueDeclarationGroups.asSequence() }

    override fun infer(scope: Scope): InferType {
        TODO("Implement infer for Do block")
    }
}