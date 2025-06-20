package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import org.purescript.file.PSFile
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class PSDoNotationBind(node: ASTNode) : PSPsiElement(node), DoStatement {
    override val binders get() = binder.descendantBinders
    private val binder get() = childrenOfType<Binder>().single()
    val expression get() = childrenOfType<Expression>().single()

    override fun unify() {
        val monad = (module.containingFile as PSFile).typeSpace.newId()
        parentOfType<PSDoBlock>()?.substitutedType?.let {
            val unknown = (module.containingFile as PSFile).typeSpace.newId()
            unify(it, monad.app(unknown))
        }
        unify(
            expression.inferType(),
            monad.app(binder.inferType())
        )
    }

    override val valueNamesAhead: Sequence<PsiNamedElement>
        get() = binders.filterIsInstance<PsiNamedElement>().asSequence() + valueNames
}