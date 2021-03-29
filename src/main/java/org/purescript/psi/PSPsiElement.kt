package org.purescript.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import org.purescript.file.PSFile

abstract class PSPsiElement(node: ASTNode) : ASTWrapperPsiElement(node) {

    /**
     * @return the [PSModule] containing this element
     */
    val module: PSModule? get() = (containingFile as? PSFile)?.module
}
