package org.purescript.typechecker

interface TypeCheckable {
    fun checkType(): TypeCheckerType?
}