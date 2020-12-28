package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import org.jetbrains.annotations.NotNull
import org.purescript.file.PSFile

class ValueReference(element: @NotNull PSVar, rangeInElement: TextRange?) :
    PsiReferenceBase<PSVar?>(element, rangeInElement) {

    override fun resolve(): PSValueDeclaration? {
        val file = myElement?.containingFile as PSFile
        return file.topLevelValueDeclarations[myElement.text.trim()]
    }

}