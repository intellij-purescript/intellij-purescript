package org.purescript.ide.refactoring

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parents
import com.intellij.psi.util.parentsOfType
import com.intellij.refactoring.RefactoringBundle.message
import com.intellij.refactoring.introduce.IntroduceHandler
import com.intellij.refactoring.introduce.PsiIntroduceTarget
import com.intellij.refactoring.introduce.inplace.AbstractInplaceIntroducer
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser
import com.intellij.usageView.UsageInfo
import org.purescript.file.PSFileType
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.ValueOwner
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionSelector
import org.purescript.module.declaration.value.expression.identifier.PSExpressionIdentifier
import org.purescript.module.declaration.value.expression.namespace.PSLambda
import org.purescript.psi.PSPsiFactory

class ValueGroupIntroducer :
    IntroduceHandler<PsiIntroduceTarget<Expression>, ValueOwner>() {
    /**
     * example:
     * ```purescript
     * f n = if true then show n else show n
     * ```
     * `show n` can be extracted, and then the other `show n` is a usage
     *
     */
    override fun collectUsages(target: PsiIntroduceTarget<Expression>, scope: ValueOwner): MutableList<UsageInfo> {
        val psi = target.place ?: return mutableListOf()
        return scope.valueNames.filterIsInstance<ValueDeclarationGroup>()
            .flatMap { it.valueDeclarations.asSequence() }
            .flatMap { it.expressions }
            .filter { psi.areSimilarTo(it) }
            .map { UsageInfo(it) }
            .toMutableList()
    }

    override fun checkUsages(usages: MutableList<UsageInfo>) = null

    /**
     * foo = {-caret-}a + b + c
     * targets could be
     *  * a
     *  * a + b
     *  * a + b + c
     */
    override fun collectTargets(file: PsiFile, editor: Editor, project: Project)
            : Pair<MutableList<PsiIntroduceTarget<Expression>>, Int> {
        val offset = editor.caretModel.offset
        val psiUnderCursor = psiNextToOffset(file, offset)
            ?: return Pair.create(mutableListOf(), 0)
        val selector = ExpressionSelector()
        val expressions = selector.getNonFilteredExpressions(psiUnderCursor, editor.document, editor.caretModel.offset)
        val targets = expressions.map { PsiIntroduceTarget(it) }.toMutableList()
        return Pair.create(targets, 0) // 0 selected target
    }

    private fun psiNextToOffset(file: PsiFile, offset: Int) =
        psiAtOffset(file, offset) ?: psiAtOffset(file, offset - 1)

    override fun findSelectionTarget(start: Int, end: Int, file: PsiFile, editor: Editor, project: Project)
            : PsiIntroduceTarget<Expression>? {
        val startElement = file.findElementAt(start) ?: return null
        val endElement = file.findElementAt(end - 1) ?: return null
        val commonElement = startElement.parents(true).firstOrNull { it.textRange.contains(endElement.textRange) }
        return commonElement
            ?.parentsOfType<Expression>()
            ?.firstOrNull()
            ?.let { PsiIntroduceTarget(it) }
            ?.takeIf { checkSelectedTarget(it, file, editor, project) == null }
    }

    private fun psiAtOffset(file: PsiFile, offset: Int) =
        file.findElementAt(offset)?.parentsOfType<Expression>()?.firstOrNull()

    override fun getRefactoringName() = message("extract.method.title")
    override fun getHelpID() = null
    override fun getChooseScopeTitle() = "Choose scope <title>"
    override fun getScopeRenderer() = DefaultPsiElementCellRenderer() as PsiElementListCellRenderer<ValueOwner>
    override fun checkSelectedTarget(t: PsiIntroduceTarget<Expression>, f: PsiFile, e: Editor, p: Project): String? =
        null

    override fun collectTargetScopes(t: PsiIntroduceTarget<Expression>, e: Editor, f: PsiFile, p: Project)
            : MutableList<ValueOwner> {
        val all = (t.place
            ?.parentsOfType<ValueOwner>()
            ?.toMutableList()
            ?: mutableListOf())
        return if (PluginManagerCore.isUnitTestMode) {
            mutableListOf(all.last())
        } else {
            all
        }
    }

    override fun getIntroducer(
        target: PsiIntroduceTarget<Expression>,
        scope: ValueOwner,
        usages: MutableList<UsageInfo>,
        replaceChoice: OccurrencesChooser.ReplaceChoice,
        file: PsiFile,
        editor: Editor,
        project: Project
    ): AbstractInplaceIntroducer<ValueDeclarationGroup, Expression> {
        val factory = project.service<PSPsiFactory>()
        val occurrences = usages.map { it.element as Expression }.toTypedArray()
        val psi = target.place ?: error("Empty target")
        val expr = when (psi) {
            is PSLambda -> psi.value?.text
            else -> psi.text
        } ?: error("Could not extract text form expression")
        val identifiersInPsi = psi.expressions.filterIsInstance<PSExpressionIdentifier>().toList()
        val name = (identifiersInPsi.firstOrNull()?.name
            ?: "expr") + "'"
        
        val moduleNamespace = psi.module?.valueNames?.toSet() ?: emptySet()
        val scopeNamespace = scope.parentsOfType<ValueNamespace>().flatMap { it.valueNames }.toSet()
        val totalNamespace = moduleNamespace + scopeNamespace
        
        val parameters = identifiersInPsi.filter { identifier ->
            val reference = identifier.reference.resolve() ?: return@filter true
            val exprNamespace = identifier.parentsOfType<ValueNamespace>().takeWhile { 
                psi.textRange.contains(it.textRange)
            }.flatMap { it.valueNames }.toSet()
            reference !in (totalNamespace + exprNamespace) 
        }.distinctBy { it.name }.toList()
        
        val nameWithParameters = (sequenceOf(name) + parameters.map { it.name } + when (psi) {
            is PSLambda -> psi.parameters?.map { it.text }?.asSequence() ?: emptySequence()
            else -> emptySequence()
        }).joinToString(" ")
        return object : AbstractInplaceIntroducer<ValueDeclarationGroup, Expression>(
            project,
            editor,
            psi,
            null,
            occurrences,
            message("extract.method.title"),
            PSFileType
        ) {
            /* this should be the ID of the shortcut action,
            * not sure if and to what it is used */
            override fun getActionName() = "ExtractMethod"

            /*
            we currently don't have a settings balloon dialog and also have
            no settings to save
            */
            override fun getComponent() = null
            override fun saveSettings(variable: ValueDeclarationGroup) = Unit

            override fun setReplaceAllOccurrences(value: Boolean) = Unit

            // this seems to never be used
            override fun performIntroduce() = Unit

            /**
             * Unsure how this is supposed to work, but we return a un attached
             * version of the template
             */
            override fun getVariable() = factory.createValueDeclarationGroup(nameWithParameters, expr)
                ?: error("Could not create value declaration")

            /**
             * This inserts the extracted method into the document
             */
            override fun createFieldToStartTemplateOn(replaceAll: Boolean, names: Array<out String>) = runWriteAction {
                if (replaceAll) {
                    for (occurrence in occurrences) {
                        for (parameter in parameters) {
                            occurrence.parent?.addAfter(parameter, occurrence)
                            occurrence.parent?.addAfter(factory.createSpace(), occurrence)
                        }
                    }
                } else {
                    for (parameter in parameters) {
                        this.expr?.parent?.addAfter(parameter, this.expr)
                        this.expr?.parent?.addAfter(factory.createSpace(), this.expr)
                    }
                }
                scope.addTypeDeclaration(variable) as ValueDeclarationGroup
            }

            override fun suggestNames(replaceAll: Boolean, variable: ValueDeclarationGroup?): Array<String> =
                emptyArray()

            override fun isReplaceAllOccurrences() =
                when (replaceChoice) {
                    OccurrencesChooser.ReplaceChoice.NO -> false
                    OccurrencesChooser.ReplaceChoice.NO_WRITE -> false
                    OccurrencesChooser.ReplaceChoice.ALL -> true
                }

            override fun restoreExpression(
                containingFile: PsiFile,
                variable: ValueDeclarationGroup,
                marker: RangeMarker,
                exprText: String?
            ) = this.expr
        }
    }
}
