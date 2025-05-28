package org.purescript.highlighting

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.ERROR
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parents
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.purescript.highlighting.PSSyntaxHighlighter.FUNCTION_CALL
import org.purescript.highlighting.PSSyntaxHighlighter.FUNCTION_DECLARATION
import org.purescript.highlighting.PSSyntaxHighlighter.GLOBAL_VARIABLE
import org.purescript.highlighting.PSSyntaxHighlighter.LOCAL_VARIABLE
import org.purescript.highlighting.PSSyntaxHighlighter.PARAMETER
import org.purescript.highlighting.PSSyntaxHighlighter.TYPE_NAME
import org.purescript.highlighting.PSSyntaxHighlighter.TYPE_VARIABLE
import org.purescript.module.declaration.Signature
import org.purescript.module.declaration.value.ValueDecl
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.VarBinder
import org.purescript.module.declaration.value.expression.identifier.Call
import org.purescript.module.declaration.value.expression.identifier.PSExpressionIdentifier
import org.purescript.name.PSIdentifier
import org.purescript.name.PSModuleName
import org.purescript.parser.ClassName
import org.purescript.parser.ExpressionCtor
import org.purescript.parser.TypeCtor

class PSSyntaxHighlightAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is PSIdentifier -> when {
                element.parent is ValueDecl || element.parent is Signature ->
                    holder.newSilentAnnotation(INFORMATION)
                        .textAttributes(FUNCTION_DECLARATION).create()

                element.parent.parent.parent is Call -> {
                    holder.newSilentAnnotation(INFORMATION).textAttributes(FUNCTION_CALL).create()
                }
            }

            is PSExpressionIdentifier -> when (val ref = element.reference.resolve()) {
                is ValueDeclarationGroup -> holder.newSilentAnnotation(INFORMATION).textAttributes(
                    if (ref.isTopLevel) GLOBAL_VARIABLE
                    else LOCAL_VARIABLE
                ).create()

                is VarBinder -> holder.newSilentAnnotation(INFORMATION).textAttributes(PARAMETER).create()
            }

            is VarBinder -> holder.newSilentAnnotation(INFORMATION).textAttributes(PARAMETER).create()
            is PSModuleName -> holder.newSilentAnnotation(INFORMATION)
                .textAttributes(TYPE_NAME).create()

        }
        when (element.node.elementType) {
            TypeCtor, ClassName -> {
                holder.newSilentAnnotation(INFORMATION).textAttributes(TYPE_NAME).create()
            }

            ExpressionCtor -> {
                holder.newSilentAnnotation(INFORMATION).textAttributes(TYPE_VARIABLE).create()
            }
        }
        when (element) {
            is PsiErrorElement -> when (element.errorDescription) {
                "missing lambda arrow" -> {
                    holder.newAnnotation(ERROR, "missing arrow")
                        .withFix(object : IntentionAction {
                            override fun getText(): String = "Add lambda arrow"
                            override fun getFamilyName(): String = "Fix Syntax"
                            override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true
                            override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
                                editor?.document?.replaceString(element.startOffset, element.endOffset, " ->")
                            }

                            override fun startInWriteAction() = true
                        }).range(element.textRange).create()
                }

                "let statement outside of do" -> {
                    holder.newAnnotation(ERROR, "missing do")
                        .withFix(object : IntentionAction {
                            override fun getText(): String = "Add do keyword"
                            override fun getFamilyName(): String = "Fix Syntax"
                            override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true
                            override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
                                val document = editor?.document ?: return
                                val line = document.getLineNumber(element.startOffset)
                                val letIndent = element.startOffset - document.getLineStartOffset(line)
                                val parent = element.parents(false).first {
                                    val parentLine = document.getLineNumber(it.parent.startOffset)
                                    val parentIndent = it.parent.startOffset - document.getLineStartOffset(parentLine)
                                    parentIndent < letIndent
                                }
                                document.insertString(parent.startOffset, "do\n${" ".repeat(letIndent)}")
                            }

                            override fun startInWriteAction() = true
                        })
                        .create()
                }
            }
        }
    }
}
