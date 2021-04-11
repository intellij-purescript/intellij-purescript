package org.purescript.psi.expression

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parents
import org.purescript.psi.PSValueDeclaration

class ExpressionIdentifierReference(expressionConstructor: PSExpressionIdentifier) :
    PsiReferenceBase<PSExpressionIdentifier>(
        expressionConstructor,
        expressionConstructor.qualifiedIdentifier.identifier.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): PsiNamedElement? =
        candidates.firstOrNull { it.name == element.name }

    private val candidates: Sequence<PsiNamedElement>
        get() {
            val module = element.module ?: return emptySequence()
            val qualifyingName = element.qualifiedIdentifier.moduleName?.name
            if (qualifyingName != null) {
                val importDeclaration = module.importDeclarations
                    .firstOrNull { it.importAlias?.name == qualifyingName }
                    ?: return emptySequence()
                return sequence {
                    yieldAll(importDeclaration.importedValueDeclarations)
                    yieldAll(importDeclaration.importedForeignValueDeclarations)
                    // TODO Support imported class members
                }
            } else {
                return sequence {
                    for (parent in element.parents) {
                        when (parent) {
                            is PSValueDeclaration -> yieldAll(parent.varBindersInParameters.values)
                        }
                    }
                    // TODO Support values defined in the expression
                    yieldAll(module.valueDeclarations.toList())
                    yieldAll(module.foreignValueDeclarations.toList())
                    // TODO Support local class members
                    val importDeclarations = module.importDeclarations.filter { it.importAlias == null }
                    yieldAll(importDeclarations.flatMap { it.importedValueDeclarations })
                    yieldAll(importDeclarations.flatMap { it.importedForeignValueDeclarations })
                    // TODO Support imported class members
                }
            }
        }
}
