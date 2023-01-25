package org.purescript

import com.intellij.ide.util.ModuleRendererFactory
import com.intellij.util.TextWithIcon
import org.purescript.icons.PSIcons
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.base.PSStubbedElement

class PsiElementModuleRendererFactory : ModuleRendererFactory() {
    override fun handles(element: Any?) =
        element is PSPsiElement || element is PSStubbedElement<*>

    override fun getModuleTextWithIcon(element: Any?): TextWithIcon? = when (element) {
        is PSPsiElement -> element.module?.name
        is PSStubbedElement<*> -> element.module?.name
        else -> null
    }?.let { TextWithIcon(it, PSIcons.FILE)}

    override fun rendersLocationString() = true
}