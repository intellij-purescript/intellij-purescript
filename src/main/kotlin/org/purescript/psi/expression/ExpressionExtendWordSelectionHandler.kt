package org.purescript.psi.expression

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.purescript.psi.declaration.fixity.PSFixity.Associativity.Infixl
import org.purescript.psi.declaration.fixity.PSFixity.Associativity.Infixr

class ExpressionExtendWordSelectionHandler : ExtendWordSelectionHandler {
    override fun canSelect(e: PsiElement) = e is PSValue

    override fun select(
        e: PsiElement,
        editorText: CharSequence,
        cursorOffset: Int,
        editor: Editor
    ): MutableList<TextRange> {
        var tree: Tree? = null
        for (child in e.children) {
            if (tree == null) tree = Tree.Atom(child)
            else tree = tree.insertRight(child)
        }
        return tree?.ranges()?.toMutableList() ?: mutableListOf()
    }

    sealed interface Tree {
        fun insertRight(other: PsiElement): Tree
        fun ranges(): Sequence<TextRange>
        val start: Int
        val end: Int

        data class Atom(val e: PsiElement) : Tree {
            override fun ranges(): Sequence<TextRange> = emptySequence()
            override val start: Int get() = e.startOffset
            override val end: Int get() = e.endOffset
            override fun insertRight(other: PsiElement) = when (other) {
                is PSExpressionOperator -> Tmp(this, other)
                else -> Call(this, Atom(other))
            }
        }

        data class Call(val c: Tree, val a: Tree) : Tree {
            override fun insertRight(other: PsiElement) = when (other) {
                is PSExpressionOperator -> Tmp(this, other)
                else -> Call(this, Atom(other))
            }

            override fun ranges(): Sequence<TextRange> =
                c.ranges() + a.ranges() + sequenceOf(TextRange(start, end))

            override val start: Int get() = c.start
            override val end: Int get() = a.end
        }

        data class Tmp(val l: Tree, val o: PSExpressionOperator) : Tree {
            override fun insertRight(other: PsiElement) = Operator(l, o, Atom(other))
            override fun ranges(): Sequence<TextRange> = emptySequence()
            override val start: Int get() = l.start
            override val end: Int get() = o.endOffset
        }

        data class Operator(val l: Tree, val o: PSExpressionOperator, val r: Tree) : Tree {
            override fun insertRight(other: PsiElement) = when (other) {
                is PSExpressionOperator -> when {
                    o.precedence ?: 0 > other.precedence ?: 0 -> Tmp(this, other)
                    o.precedence ?: 0 < other.precedence ?: 0 -> copy(r = r.insertRight(other))
                    else -> when (o.associativity) {
                        Infixl -> Tmp(this, other)
                        Infixr -> copy(r = r.insertRight(other))
                        else -> Tmp(this, other)
                    }
                }
                else -> copy(r = r.insertRight(other))
            }
            override fun ranges() = l.ranges() + r.ranges() + sequenceOf(TextRange(start, end))
            override val start: Int get() = l.start
            override val end: Int get() = r.end
        }
    }
}