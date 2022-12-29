package org.purescript.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import org.purescript.PSLanguage
import org.purescript.psi.module.Module
import org.purescript.psi.declaration.PSValueDeclaration

interface PSFile {
    interface Stub: PsiFileStub<Psi>
    object Type : IStubFileElementType<Stub>(PSLanguage.INSTANCE)
    class Psi(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, PSLanguage.INSTANCE) {
        override fun getFileType(): FileType {
            return PSFileType.INSTANCE
        }

        override fun toString(): String {
            return "Purescript File"
        }

        /**
         * @return the [Module.Psi] that this file contains,
         * or null if the module couldn't be parsed
         */
        val module: Module.Psi?
            get() = findChildByClass(Module.Psi::class.java)

        val topLevelValueDeclarations: Map<String, List<PSValueDeclaration>>
            get() = module?.cache?.valueDeclarations?.groupBy { it.name }
                ?: emptyMap()

    }
}
