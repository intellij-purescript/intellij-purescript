package org.purescript.inference

import com.intellij.psi.PsiReference

sealed interface InferType {
    val argument: InferType?

    @JvmInline
    value class Id(val id: Int) : InferType {
        override val argument: InferType? get() = null
        override fun contains(t: Id) = t == this
        override fun toString(): String = "u$id"
        override fun withNewIds(map: (Id) -> Id): Id = map(this)
    }

    @JvmInline
    value class Constructor(val name: String) : InferType {
        override val argument: InferType? get() = null
        override fun contains(t: Id): Boolean = false
        override fun toString(): String = name
        override fun withNewIds(map: (Id) -> Id): InferType = this
    }

    @JvmInline
    value class Prim(val name: String) : InferType {
        override val argument: InferType? get() = null
        override fun contains(t: Id): Boolean = false
        override fun withNewIds(map: (Id) -> Id): InferType = this
        override fun toString(): String = name
    }

    data class App(val f: InferType, val on: InferType) : InferType {
        override val argument: InferType?
            get() = when {
                f is App && f.f == Function -> f.on
                else -> null
            }
        val isFunction get() = f is App && f.f == Function
        override fun contains(t: Id): Boolean = f.contains(t) || on.contains(t)
        override fun withNewIds(map: (Id) -> Id): InferType = App(f.withNewIds(map), on.withNewIds(map))
        override fun toString() = when {
            f == Record && on is Row -> on.mergedLabels()
                .joinToString(", ", "{ ", " }") { "${it.first}::${it.second}" }

            f is App && f.f == Function ->
                if (f.on is App && f.on.isFunction) "(${f.on}) -> $on"
                else "${f.on} -> $on"

            on is App -> "$f ($on)"
            else -> "$f $on"
        }
    }

    sealed interface Row : InferType {
        override fun withNewIds(map: (Id) -> Id): Row
        fun mergedLabels(): List<Pair<String, InferType>>
    }

    data class RowList(val labels: List<Pair<String, InferType>>) : Row {
        override val argument: InferType? get() = null
        override fun contains(t: Id) = labels.any { it.second.contains(t) }
        override fun toString() = labels.joinToString(", ", "(", ")") { "${it.first}::${it.second}" }
        override fun withNewIds(map: (Id) -> Id): RowList =
            RowList(labels.map { it.first to it.second.withNewIds(map) })

        override fun mergedLabels(): List<Pair<String, InferType>> = labels
    }

    @JvmInline
    value class RowId(val id: Id) : Row {
        override val argument: InferType? get() = null
        override fun withNewIds(map: (Id) -> Id): Row = RowId(id.withNewIds(map))
        override fun mergedLabels(): List<Pair<String, InferType>> = emptyList()
        override fun contains(t: Id): Boolean = id.contains(t)
    }

    data class RowMerge(val left: Row, val right: Row) : Row {
        override val argument: InferType? get() = null
        override fun contains(t: Id): Boolean = left.contains(t) || right.contains(t)
        override fun toString() = mergedLabels().joinToString(", ", "(", ")") { "${it.first}::${it.second}" }
        override fun withNewIds(map: (Id) -> Id): RowMerge = RowMerge(left.withNewIds(map), right.withNewIds(map))
        override fun mergedLabels(): List<Pair<String, InferType>> = left.mergedLabels() + right.mergedLabels()
    }

    data class Constraint(val constraint: InferType, val of: InferType) : InferType {
        override val argument: InferType? get() = of.argument
        override fun toString() = "$constraint => $of"
        override fun contains(t: Id): Boolean = constraint.contains(t) || of.contains(t)
        override fun withNewIds(map: (Id) -> Id) = Constraint(constraint.withNewIds(map), of.withNewIds(map))
    }

    data class Alias(val name: String, val type: InferType) : InferType {
        override val argument: InferType? get() = type.argument
        override fun toString(): String = name
        override fun contains(t: Id): Boolean = type.contains(t)
        override fun withNewIds(map: (Id) -> Id): InferType = Alias(name, type.withNewIds(map))
    }

    data class ForAll(val name: Id, val scope: InferType) : InferType {
        override val argument get() = scope.argument
        override fun contains(t: Id): Boolean = name == t || scope.contains(t)
        override fun withNewIds(map: (Id) -> Id): InferType = ForAll(map(name), scope.withNewIds(map))
        override fun toString(): String = "forall $name. $scope"
    }

    fun app(other: InferType): App = App(this, other)
    fun contains(t: Id): Boolean
    fun withNewIds(map: (Id) -> Id): InferType
    fun withoutConstraints(): InferType = when (this) {
        is Constraint -> of.withoutConstraints()
        is Alias -> Alias(name, type.withoutConstraints())
        is App -> App(f.withoutConstraints(), on.withoutConstraints())
        is Constructor -> this
        is Id -> this
        is Prim -> this
        is RowId -> this
        is RowList -> RowList(labels.map { it.first to it.second.withoutConstraints() })
        is RowMerge -> RowMerge(left.withoutConstraints() as Row, right.withoutConstraints() as Row)
        is ForAll -> ForAll(name, scope.withoutConstraints())
    }

    companion object {
        val Union = Prim("Row.Union")
        val Char = Prim("Char")
        val Boolean = Prim("Boolean")
        val Int = Prim("Int")
        val Number = Prim("Number")
        val String = Prim("String")
        val Function = Prim("Function")
        val Array = Prim("Array")
        val Record = Prim("Record")
        fun function(vararg parameters: InferType): InferType =
            parameters.dropLast(1).foldRight(parameters.last()) { param, ret -> Function.app(param).app(ret) }

        fun record(labels: List<Pair<String, InferType>>) = Record.app(RowList(labels))
    }
}

data class IdGenerator(private var unknownCounter: Int = 0) {
    fun newId(): InferType.Id = InferType.Id(unknownCounter++)
}

fun Map<InferType.Id, InferType>.substitute(t: InferType): InferType = substitute(t) { it }

fun Map<InferType.Id, InferType>.substituteRow(t: InferType.Row): InferType.Row = when (t) {
    is InferType.RowList -> InferType.RowList(t.labels.map { it.first to substitute(it.second) })
    is InferType.RowMerge -> InferType.RowMerge(substituteRow(t.left), substituteRow(t.right))
    is InferType.RowId -> when (val it = substitute(t.id)) {
        is InferType.Row -> it
        is InferType.Id -> InferType.RowId(it)
        else -> error("substitution of row but got ${it::class.java} value $it")
    }
}

tailrec fun Map<InferType.Id, InferType>.substitute(t: InferType, andThen: (InferType) -> InferType): InferType =
    when (t) {
        is InferType.Prim, is InferType.Constructor -> andThen(t)
        is InferType.Id -> {
            val substitution = this[t]
            if (substitution != null) {
                if (substitution.contains(t)) throw RecursiveTypeException(substitution)
                else substitute(substitution, andThen)
            } else andThen(t)
        }

        is InferType.App -> substitute(t.f) { f ->
            andThen(InferType.App(f, substitute(t.on)))
        }

        is InferType.Constraint -> substitute(t.constraint) { constraint ->
            andThen(InferType.Constraint(constraint, substitute(t.of)))
        }

        is InferType.Alias -> substitute(t.type) { type ->
            andThen(InferType.Alias(t.name, type))
        }

        is InferType.Row -> substituteRow(t)
        is InferType.ForAll -> {
            substitute(t.scope) {
                andThen(
                    when (val name = substitute(t.name)) {
                        is InferType.Id -> InferType.ForAll(name, it)
                        else -> it
                    }
                )
            }
        }
    }

fun MutableMap<InferType.Id, InferType>.unify(x: InferType, y: InferType) {
    val sx = substitute(x)
    val sy = substitute(y)
    when {
        sx == sy -> return
        sx is InferType.Id -> this[sx] = sy
        sy is InferType.Id -> this[sy] = sx
        sy is InferType.ForAll -> unify(sy.scope, sx)
        sx is InferType.ForAll -> unify(sx.scope, sy)
        sx is InferType.App && sy is InferType.App -> {
            unify(sx.f, sy.f)
            unify(sx.on, sy.on)
        }

        sx is InferType.Row && sy is InferType.Row -> {
            unifyLabels(sx, sy)
            unifyRowId(sx, sy)
        }

        sx is InferType.Constraint && sy is InferType.Constraint -> {
            unify(sx.of, sy.of)
        }

        sx is InferType.Constraint -> unify(sx.of, sy)
        sy is InferType.Constraint -> unify(sx, sy.of)
        sy is InferType.Alias -> unify(sx, sy.type)
        sx is InferType.Alias -> unify(sx.type, sy)
    }
}

fun MutableMap<InferType.Id, InferType>.unifyRowId(x: InferType.Row, y: InferType.Row) {
    val sx = substituteRow(x)
    val sy = substituteRow(y)
    when (sx) {
        is InferType.RowId -> this[sx.id] = sy
        is InferType.RowMerge -> when (sy) {
            is InferType.RowId -> this[sy.id] = sx
            is InferType.RowList -> {
                val all = sy.labels
                val left = all.toSet().subtract(sx.right.mergedLabels().toSet())
                val right = all.toSet().subtract(sx.left.mergedLabels().toSet())
                unifyRowId(sx.left, InferType.RowList(left.toList()))
                unifyRowId(sx.right, InferType.RowList(right.toList()))
            }

            is InferType.RowMerge -> {
                unifyRowId(sx, InferType.RowList(sy.mergedLabels()))
                unifyRowId(sy, InferType.RowList(sx.mergedLabels()))
            }
        }

        is InferType.RowList -> when (sy) {
            is InferType.RowId -> this[sy.id] = sx
            is InferType.RowList -> {}
            is InferType.RowMerge -> {
                val all = sx.labels
                val left = all.toSet().subtract(sy.right.mergedLabels().toSet())
                val right = all.toSet().subtract(sy.left.mergedLabels().toSet())
                unifyRowId(sy.left, InferType.RowList(left.toList()))
                unifyRowId(sy.right, InferType.RowList(right.toList()))
            }
        }
    }
}

fun MutableMap<InferType.Id, InferType>.unifyLabels(sx: InferType.Row, sy: InferType.Row) {
    for ((xLabel, xType) in sx.mergedLabels()) {
        for ((yLabel, yType) in sy.mergedLabels()) {
            if (yLabel == xLabel) unify(xType, yType)
        }
    }
}

interface Inferable : HasTypeId, Unifiable {
    fun inferType(): InferType {
        this.unify()
        return this.substitutedType
    }
}

interface HasTypeId {
    val typeId: InferType.Id?
    val substitutedType: InferType
}

interface Unifiable {
    fun unify()
}


class RecursiveTypeException(t: InferType) : Exception("$t is recursive")

fun PsiReference.inferType(map: (InferType.Id) -> InferType.Id): InferType? =
    (this.resolve() as? Inferable)?.inferType()?.withNewIds(map)