package net.kenro.ji.jin.purescript.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import net.kenro.ji.jin.purescript.PSLanguage
import net.kenro.ji.jin.purescript.psi.impl.PSProgramImpl
import net.kenro.ji.jin.purescript.psi.impl.PSValueDeclarationImpl
import javax.swing.Icon

class PSFile(viewProvider: FileViewProvider) :
    PsiFileBase(viewProvider, PSLanguage.INSTANCE) {
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