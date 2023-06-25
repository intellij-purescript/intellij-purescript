package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.childrenOfType
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.purescript.inference.InferType
import org.purescript.inference.Inferable

import org.purescript.module.declaration.fixity.PSFixity
import org.purescript.module.declaration.value.expression.identifier.PSExpressionOperator
import org.purescript.psi.PSPsiElement

class OperatorExpression(node: ASTNode) : PSPsiElement(node), Expression {
    val tree get() = CachedValuesManager.getCachedValue(this) {
        CachedValueProvider.Result(Tree.fromElement(this) ?: error("could not parse expression"), *children)
    }
    override fun unify() = unify(tree.inferType())
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
            override fun ranges(): Sequence<TextRange> = sequenceOf(e.textRange)
            override val start: Int get() = e.startOffset
            override val end: Int get() = e.endOffset
            override fun unify(): Unit = e.unify()
            override val typeId: InferType.Id? get() = e.typeId
            override val substitutedType: InferType get() = e.substitutedType
            override fun insertRight(other: Expression) = when {
                other is PSExpressionOperator -> TmpLeftHand(this, other)
                e is PSExpressionOperator -> TmpRightHand(e, Atom(other))
                else -> TODO("syntax broken")
            }
        }

        data class TmpRightHand(val o: PSExpressionOperator, val r: Tree) : Tree {
            override fun insertRight(other: Expression) = Operator(Atom(other), o, r)
            override fun ranges(): Sequence<TextRange> = emptySequence()
            override val start: Int get() = o.startOffset
            override val end: Int get() = r.end
            override val typeId: InferType.Id get() = TODO("Not yet implemented")
            override val substitutedType: InferType get() = TODO("Not yet implemented")
            override fun unify(): Unit = TODO("Not yet implemented")
        }

        data class TmpLeftHand(val l: Tree, val o: PSExpressionOperator) : Tree {
            override fun insertRight(other: Expression) = Operator(l, o, Atom(other))
            override fun ranges(): Sequence<TextRange> = emptySequence()
            override val start: Int get() = l.start
            override val end: Int get() = o.endOffset
            override val typeId: InferType.Id get() = TODO("Not yet implemented")
            override val substitutedType: InferType get() = TODO("Not yet implemented")
            override fun unify():Unit = TODO("Not yet implemented")
        }

        data class Operator(val l: Tree, val o: PSExpressionOperator, val r: Tree) : Tree {
            override fun ranges() = l.ranges() + r.ranges() + sequenceOf(TextRange(start, end))
            override val start: Int get() = l.start
            override val end: Int get() = r.end
            override val typeId: InferType.Id = o.module.newId()
            override val substitutedType: InferType get() = o.module.substitute(typeId)
            override fun unify() {
                val leftHand = l.inferType()
                val rightHand = r.inferType()
                val operatorType = o.inferType()
                val ret = o.module.newId()
                o.module.unify(operatorType, InferType.function(leftHand, ret))
                val app1 = o.module.substitute(ret)
                val ret1 = o.module.newId()
                o.module.unify(app1, InferType.function(rightHand, ret1))
                val app2 = o.module.substitute(ret1)
                o.module.unify(substitutedType, app2)
            }

            override fun insertRight(other: Expression) = when (other) {
                is PSExpressionOperator -> when {
                    (o.precedence ?: 0) > (other.precedence ?: 0) -> TmpLeftHand(this, other)
                    (o.precedence ?: 0) < (other.precedence ?: 0) -> copy(r = r.insertRight(other))
                    else -> when (o.associativity) {
                        PSFixity.Associativity.Infixl -> TmpLeftHand(this, other)
                        PSFixity.Associativity.Infixr -> copy(r = r.insertRight(other))
                        else -> TmpLeftHand(this, other)
                    }
                }

                else -> copy(r = r.insertRight(other))
            }
        }
    }
}