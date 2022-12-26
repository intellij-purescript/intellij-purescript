package org.purescript.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.purescript.PSLanguage
import org.purescript.psi.module.PSModule
import org.purescript.psi.declaration.PSValueDeclaration

class PSFile(viewProvider: FileViewProvider) :
    PsiFileBase(viewProvider, PSLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return PSFileType.INSTANCE
    }

    override fun toString(): String {
        return "Purescript File"
    }

    /**
     * @return the [PSModule] that this file contains,
     * or null if the module couldn't be parsed
     */
    val module: PSModule?
        get() = findChildByClass(PSModule::class.java)

    val topLevelValueDeclarations: Map<String, List<PSValueDeclaration>>
        get() = module?.let { it.cache.valueDeclarations }?.groupBy { it.name }
            ?: emptyMap()

}
