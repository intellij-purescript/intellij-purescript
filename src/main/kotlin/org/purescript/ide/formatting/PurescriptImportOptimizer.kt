package org.purescript.ide.formatting

import com.intellij.lang.ImportOptimizer
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import org.purescript.file.PSFile
import org.purescript.psi.PSPsiFactory

class PurescriptImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean = file is PSFile
    override fun processFile(file: PsiFile): Runnable {
        val psFile = file as PSFile
        val module = psFile.module
            ?: error("File contains no Purescript module: ${file.name} ")
        val factory: PSPsiFactory = file.project.service()
        val fromModule = module.cache.imports.map { ImportDeclaration.fromPsiElement(it) }
        val psiPair = if (fromModule.isEmpty()) null
        else {
            val importDeclarations = ImportDeclarations(fromModule.toSet())
            factory.createImportDeclarations(importDeclarations)
        }
        return Runnable {
            for (importDeclaration in module.cache.imports) {
                importDeclaration.delete()
            }
            val where = module.whereKeyword
            where.siblings(forward = true, withSelf = false)
                .takeWhile { it is PsiWhiteSpace }
                .forEach { it.delete() }
            if (where.nextSibling != null) {
                module.addAfter(factory.createNewLines(2), where)
            } else {
                module.addAfter(factory.createNewLines(1), where)
            }
            when (psiPair) {
                null , null to null -> {}
                else -> {
                    module.addRangeAfter(psiPair.first, psiPair.second, where)
                    module.addAfter(factory.createNewLines(2), where)
                }
            }
        }
    }

}
