package org.purescript.module.declaration.value.binder

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.purescript.file.PSFile
import org.purescript.inference.HasTypeId
import org.purescript.inference.InferType
import org.purescript.inference.Unifiable
import org.purescript.module.declaration.fixity.PSFixity

class BinderOperatorExpression(node: ASTNode) : Binder(node) {
    val tree get() = CachedValuesManager.getCachedValue(this) {
        CachedValueProvider.Result(Tree.fromElement(this) ?: error("could not parse expression"), *children)
    }
    override fun unify() = unify(tree.inferType())
    sealed interface Tree : HasTypeId, Unifiable {
        fun inferType(): InferType {
            this.unify()
            return this.substitutedType
        }
        companion object {
            fun fromElement(e: Binder): Tree? {
                var tree: Tree? = null
                for (child in e.childrenOfType<Binder>()) {
                    tree = tree?.insertRight(child) ?: Atom(child)
                }
                return tree
            }
        }

        fun insertRight(other: Binder): Tree
        fun ranges(): Sequence<TextRange>
        val start: Int
        val end: Int

        data class Atom(val e: Binder) : Tree {
            override fun ranges(): Sequence<TextRange> = sequenceOf(e.textRange)
            override val start: Int get() = e.startOffset
            override val end: Int get() = e.endOffset
            override fun unify(): Unit = e.unify()
            override val typeId: InferType.Id get() = e.typeId
            override val substitutedType: InferType get() = e.substitutedType
            override fun insertRight(other: Binder) = when {
                other is BinderOperator -> TmpLeftHand(this, other)
                e is BinderOperator -> TmpRightHand(e, Atom(other))
                else -> TODO("syntax broken")
            }
        }

        data class TmpRightHand(val o: BinderOperator, val r: Tree) : Tree {
            override fun insertRight(other: Binder) = Operator(Atom(other), o, r)
            override fun ranges(): Sequence<TextRange> = emptySequence()
            override val start: Int get() = o.startOffset
            override val end: Int get() = r.end
            override val typeId: InferType.Id get() = TODO("Not yet implemented")
            override val substitutedType: InferType get() = TODO("Not yet implemented")
            override fun unify(): Unit = TODO("Not yet implemented")
        }

        data class TmpLeftHand(val l: Tree, val o: BinderOperator) : Tree {
            override fun insertRight(other: Binder) = Operator(l, o, Atom(other))
            override fun ranges(): Sequence<TextRange> = emptySequence()
            override val start: Int get() = l.start
            override val end: Int get() = o.endOffset
            override val typeId: InferType.Id get() = TODO("Not yet implemented")
            override val substitutedType: InferType get() = TODO("Not yet implemented")
            override fun unify():Unit = TODO("Not yet implemented")
        }

        data class Operator(val l: Tree, val o: BinderOperator, val r: Tree) : Tree {
            override fun ranges() = l.ranges() + r.ranges() + sequenceOf(TextRange(start, end))
            override val start: Int get() = l.start
            override val end: Int get() = r.end
            override val typeId: InferType.Id = (o.module.containingFile as PSFile).typeSpace.newId()
            override val substitutedType: InferType get() = (o.module.containingFile as PSFile).typeSpace.substitute(
                typeId
            )
            override fun unify() {
                val leftHand = l.inferType()
                val rightHand = r.inferType()
                val operatorType = o.inferType()
                val ret = (o.module.containingFile as PSFile).typeSpace.newId()
                (o.module.containingFile as PSFile).typeSpace.unify(operatorType, InferType.function(leftHand, ret))
                val app1 = (o.module.containingFile as PSFile).typeSpace.substitute(ret)
                val ret1 = (o.module.containingFile as PSFile).typeSpace.newId()
                (o.module.containingFile as PSFile).typeSpace.unify(app1, InferType.function(rightHand, ret1))
                val app2 = (o.module.containingFile as PSFile).typeSpace.substitute(ret1)
                (o.module.containingFile as PSFile).typeSpace.unify(substitutedType, app2)
            }

            override fun insertRight(other: Binder) = when (other) {
                is BinderOperator -> when {
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