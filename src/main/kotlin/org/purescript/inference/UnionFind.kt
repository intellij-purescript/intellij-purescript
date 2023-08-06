package org.purescript.inference


class UnionFind {
    private val database = mutableListOf<Node>()
    operator fun get(node: Node): Node = node.lookup()
    operator fun get(id: Int): Node = database[id]

    /**
     * https://github.com/purescript/purescript/blob/v0.15.10/src/Language/PureScript/TypeChecker/Unify.hs#L109
     **/
    fun unify(a: Node, b: Node) = a.unify(b)
    operator fun set(id: Int, value: Node) {
        database[id] = value
    }

    fun unknown(): Node.Unknown {
        val unknown = Node.Unknown(this, database.size)
        database.add(unknown)
        return unknown
    }
}

/**
 * An inferred type
 */
sealed interface Node {
    fun unify(other: Node)
    fun lookup(): Node = this
    fun unifyRight(other: Node) = Unit
    fun application(other: Node, vararg rest: Node): Application =
        rest.fold(Application(this, other)) { f, a -> f.application(a) }

    fun function(other: Node) = Constructor("Prim.Function").application(this, other)

    /**
     * all nodes are unknown at the beginning
     */
    class Unknown(private val space: UnionFind, private val id: Int) : Node {
        override fun lookup(): Node = space[id]
        override fun unify(other: Node) {
            val self = lookup()
            if (self != this) self.unify(other)
            else space[id] = other.lookup()
        }

        override fun unifyRight(other: Node) {
            val self = lookup()
            if (self != this) self.unifyRight(other)
            space[id] = other.lookup()
        }
    }

    /**
     * Reference to a known but external type to this declaration
     **/
    @JvmInline
    value class Ref(val fqn: String) : Node {
        override fun unify(other: Node) {
            other.unifyRight(this)
        }
    }

    /**
     * A constructor local to this declaration
     **/
    @JvmInline
    value class Constructor(val fqn: String) : Node {
        override fun unify(other: Node) {
            other.unifyRight(this)
        }
    }

    /**
     * A constructor local to this declaration
     **/
    data class Application(val function: Node, val argument: Node) : Node {
        override fun unify(other: Node) = when (other) {
            is Application -> {
                function.unify(other.function)
                argument.unify(other.argument)
            }

            else -> {}
        }
    }

    class ForAll(val variable: Node, val scope: Node) : Node {
        override fun unify(other: Node) {
            other.unify(scope)
        }

        override fun lookup(): Node = when (variable.lookup()) {
            is Unknown -> this
            else -> scope.lookup()
        }

    }

    /**
     * the empty road
     */
    data object EmptyRow : Node {
        override fun unify(other: Node) {
            other.unifyRight(this)
        }
        fun add(label: String, type: Node): Row {
            return Row(label, type, this)
        }
    }

    /**
     * A constructor local to this declaration
     **/
    class Row(
        private val label: String,
        private val type: Node,
        private val next: Node,
    ) : Node {
        override fun lookup(): Node {
            val type = this.type.lookup()
            return if (type == this.type) this
            else Row(label, type, next.lookup())
        }

        override fun unify(other: Node) = when (other) {
            is Row ->
                if (other.label == label) type.unify(other.type)
                else other.unifyRight(this)

            else -> other.unifyRight(this)
        }

        fun add(label: String, type: Node): Row {
            return Row(label, type, this)
        }
    }


}