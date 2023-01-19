package org.purescript.run.spago

import com.intellij.ide.actions.runAnything.activity.RunAnythingCommandProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.util.SystemInfo
import org.purescript.icons.PSIcons
import javax.swing.Icon

class SpagoRunAnythingProvider : RunAnythingCommandProvider() {
    override fun getIcon(value: String) = PSIcons.SPAGO
    override fun findMatchingValue(
        dataContext: DataContext,
        pattern: String
    ): String? {
        if (!pattern.startsWith("spago")) return null
        if (SystemInfo.isWindows && !pattern.startsWith("spago.cmd"))
            return pattern.replaceFirst("spago", "spago.cmd")
        return pattern
    }

    override fun getHelpCommand() = "spago"
    override fun getHelpDescription() = "spago <command>"
    override fun getHelpIcon() = PSIcons.SPAGO
    companion object {
        val INSTANCE = SpagoRunAnythingProvider()
    }
}