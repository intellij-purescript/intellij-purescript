package org.purescript.module.declaration.value.expression.identifier

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentsOfType
import org.purescript.ide.formatting.ImportedValue
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.imports.ImportQuickFix
import org.purescript.module.declaration.imports.ReExportedImportIndex
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.psi.PSPsiFactory

class ExpressionIdentifierReference(expressionIdentifier: PSExpressionIdentifier) :
    LocalQuickFixProvider,
    PsiReferenceBase<PSExpressionIdentifier>(
        expressionIdentifier,
        expressionIdentifier.qualifiedIdentifier.identifier.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        moduleLocalCandidates
            .map {
                when (it) {
                    is ValueDeclarationGroup -> LookupElementBuilder
                        .createWithIcon(it)
                        .withTypeText(it.type?.text)
                        .withTailText(it.module.name?.let { "($it)" })

                    else -> it
                }
            }.toList().toTypedArray()

    override fun resolve(): PsiNamedElement? {
        val name = element.name
        return when (val qualifyingName = element.qualifiedIdentifier.moduleName?.name) {
            null -> element
                .parentsOfType<ValueNamespace>(withSelf = false)
                .flatMap { it.valueNames }
                .takeWhile { it.containingFile == element.containingFile }
                .firstOrNull { it.name == name }
                ?: getImportedCandidates(null).firstOrNull { it.name == name }

            else -> getImportedCandidates(qualifyingName).firstOrNull { it.name == name }
        } 
    }

    private fun getImportedCandidates(qualifyingName: String?): Sequence<PsiNamedElement> {
        val module = element.module
        val name = element.name
        return sequence {
            val importDeclarations = module.cache.imports
                .filter { it.importAlias?.name == qualifyingName }
            yieldAll(importDeclarations.flatMap { it.importedValue(name) })
        }
    }

    private val moduleLocalCandidates: Sequence<PsiNamedElement>
        get() = when (element.qualifiedIdentifier.moduleName?.name) {
            null -> element
                .parentsOfType<ValueNamespace>(withSelf = false)
                .flatMap { it.valueNames }
                .filter { it.containingFile == element.containingFile }
            else -> emptySequence()
        }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val qualifyingName = element.qualifiedIdentifier.moduleName?.name
        val project = element.project
        val scope = GlobalSearchScope.allScope(project)
        val importable = ImportableIndex.get(element.name, project, scope).toList()
        val exported = importable.mapNotNull { it.asImport()?.withAlias(qualifyingName) }
        return when (exported.size) {
            0 -> arrayOf()
            1 -> arrayOf(ImportQuickFix(*exported.toTypedArray()))
            else -> {
                val reExports = exported
                    .map { it.moduleName }
                    .flatMap { ReExportedImportIndex.get(it, project, scope) }
                    .mapNotNull { import ->
                        import.module.asImport()
                            ?.withItems(ImportedValue(element.name))
                            ?.withAlias(qualifyingName)
                    }
                arrayOf(ImportQuickFix(*(reExports + exported).toTypedArray()))
            }
        }
    }


    override fun handleElementRename(name: String): PsiElement? {
        val oldName = element.qualifiedIdentifier.identifier
        val newName = PSPsiFactory(element.project).createIdentifier(name)
            ?: return null
        oldName.replace(newName)
        return element
    }
}
