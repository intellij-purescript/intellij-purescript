package org.purescript.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.purescript.PSLanguage
import org.purescript.psi.PSProgramImpl
import org.purescript.psi.PSValueDeclarationImpl

class PSFile(viewProvider: FileViewProvider) :
    PsiFileBase(viewProvider, org.purescript.PSLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return PSFileType.INSTANCE
    }

    override fun toString(): String {
        return "Purescript File"
    }

    val topLevelValueDeclarations: Map<String, PSValueDeclarationImpl>
        get() = program
            ?.module
            ?.topLevelValueDeclarations!!
    private val program: PSProgramImpl?
        private get() = findChildByClass(PSProgramImpl::class.java)
}