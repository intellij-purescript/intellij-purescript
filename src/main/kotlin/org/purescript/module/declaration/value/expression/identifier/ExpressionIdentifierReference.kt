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
import com.jetbrains.rd.generator.nova.cpp.Signature
import org.purescript.Find
import org.purescript.ide.formatting.ImportedValue
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.imports.Import
import org.purescript.module.declaration.imports.ImportQuickFix
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
    val name = expressionIdentifier.name
    val qualifierName = expressionIdentifier.qualifierName
    val module = expressionIdentifier.module
    val find = Find(project)

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

    override fun resolve(): PsiNamedElement? = when (qualifierName) {
        null -> {
            insideTopLevel()
                ?: getTopLevelValuesInModule()
                ?: getImportedCandidates()
        }

        else -> getImportedCandidates()
    }

    private fun insideTopLevel(): PsiNamedElement? =
        moduleLocalCandidates.firstOrNull { it.name == name }

    private fun getTopLevelValuesInModule(): ValueDeclarationGroup? {
        return Find(element.project).topLevelValuesInModule(module.name).firstOrNull() { it.name == name }
    }

    private fun getImportedCandidates(): PsiNamedElement? {
        val moduleName = this.element.module.name
        val name = element.name
        return Find(element.project).importedValueInModule(name, moduleName, qualifierName)
    }

    private val moduleLocalCandidates: Sequence<PsiNamedElement>
        get() = when (qualifierName) {
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
                    .flatMap { Find.ExportedImportsInModule().get(it, project, scope) }
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
