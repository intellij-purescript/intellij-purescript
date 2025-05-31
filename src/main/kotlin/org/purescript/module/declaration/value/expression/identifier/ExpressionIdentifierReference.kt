package org.purescript.module.declaration.value.expression.identifier

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex.getElements
import com.intellij.psi.util.parentsOfType
import org.purescript.ide.formatting.ImportedValue
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.imports.Import
import org.purescript.module.declaration.imports.ImportQuickFix
import org.purescript.module.declaration.imports.ImportsInModule
import org.purescript.module.declaration.imports.ImportsInModuleAndWithAlias
import org.purescript.module.declaration.imports.ImportsInModuleWithoutAlias
import org.purescript.module.declaration.imports.ReExportedImportIndex
import org.purescript.module.declaration.value.TopLevelValueDeclarationsByModule
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
    val project = expressionIdentifier.project
    override fun getVariants(): Array<Any> =
        moduleLocalCandidates
            .map {
                when (it) {
                    is ValueDeclarationGroup -> LookupElementBuilder
                        .createWithIcon(it)
                        .withTypeText(it.type?.text)
                        .withTailText(it.module.name.let { "($it)" })

                    else -> it
                }
            }.toList().toTypedArray()

    override fun resolve(): PsiNamedElement? {
        val name = element.name
        return when (val qualifyingName = element.qualifierName) {
            null -> {
                element
                    .parentsOfType<ValueNamespace>(withSelf = false)
                    .flatMap { it.valueNames }
                    .takeWhile { it.containingFile == element.containingFile }
                    .firstOrNull { it.name == name }
                    ?: getImportedCandidates().firstOrNull { it.name == name }
            }

            else -> getQualifiedImportedCandidates(qualifyingName).firstOrNull { it.name == name }
        } 
    }

    private fun getImportedCandidates(): Sequence<PsiNamedElement> {
        val moduleName = this.element.module.name
        val name = element.name
        return sequence {
            val importDeclarations = importsInModuleWithoutAlias(moduleName)
            for (importDeclaration in importDeclarations) {
                yieldAll(importDeclaration.importedValue(name))
            }
        }
    }

    private fun getQualifiedImportedCandidates(qualifyingName: String): Sequence<PsiNamedElement> {
        val moduleName = this.element.module.name
        val name = element.name
        return sequence {
            val importDeclarations = importsInModuleAndWithAlias(moduleName, qualifyingName)
            for (importDeclaration in importDeclarations) {
                yieldAll(importDeclaration.importedValue(name))
            }
        }
    }

    fun importsInModule(moduleName: String) = getElements(
        ImportsInModule.KEY,
        moduleName,
        project,
        null,
        Import::class.java
    )

    fun importsInModuleWithoutAlias(moduleName: String) = getElements(
        ImportsInModuleWithoutAlias.KEY,
        moduleName,
        project,
        null,
        Import::class.java
    )

    fun importsInModuleAndWithAlias(moduleName: String, alias: String) = getElements(
        ImportsInModuleAndWithAlias.KEY,
        "$moduleName&$alias",
        project,
        null,
        Import::class.java
    )

    fun topLevelValueDeclarationsInModule(moduleName: String) = getElements(
        TopLevelValueDeclarationsByModule.KEY,
        moduleName,
        project,
        null,
        ValueDeclarationGroup::class.java
    )

    private val moduleLocalCandidates: Sequence<PsiNamedElement>
        get() = when (element.qualifierName) {
            null -> element
                .parentsOfType<ValueNamespace>(withSelf = false)
                .flatMap { it.valueNames }
                .filter { it.containingFile == element.containingFile }
            else -> emptySequence()
        }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val qualifyingName = element.qualifierName
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
                            .withItems(ImportedValue(element.name))
                            .withAlias(qualifyingName)
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
