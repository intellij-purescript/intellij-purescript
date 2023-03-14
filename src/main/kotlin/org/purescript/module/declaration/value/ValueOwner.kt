package org.purescript.module.declaration.value

interface ValueOwner: ValueNamespace {
    fun addTypeDeclaration(variable: ValueDeclarationGroup): ValueDeclarationGroup
}