package org.purescript.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.purescript.psi.PSModule
import org.purescript.psi.PSValueDeclaration

class PSFile(viewProvider: FileViewProvider) :
    PsiFileBase(viewProvider, org.purescript.PSLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return PSFileType.INSTANCE
    }

    override fun toString(): String {
        return "Purescript File"
    }

    val module: PSModule
        get() = findChildByClass(PSModule::class.java)!!

    val topLevelValueDeclarations: Map<String, List<PSValueDeclaration>>
        get() = module.valueDeclarations.groupBy { it.name }

}