package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class PSDoNotationBind(node: ASTNode) : PSPsiElement(node), DoStatement {
    override val binders get() = binder.descendantBinders
    private val binder get() = childrenOfType<Binder>().single()
    val expression get() = childrenOfType<Expression>().single()

    override fun unify() {
        val monad = module.newId()
        parentOfType<PSDoBlock>()?.substitutedType?.let {
            val unknown = module.newId()
            unify(it, monad.app(unknown))
        }
        unify(
            expression.inferType(),
            monad.app(binder.inferType())
        )
    }
}