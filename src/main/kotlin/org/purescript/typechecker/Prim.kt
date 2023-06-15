package org.purescript.typechecker

object Prim {
    val int = TypeCheckerType.TypeConstructor("Prim.Int")
    val char = TypeCheckerType.TypeConstructor("Prim.Char")
    val boolean = TypeCheckerType.TypeConstructor("Prim.Boolean")
    val string = TypeCheckerType.TypeConstructor("Prim.String")
    val function = TypeCheckerType.TypeConstructor("Prim.Function")
    val array = TypeCheckerType.TypeConstructor("Prim.Array")
} 


