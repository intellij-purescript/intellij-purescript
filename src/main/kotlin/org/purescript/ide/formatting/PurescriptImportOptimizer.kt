package org.purescript.ide.formatting

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.ImportOptimizer
import com.intellij.openapi.components.service
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.purescript.file.PSFile
import org.purescript.ide.inspections.UnusedInspection
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.declaration.imports.PSImportedData

class PurescriptImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean = file is PSFile
    override fun processFile(file: PsiFile): Runnable {
        val psFile = file as PSFile
        val module = psFile.module
            ?: error("File contains no Purescript module: ${file.name} ")
        val factory: PSPsiFactory = file.project.service()
        val project = file.project
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(file) ?: return Runnable {  }
        val holder = ProblemsHolder(InspectionManager.getInstance(project), file, false)
        val visitor = UnusedInspection().buildVisitor(holder, false)
        for (import in module.cache.imports) {
            import.importedItems.forEach {
                when (it) {
                    is PSImportedData -> {
                        visitor.visitElement(it)
                        it.importedDataMembers.forEach { member ->
                            visitor.visitElement(member)
                        }
                    }

                    else -> visitor.visitElement(it)
                }
            }
        }
        return Runnable {
            for (problemDescriptor in holder.results) {
                problemDescriptor.fixes?.filterIsInstance<UnusedInspection.UnusedImport>()?.forEach {
                    it.applyFix(project, problemDescriptor)
                }
            }
            documentManager.commitDocument(document)
            val importsToReplace = module.cache.imports
            val fromModule = importsToReplace.map { ImportDeclaration.fromPsiElement(it) }
            if (fromModule.isEmpty()) return@Runnable
            val importDeclarations = ImportDeclarations(fromModule.toSet())
            val rangeToReplace = importsToReplace.first().textRange.union(importsToReplace.last().textRange)
            document.replaceString(rangeToReplace.startOffset, rangeToReplace.endOffset, importDeclarations.text)
            documentManager.commitDocument(document)
        }
    }

}
