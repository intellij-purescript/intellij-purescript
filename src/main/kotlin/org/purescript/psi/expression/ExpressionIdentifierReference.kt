package org.purescript.psi.expression

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parents
import org.purescript.ide.formatting.ImportedValue
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.declaration.ImportableIndex
import org.purescript.psi.declaration.imports.ImportQuickFix
import org.purescript.psi.declaration.imports.ReExportedImportIndex
import org.purescript.psi.declaration.value.ValueDeclarationGroup
import org.purescript.psi.expression.dostmt.PSDoBlock

class ExpressionIdentifierReference(expressionConstructor: PSExpressionIdentifier) :
    LocalQuickFixProvider,
    PsiReferenceBase<PSExpressionIdentifier>(
        expressionConstructor,
        expressionConstructor.qualifiedIdentifier.identifier.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        moduleLocalCandidates
            .map {
                when (it) {
                    is ValueDeclarationGroup -> LookupElementBuilder
                        .createWithIcon(it)
                        .withTypeText(it.type?.text)
                        .withTailText(it.module?.name?.let { "($it)" })

                    else -> it
                }
            }.toList().toTypedArray()

    override fun resolve(): PsiNamedElement? {
        val name = element.name
        val importedCandidates = getImportedCandidates(name)
        return (moduleLocalCandidates + importedCandidates).firstOrNull { it.name == name }
    }

    private fun getImportedCandidates(name: String): Sequence<PsiNamedElement> {
        val module = element.module ?: return emptySequence()
        val qualifyingName = element.qualifiedIdentifier.moduleName?.name
        return sequence {
            val importDeclarations = module.cache.imports
                .filter { it.importAlias?.name == qualifyingName }
            yieldAll(importDeclarations.flatMap { it.importedValue(name) })
        }
    }

    private val moduleLocalCandidates: Sequence<PsiNamedElement>
        get() {
            val module = element.module ?: return emptySequence()
            val qualifyingName = element.qualifiedIdentifier.moduleName?.name
            return sequence {
                if (qualifyingName == null) {
                    for (parent in element.parents(false)) {
                        when (parent) {
                            is ValueDeclarationGroup -> {
                                parent.valueDeclarations.forEach { decl ->
                                    yieldAll(decl.namedBinders.values)
                                    yieldAll(decl.valueDeclarationGroups.asSequence())
                                }
                            }

                            is PSLambda -> yieldAll(parent.binders.filterIsInstance<PsiNamedElement>())

                            is PSExpressionWhere -> {
                                val valueDeclarations = parent
                                    .where
                                    ?.valueDeclarationGroups
                                    ?.asSequence()
                                    ?: sequenceOf()
                                yieldAll(valueDeclarations)
                            }

                            is PSLet ->
                                yieldAll(parent.valueDeclarationGroups.asSequence())

                            is PSDoBlock -> {
                                val binders = parent
                                    .statements
                                    .takeWhile { it.textOffset < element.textOffset }
                                    .flatMap { it.namedElements}
                                yieldAll(binders)
                            }
                        }
                    }
                    // TODO Support values defined in the expression
                    yieldAll(module.cache.valueDeclarationGroups.toList())
                    yieldAll(module.cache.foreignValueDeclarations.toList())
                    val localClassMembers = module
                        .cache.classes
                        .asSequence()
                        .flatMap { it.classMembers.asSequence() }
                    yieldAll(localClassMembers)
                }
            }
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
                        import.module?.asImport()
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
