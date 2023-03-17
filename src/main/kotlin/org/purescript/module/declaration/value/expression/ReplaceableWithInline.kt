package org.purescript.module.declaration.value.expression

interface ReplaceableWithInline: Expression {
    fun replaceWithInline(numberOfArgumentsInlined: Int, toInlineWith: Expression)
}