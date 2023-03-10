package org.purescript.module.declaration.value.expression

import com.intellij.openapi.util.TextRange
import junit.framework.TestCase
import org.purescript.module.declaration.fixity.PSFixity.Associativity

sealed interface Tree {
    val ranges: Sequence<TextRange>
    val start: Int
    val end: Int
    fun add(node: Tree): Tree

    data class Span(override val start: Int, override val end: Int) : Tree {
        override val ranges get() = emptySequence<TextRange>()
        override fun add(node: Tree): Tree = Call(this, node)
    }

    data class Call(val function: Tree, val argument: Tree) : Tree {
        override val ranges get() = sequenceOf(TextRange(start, end))
        override val start get() = function.start
        override val end get() = argument.end
        override fun add(node: Tree): Tree = Call(this, node)
    }

    data class Operator(
        val associativity: Associativity,
        val precedence: Int,
        val left: Span,
        val right: Span
    ) : Tree {
        override val ranges get() = sequenceOf(TextRange(start, end))
        override val start: Int get() = left.start
        override val end: Int get() = right.end
        override fun add(node: Tree): Tree {
            TODO("Not yet implemented")
        }

    }

    object Empty : Tree {
        override val ranges get() = emptySequence<TextRange>()
        override val start: Int get() = TODO("Not yet implemented")
        override val end: Int get() = TODO("Not yet implemented")
        override fun add(node: Tree): Tree = node
    }
}

class TestTree : TestCase() {

    fun `test empty has no range`() {
        assertEquals(0, Tree.Empty.ranges.count())
    }

    fun `test span has no range`() {
        assertEquals(0, Tree.Span(0, 0).ranges.count())
    }

    fun `test call has range including both children`() {
        val call = Tree.Call(Tree.Span(1, 2), Tree.Span(2, 3))
        assertEquals(TextRange(1, 3), call.ranges.single())
    }

    fun `test operator has range including both children`() {
        val call = Tree.Operator(Associativity.Infix, 0, Tree.Span(1, 2), Tree.Span(2, 3))
        assertEquals(TextRange(1, 3), call.ranges.single())
    }
    
    fun `test adding anything to empty replaces it `() {
        val span = Tree.Span(1, 2)
        val tree = Tree.Empty.add(span)
        assertEquals(span, tree)
    }
    
    fun `test adding spans together creates call`() {
        val f = Tree.Span(1, 2)
        val a = Tree.Span(2, 3)
        val tree = Tree.Empty.add(f).add(a)
        assertEquals(Tree.Call(f, a), tree)
    }
    fun `test adding spans together to call creates nesting call`() {
        val f = Tree.Span(1, 2)
        val a = Tree.Span(2, 3)
        val b = Tree.Span(3, 4)
        val tree = Tree.Empty.add(f).add(a).add(b)
        assertEquals(Tree.Call(Tree.Call(f, a), b), tree)
    }
}