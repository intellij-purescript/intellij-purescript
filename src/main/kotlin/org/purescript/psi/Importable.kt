package org.purescript.psi

import org.purescript.ide.formatting.ImportDeclaration

interface Importable {
    fun asImport(): ImportDeclaration?
}