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

    override fun getVariants(): Array<String> {
        val localValueDeclarations = (myElement.containingFile as PSFile)
            .topLevelValueDeclarations
            .keys

        val importEverythingNames = myElement.module
            .importDeclarations
            .asSequence()
            .filter { it.namedImports.isEmpty()}
            .map { ModuleReference(it).resolve()}
            .filterNotNull()
            .flatMap {it.exportedValueDeclarations.keys}
            .toSet()

        val importWithHidesNames = myElement.module
            .importDeclarations
            .asSequence()
            .filter { it.isHiding }
            .flatMap {
                (ModuleReference(it).resolve()?.exportedValueDeclarations?.keys
                    ?: setOf()) subtract (it.namedImports.toSet())
            }
            .toSet()

        val importWithNames = myElement.module
            .importDeclarations
            .asSequence()
            .flatMap { if (it.isHiding) setOf()  else  it.namedImports.toSet() }
            .toSet()

        return (localValueDeclarations + importWithNames + importEverythingNames + importWithHidesNames)
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val name = myElement.text.trim()
        val module = myElement.module
        val importedModules = module
            .importDeclarations
            .asSequence()
            .filter { it.isNotHidingName(name)}
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