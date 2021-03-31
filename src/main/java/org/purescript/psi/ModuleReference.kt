package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.FilenameIndex
import com.intellij.util.gist.GistManager
import com.intellij.util.io.EnumeratorStringDescriptor
import org.purescript.file.PSFile
import org.purescript.file.PSFileType
import org.purescript.psi.imports.PSImportDeclarationImpl

val gistManager = GistManager.getInstance()
val newPsiFileGist = gistManager.newPsiFileGist(
    "module name",
    0,
    EnumeratorStringDescriptor()
) { file ->
    when (file) {
        is PSFile -> file.module?.name
        else -> null
    }
}

class ModuleReference(element: PSImportDeclarationImpl) : PsiReferenceBase<PSImportDeclarationImpl>(
    element,
    element.importName?.textRangeInParent ?: TextRange.allOf(element.text.trim()),
    false
) {
    override fun getVariants(): Array<PSModule> {
        return candidateFiles
            .mapNotNull { it.module }
            .toTypedArray()
    }

    private val candidateFiles: List<PSFile>
        get() {
            val project = element.project
            val psiManager = PsiManager.getInstance(project)
            val filterIsInstance = FilenameIndex
                .getAllFilesByExt(project, PSFileType.DEFAULT_EXTENSION)
                .mapNotNull { psiManager.findFile(it) }
                .filterIsInstance<PSFile>()
            return filterIsInstance
        }

    override fun resolve(): PSModule? {
        val moduleName = element.importName?.name
            ?: return null
        val fileName = moduleName.split(".").last() + ".purs"
        val file = candidateFiles
            .firstOrNull { newPsiFileGist.getFileData(it) == moduleName }
        return file?.module
    }
}
