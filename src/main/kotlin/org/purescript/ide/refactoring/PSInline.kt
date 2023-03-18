package org.purescript.ide.refactoring

import com.intellij.ide.plugins.PluginManagerCore.isUnitTestMode
import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.alsoIfNull
import org.purescript.PSLanguage
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.VarBinder
import org.purescript.module.declaration.value.expression.dostmt.PSDoNotationLet
import org.purescript.module.declaration.value.expression.identifier.PSExpressionIdentifier
import org.purescript.module.declaration.value.expression.namespace.PSExpressionWhere
import org.purescript.module.declaration.value.expression.namespace.PSLet

class PSInline : InlineActionHandler() {
    override fun isEnabledForLanguage(l: Language?): Boolean =
        l == PSLanguage

    override fun canInlineElement(element: PsiElement?): Boolean {
        return if (element is ValueDeclarationGroup) {
            element.valueDeclarations.singleOrNull()?.let { true } ?: false
        } else {
            false
        }
    }

    override fun inlineElement(project: Project, editor: Editor, element: PsiElement?) {
        val document = editor.document
        val file = PsiDocumentManager.getInstance(project).getPsiFile(document)
        val offset = editor.caretModel.offset
        val original = file
            ?.findElementAt(offset)
            ?.parentOfType<PSExpressionIdentifier>(true)
        when (element) {
            is ValueDeclarationGroup -> {
                val valueDeclaration = element.valueDeclarations.singleOrNull() ?: error("can only inline value declarations with one body")
                val binders = valueDeclaration.parameterList?.parameterBinders ?: emptyList()
                if (binders.any { it !is VarBinder }) error("can only inline simple parameters")
                
                val dialog = InlineDialog(project, element, original) {
                    InlineProcessor(this) { usages ->
                        for (usage in usages) {
                            (usage.element as? PSExpressionIdentifier)?.replaceWithInline(valueDeclaration)
                        }
                        // delete declaration
                        if (!isInlineThisOnly) when (val parent = toInline.parent) {
                            is PSLet ->
                                if (parent.childrenOfType<ValueDeclarationGroup>().size == 1) {
                                    parent.value?.let { parent.parent.parent.replace(it) }
                                        .alsoIfNull { toInline.delete() }
                                } else {
                                    toInline.delete()
                                }

                            is PSDoNotationLet, is PSExpressionWhere ->
                                if (parent.childrenOfType<ValueDeclarationGroup>().size == 1) {
                                    parent.delete()
                                } else {
                                    toInline.delete()
                                }

                            else -> toInline.delete()
                        }

                    }
                }
                if (!isUnitTestMode) {
                    dialog.show()
                } else {
                    try {
                        dialog.doAction()
                    } finally {
                        dialog.close(DialogWrapper.OK_EXIT_CODE, true)
                    }
                }
            }
        }
    }
}