package org.purescript.ide.refactoring

import com.intellij.ide.IdeBundle
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.inline.InlineOptionsDialog

class InlineDialog<Element:PsiElement , Location: PsiElement>(
    project: Project,
    val toInline: Element,
    val location: Location?,
    private val action: InlineDialog<Element, Location>.() -> BaseRefactoringProcessor
) :
    
    InlineOptionsDialog(project, true, toInline) {
    init {
        setDoNotAskOption(object : com.intellij.openapi.ui.DoNotAskOption {
            override fun isToBeShown() = EditorSettingsExternalizable.getInstance().isShowInlineLocalDialog
            override fun canBeHidden() = true
            override fun shouldSaveOptionsOnCancel() = false
            override fun getDoNotShowMessage() = IdeBundle.message("label.dont.show")
            override fun setToBeShown(value: Boolean, exitCode: Int) {
                EditorSettingsExternalizable.getInstance().isShowInlineLocalDialog = value
            }
        })
        myInvokedOnReference = location != null
        init()
    }
    public override fun doAction() = invokeRefactoring(action())
    public override fun getProject(): Project = super.getProject()

    override fun getNameLabelText(): String = "Inline value"
    override fun getInlineAllText(): String = RefactoringBundle.message("all.references.and.remove.the.local")
    override fun getKeepTheDeclarationText(): String? = null
    override fun getInlineThisText(): String = RefactoringBundle.message("this.reference.only.and.keep.the.variable")
    
    override fun isInlineThis(): Boolean = true
}