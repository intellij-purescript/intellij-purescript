package org.purescript.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IStubFileElementType
import org.purescript.PSLanguage
import org.purescript.psi.declaration.value.ValueDecl
import org.purescript.psi.module.Module

interface PSFile {
    class Stub(file: Psi) : PsiFileStubImpl<Psi>(file) {
        override fun getType() = Type
    }

    object Type : IStubFileElementType<Stub>("PSFile", PSLanguage) {
        override fun getBuilder(): StubBuilder = object : DefaultStubBuilder() {
            override fun createStubForFile(file: PsiFile): Stub = Stub(file as Psi)
        }
    }

    class Psi(viewProvider: FileViewProvider) :
        PsiFileBase(viewProvider, PSLanguage) {
        override fun getFileType(): FileType = PSFileType
        override fun toString(): String = "Purescript File"

        /**
         * @return the [Module.Psi] that this file contains,
         * or null if the module couldn't be parsed
         */
        val module: Module.Psi?
            get() = findChildByClass(Module.Psi::class.java)

        val topLevelValueDeclarations: Map<String, List<ValueDecl>>
            get() = module?.cache?.valueDeclarations?.groupBy { it.name }
                ?: emptyMap()

    }
}
