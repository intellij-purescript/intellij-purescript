package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.PsiElementResolveResult.createResults
import org.purescript.file.PSFile

class ValueReference(element: PSVar) : PsiReferenceBase.Poly<PSVar>(
    element,
    TextRange.allOf(element.text.trim()),
    false
) {

    override fun getVariants(): Array<PSValueDeclaration> {
        val localValueDeclarations: Sequence<PSValueDeclaration> =
            (myElement.containingFile as PSFile)
                .topLevelValueDeclarations
                .values
                .flatten()
                .asSequence()

        val importEverythingNames: Sequence<PSValueDeclaration> =
            myElement.module
                .importDeclarations
                .asSequence()
                .filter { it.namedImports.isEmpty() }
                .map { ModuleReference(it).resolve() }
                .filterNotNull()
                .flatMap { it.exportedValueDeclarations.values.flatten() }

        val importWithHidesNames: Sequence<PSValueDeclaration> =
            myElement.module
                .importDeclarations
                .asSequence()
                .filter { it.isHiding }
                .flatMap { import ->
                    val module = ModuleReference(import).resolve()
                    if (module == null) {
                        listOf()
                    } else {
                        val exportedNames =
                            module.exportedValueDeclarations.keys
                        val keys =
                            exportedNames subtract (import.namedImports.toSet())
                        module.exportedValueDeclarations.filterKeys { it in keys }.values.flatten()
                    }
                }


        val importWithNames: Sequence<PSValueDeclaration> = myElement.module
            .importDeclarations
            .asSequence()
            .filter { !it.isHiding }
            .filter { it.namedImports.isNotEmpty() }
            .flatMap { import ->
                val keys = import.namedImports.toSet()
                val module = ModuleReference(import).resolve()
                if (module == null) {
                    listOf()
                } else {
                    module
                        .exportedValueDeclarations
                        .filterKeys { it in keys }
                        .values.flatten()
                }
            }

        return (
            localValueDeclarations +
                importWithNames +
                importEverythingNames +
                importWithHidesNames
            ).toList().toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val name = myElement.text.trim()
        val module = myElement.module
        val importedModules = module
            .importDeclarations
            .asSequence()
            .filter { it.isNotHidingName(name) }
            .map { ModuleReference(it).resolve() }
            .filterNotNull()
        val localDeclarations = module
            .topLevelValueDeclarations
            .getOrDefault(name, emptyList())
            .asSequence()
        val importedDeclarations = importedModules
            .map { it.exportedValueDeclarations[name] }
            .filterNotNull()
            .flatMap { it.asSequence() }
        val declarations =
            (importedDeclarations + localDeclarations).filterNotNull().toList()
        return createResults(declarations)
    }

}