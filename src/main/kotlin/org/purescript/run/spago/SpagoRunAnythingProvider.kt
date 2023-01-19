package org.purescript.run.spago

import com.intellij.ide.actions.runAnything.RunAnythingAction
import com.intellij.ide.actions.runAnything.RunAnythingUtil
import com.intellij.ide.actions.runAnything.activity.RunAnythingCommandProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import org.purescript.icons.PSIcons

class SpagoRunAnythingProvider : RunAnythingCommandProvider() {
    override fun execute(dataContext: DataContext, value: String) {
        val workDirectory = dataContext.getData(CommonDataKeys.PROJECT)
            ?.guessProjectDir()
        val executor = dataContext.getData(RunAnythingAction.EXECUTOR_KEY)
        RunAnythingUtil.LOG.assertTrue(workDirectory != null)
        RunAnythingUtil.LOG.assertTrue(executor != null)
        runCommand(workDirectory!!, value, executor!!, dataContext)
    }

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