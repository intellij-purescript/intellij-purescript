package org.purescript.inference

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class UnionFindTest {
    @Test
    fun `not yet unified unknown is it self`() {
        val space = UnionFind()
        val first: Node = space.unknown()
        assertSame(first, first.lookup())
    }
    @Test
    fun `unified unknowns are the same`() {
        val space = UnionFind()
        val first: Node = space.unknown()
        val second: Node = space.unknown()
        first.unify(second)
        assertSame(first.lookup(), second.lookup())
    }
    
    @Test
    fun `unknown that is unified to constructor is that constructor`() {
        val space = UnionFind()
        val first: Node.Unknown = space.unknown()
        val me = Node.Constructor("Me")
        first.unify(me)
        assertEquals(first.lookup(), me)
    }
    
    @Test
    fun `unknown that is unified to a known unkown is known`() {
        val space = UnionFind()
        val first: Node.Unknown = space.unknown()
        val second: Node.Unknown = space.unknown()
        first.unify(second)
        val me = Node.Constructor("Me")
        first.unify(me)
        assertEquals(me, second.lookup())
    }    
    @Test
    fun `unknown that is unified to a known unkown is known other way around`() {
        val space = UnionFind()
        val first: Node.Unknown = space.unknown()
        val second: Node.Unknown = space.unknown()
        first.unify(second)
        val me = Node.Constructor("Me")
        me.unify(first)
        assertEquals(me, second.lookup())
    }
    
    @Test
    fun `unifying two calls, unifies callers and arguments`() {
        val space = UnionFind()
        val f: Node.Unknown = space.unknown()
        val x: Node.Unknown = space.unknown()
        val g: Node.Unknown = space.unknown()
        val y: Node.Unknown = space.unknown()
        
        f.application(x).unify(g.application(y))
        
        assertEquals(f.lookup(), g.lookup())
        assertEquals(x.lookup(), y.lookup())
    }
    
    @Test
    fun `uncurry applications`() {
        val space = UnionFind()
        val f: Node.Unknown = space.unknown()
        val x: Node.Unknown = space.unknown()
        val y: Node.Unknown = space.unknown()
        // ((f x) y)
        assertEquals(
            f.application(x).application(y),
            f.application(x, y)
        )
    }
    
    @Test
    fun `function is just a application of the PrimFunction Kind`() {
        val space = UnionFind()
        val from: Node.Unknown = space.unknown()
        val to: Node.Unknown = space.unknown()
        assertEquals(
            from.function(to),
            Node.Constructor("Prim.Function").application(from, to)
        )
    }    
    @Test
    fun `forall gets removed when typvariable becomes concrete`() {
        val space = UnionFind()
        val a: Node.Unknown = space.unknown()
        val forall = Node.ForAll(a, a)
        assertEquals(forall.lookup(), forall)
        val int = Node.Constructor("Prim.Int")
        a.unify(int)
        assertEquals(forall.lookup(), int)
    }

    @Test
    fun `single row label unifies`() {
        val space = UnionFind()
        val a = space.unknown()
        val b = space.unknown()
        val labelA = a.labeled("foo")
        val labelB = b.labeled("foo")
        labelA.unify(labelB)
        assertEquals(a.lookup(), b.lookup())
    }
}