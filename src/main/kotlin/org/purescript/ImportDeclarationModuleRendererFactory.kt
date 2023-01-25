package org.purescript

import com.intellij.ide.util.ModuleRendererFactory
import com.intellij.util.TextWithIcon
import org.purescript.icons.PSIcons
import org.purescript.ide.formatting.ImportDeclaration

class ImportDeclarationModuleRendererFactory : ModuleRendererFactory() {
    override fun handles(element: Any?): Boolean {
        return element is ImportDeclaration
    }

    override fun getModuleTextWithIcon(element: Any?): TextWithIcon? =
        (element as? ImportDeclaration)
            ?.let { TextWithIcon(it.moduleName, PSIcons.FILE)}

    override fun rendersLocationString() = true
}