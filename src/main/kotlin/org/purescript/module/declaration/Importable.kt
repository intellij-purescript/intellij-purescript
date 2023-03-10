package org.purescript.module.declaration

import com.intellij.psi.PsiNamedElement
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.module.declaration.type.PSType

interface Importable: PsiNamedElement {
    fun asImport(): ImportDeclaration?
    val type: PSType?
}