package org.purescript.module.declaration.value.expression

import org.purescript.psi.InlinableElement

interface ReplaceableWithInline: Expression {
    fun replaceWithInline(toInlineWith: InlinableElement)
    fun canBeReplacedWithInline(): Boolean
}