package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.childrenOfType
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.purescript.inference.Inferable
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.fixity.PSFixity
import org.purescript.module.declaration.value.expression.identifier.PSExpressionOperator
import org.purescript.psi.PSPsiElement

class OperatorExpression(node: ASTNode) : PSPsiElement(node), Expression {
    val tree get() = Tree.fromElement(this)
    override fun infer(scope: Scope): Type {
        return tree!!.infer(scope)
    }

    sealed interface Tree : Inferable {
        companion object {
            fun fromElement(e: Expression): Tree? {
                var tree: Tree? = null
                for (child in e.childrenOfType<Expression>()) {
                    tree = tree?.insertRight(child) ?: Atom(child)
                }
                return tree
            }
        }

        fun insertRight(other: Expression): Tree
        fun ranges(): Sequence<TextRange>
        val start: Int
        val end: Int

        data class Atom(val e: Expression) : Tree {
            override fun ranges(): Sequence<TextRange> = emptySequence()
            override val start: Int get() = e.startOffset
            override val end: Int get() = e.endOffset
            override fun infer(scope: Scope): Type = e.infer(scope)
            override fun insertRight(other: Expression) = when (other) {
                is PSExpressionOperator -> Tmp(this, other)
                else -> Call(this, Atom(other))
            }
        }

        data class Call(val c: Tree, val a: Tree) : Tree {
            override fun insertRight(other: Expression) = when (other) {
                is PSExpressionOperator -> Tmp(this, other)
                else -> Call(this, Atom(other))
            }

            override fun ranges(): Sequence<TextRange> =
                c.ranges() + a.ranges() + sequenceOf(TextRange(start, end))

            override val start: Int get() = c.start
            override val end: Int get() = a.end
            override fun infer(scope: Scope): Type =
                scope.inferApp(c.infer(scope), a.infer(scope))
        }

        data class Tmp(val l: Tree, val o: PSExpressionOperator) : Tree {
            override fun insertRight(other: Expression) = Operator(l, o, Atom(other))
            override fun ranges(): Sequence<TextRange> = emptySequence()
            override val start: Int get() = l.start
            override val end: Int get() = o.endOffset
            override fun infer(scope: Scope): Type {
                TODO("Tmp should not exist")
            }
        }

        data class Operator(val l: Tree, val o: PSExpressionOperator, val r: Tree) : Tree {
            override fun ranges() = l.ranges() + r.ranges() + sequenceOf(TextRange(start, end))
            override val start: Int get() = l.start
            override val end: Int get() = r.end
            override fun infer(scope: Scope): Type = scope.inferApp(
                scope.inferApp(o.infer(scope), l.infer(scope)),
                r.infer(scope)
            )

            override fun insertRight(other: Expression) = when (other) {
                is PSExpressionOperator -> when {
                    (o.precedence ?: 0) > (other.precedence ?: 0) -> Tmp(this, other)
                    (o.precedence ?: 0) < (other.precedence ?: 0) -> copy(r = r.insertRight(other))
                    else -> when (o.associativity) {
                        PSFixity.Associativity.Infixl -> Tmp(this, other)
                        PSFixity.Associativity.Infixr -> copy(r = r.insertRight(other))
                        else -> Tmp(this, other)
                    }
                }

                else -> copy(r = r.insertRight(other))
            }
        }
    }
}