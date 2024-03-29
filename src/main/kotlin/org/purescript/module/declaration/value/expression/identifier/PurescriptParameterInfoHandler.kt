package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.parameterInfo.ParameterInfoHandler
import com.intellij.lang.parameterInfo.ParameterInfoUIContext
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.descendantsOfType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.prevLeaf
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.type.type.PSType

class PurescriptParameterInfoHandler : ParameterInfoHandler<PSExpressionIdentifier, PSType> {
    override fun findElementForParameterInfo(context: CreateParameterInfoContext): PSExpressionIdentifier? {
        val identifier = findParameterOwner(context.file, context.offset)
        val reference = identifier?.reference?.resolve() as? Importable
        reference?.type?.let {
            context.itemsToShow = arrayOf(it)
        }
        return identifier
    }

    private fun findParameterOwner(file: PsiFile, offset: Int): PSExpressionIdentifier? {
        val elem = file.findElementAt(offset) ?: return null
        val element = when {
            elem.parentOfType<Call>() != null -> elem.prevLeaf {
                it.parentOfType<Call>() != null
            }
            else -> elem
        }
        val aCall = element?.parentOfType<Call>(true)
        val call = aCall?.descendantsOfType<Call>()?.lastOrNull() ?: aCall ?: return null
        return call.childrenOfType<PSExpressionIdentifier>().firstOrNull()
    }

    override fun updateParameterInfo(parameterOwner: PSExpressionIdentifier, context: UpdateParameterInfoContext) {
        context.parameterOwner = parameterOwner
    }

    override fun updateUI(p: PSType?, context: ParameterInfoUIContext) {
        p?.let {
            val text = it.text
            context.setupUIComponentPresentation(
                text,
                0,
                text.length,
                false,
                false,
                true,
                context.defaultParameterColor
            )
        }
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): PSExpressionIdentifier? {
        return findParameterOwner(context.file, context.offset)
    }

    override fun showParameterInfo(element: PSExpressionIdentifier, context: CreateParameterInfoContext) {
        context.showHint(element, element.textRange.startOffset + 1, this)
    }
}