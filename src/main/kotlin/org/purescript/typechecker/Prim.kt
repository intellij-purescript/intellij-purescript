package org.purescript.typechecker

object Prim {
    val int = TypeCheckerType.TypeConstructor("Prim.Int")
    val string = TypeCheckerType.TypeConstructor("Prim.String")
    val function = TypeCheckerType.TypeConstructor("Prim.Function")
} 


