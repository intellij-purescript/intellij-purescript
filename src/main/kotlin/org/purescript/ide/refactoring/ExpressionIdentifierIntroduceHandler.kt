package org.purescript.ide.refactoring

import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentsOfType
import com.intellij.refactoring.RefactoringBundle.message
import com.intellij.refactoring.introduce.IntroduceHandler
import com.intellij.refactoring.introduce.PsiIntroduceTarget
import com.intellij.refactoring.introduce.inplace.AbstractInplaceIntroducer
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser
import com.intellij.usageView.UsageInfo
import org.purescript.file.PSFileType
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.declaration.value.ValueDeclarationGroup
import org.purescript.psi.expression.PSExpressionIdentifier
import org.purescript.psi.module.Module

class ExpressionIdentifierIntroduceHandler :
    IntroduceHandler<PsiIntroduceTarget<PSExpressionIdentifier>, Module.Psi>() {
    override fun collectUsages(
        target: PsiIntroduceTarget<PSExpressionIdentifier>,
        scope: Module.Psi
    ): MutableList<UsageInfo> {
        val definition = target.place?.reference?.resolve()
        val exprAtoms = scope.cache.valueDeclarationGroups
            .flatMap { it.expressionAtoms }
            .toList()
        val usages = exprAtoms
            .filterIsInstance<PSExpressionIdentifier>()
            .filter {
                it.name == target.place?.name &&
                    it.reference.resolve() == definition
            }
        return usages.map { UsageInfo(it) }.toMutableList()
    }

    override fun checkUsages(usages: MutableList<UsageInfo>) = null

    /**
     * foo = {-caret-}a + b + c
     * targets could be
     *  * a
     *  * a + b
     *  * a + b + c
     */
    override fun collectTargets(
        file: PsiFile,
        editor: Editor,
        project: Project
    ): Pair<MutableList<PsiIntroduceTarget<PSExpressionIdentifier>>, Int> {
        val offset = editor.caretModel.offset
        val target = getTarget(file, offset) ?: getTarget(file, offset - 1)
        return when (target) {
            null -> Pair.create(mutableListOf(), 0)
            else -> Pair.create(mutableListOf(target), 1)
        }
    }

    private fun getTarget(file: PsiFile, offset: Int)
        : PsiIntroduceTarget<PSExpressionIdentifier>? = file
        .findElementAt(offset)
        ?.parentsOfType<PSExpressionIdentifier>()
        ?.firstOrNull()
        ?.let { PsiIntroduceTarget(it) }

    override fun findSelectionTarget(
        start: Int, end: Int, file: PsiFile, editor: Editor, project: Project
    ) = file.findElementAt(start)
        ?.parentsOfType<PSExpressionIdentifier>()
        ?.firstOrNull()
        ?.let { PsiIntroduceTarget(it) }

    override fun getRefactoringName() = message("extract.method.title")
    override fun getHelpID() = null
    override fun getChooseScopeTitle() = "Choose scope <title>"
    override fun getScopeRenderer() = DefaultPsiElementCellRenderer()
        as PsiElementListCellRenderer<Module.Psi>

    override fun checkSelectedTarget(
        target: PsiIntroduceTarget<PSExpressionIdentifier>,
        file: PsiFile,
        editor: Editor,
        project: Project
    ) = null

    override fun collectTargetScopes(
        target: PsiIntroduceTarget<PSExpressionIdentifier>,
        editor: Editor,
        file: PsiFile,
        project: Project
    ): MutableList<Module.Psi> = target.place
        ?.module
        ?.let { mutableListOf(it) }
        ?: mutableListOf()

    override fun getIntroducer(
        target: PsiIntroduceTarget<PSExpressionIdentifier>,
        scope: Module.Psi,
        usages: MutableList<UsageInfo>,
        replaceChoice: OccurrencesChooser.ReplaceChoice,
        file: PsiFile,
        editor: Editor,
        project: Project
    ): AbstractInplaceIntroducer<ValueDeclarationGroup, PSExpressionIdentifier> {
        val factory = project.service<PSPsiFactory>()
        val occurrences = usages
            .map { it.element as PSExpressionIdentifier }
            .toTypedArray()

        return object :
            AbstractInplaceIntroducer<ValueDeclarationGroup, PSExpressionIdentifier>(
                project,
                editor,
                target.place,
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
            override fun getVariable() =
                factory.createValueDeclarationGroup(
                    "$myExprText'",
                    myExprText
                )!!

            /**
             * This inserts the extracted method into the document
             */
            override fun createFieldToStartTemplateOn(
                replaceAll: Boolean,
                names: Array<out String>
            ) = runWriteAction {
                scope.add(factory.createNewLines(2))
                scope.addTyped(variable)
            }

            override fun suggestNames(
                replaceAll: Boolean,
                variable: ValueDeclarationGroup?
            ): Array<String> = emptyArray()

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
            ) = target.place
        }
    }
}
