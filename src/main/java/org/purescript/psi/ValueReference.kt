package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.PsiElementResolveResult.createResults

class ValueReference(element: PSVar) : PsiReferenceBase.Poly<PSVar>(
    element,
    TextRange.allOf(element.text.trim()),
    false
) {

    override fun getVariants(): Array<PSValueDeclaration> {
        val currentModule = myElement.module
        return (
            currentModule.valueDeclarations +
                currentModule.importedValueDeclarations
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
            .valueDeclarationsByName
            .getOrDefault(name, emptyList())
            .asSequence()
        val importedDeclarations = importedModules
            .map { it.exportedValueDeclarationsByName[name] }
            .filterNotNull()
            .flatMap { it.asSequence() }
        val declarations =
            (importedDeclarations + localDeclarations).filterNotNull().toList()
        return createResults(declarations)
    }

}