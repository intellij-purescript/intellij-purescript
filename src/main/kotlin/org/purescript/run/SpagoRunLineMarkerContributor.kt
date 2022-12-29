package org.purescript.run

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import org.purescript.psi.declaration.PSValueDeclaration

class SpagoRunLineMarkerContributor: RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        return if ((element as? PSValueDeclaration)?.name == "main") {
            val actions = ExecutorAction.getActions(0)
            Info(
                AllIcons.RunConfigurations.TestState.Run,
                null,
                *actions
            )
        } else {
            null
        }
    }
}