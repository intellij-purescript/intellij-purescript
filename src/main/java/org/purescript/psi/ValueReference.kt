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

    override fun getVariants(): Array<PsiNamedElement> {
        return (myElement.containingFile as PSFile)
            .topLevelValueDeclarations
            .values
            .flatten()
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val name = myElement.text.trim()
        val file = myElement.containingFile as PSFile
        val module = file.module
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