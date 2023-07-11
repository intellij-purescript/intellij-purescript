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
import org.purescript.module.Module
import java.nio.file.Path
import java.nio.file.Paths

class PSFile(viewProvider: FileViewProvider) :
    PsiFileBase(viewProvider, PSLanguage) {
    class Stub(file: PSFile) : PsiFileStubImpl<PSFile>(file) {
        override fun getType() = Type
    }

    object Type : IStubFileElementType<Stub>("PSFile", PSLanguage) {
        override fun getStubVersion(): Int = 14
        override fun getBuilder(): StubBuilder = object : DefaultStubBuilder() {
            override fun createStubForFile(file: PsiFile): Stub = Stub(file as PSFile)
        }
    }
        override fun getFileType(): FileType = PSFileType
        override fun toString(): String = "Purescript File"

        /**
         * @return the [Module] that this file contains,
         * or null if the module couldn't be parsed
         */
        val module: Module?
            get() = findChildByClass(Module::class.java)

    fun suggestModuleName(): String? {
        val fileName = name.removeSuffix(".purs")
        val path = parent?.virtualFile?.path ?: return null
        val directoryPath = Paths.get(path)
        val relativePath = try {
            project
                .basePath
                ?.let { Paths.get(it).toAbsolutePath() }
                ?.relativize(directoryPath.toAbsolutePath())
                ?: directoryPath
        } catch (ignore: IllegalArgumentException) {
            directoryPath
        }
        return relativePath
            .reversed<Path?>()
            .takeWhile<Path?> { "$it" != "src" && "$it" != "test" }
            .filter<Path?> { "$it".first().isUpperCase() }
            .reversed<Path?>()
            .joinToString<Path?>(".")
            .let<String, String> {
                "$it.$fileName"
            }
            .removePrefix(".")
    }

}
