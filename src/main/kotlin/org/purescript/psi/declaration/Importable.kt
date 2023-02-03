package org.purescript.psi.declaration

import org.purescript.ide.formatting.ImportDeclaration

interface Importable {
    fun asImport(): ImportDeclaration?
}