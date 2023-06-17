package org.purescript.run.spago

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PtyCommandLine
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.runAnything.RunAnythingAction
import com.intellij.ide.actions.runAnything.RunAnythingCache
import com.intellij.ide.actions.runAnything.RunAnythingUtil
import com.intellij.ide.actions.runAnything.activity.RunAnythingCommandProvider
import com.intellij.ide.actions.runAnything.commands.RunAnythingCommandCustomizer
import com.intellij.ide.actions.runAnything.execution.RunAnythingRunProfile
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.execution.ParametersListUtil
import org.purescript.icons.PSIcons

class SpagoRunAnythingProvider : RunAnythingCommandProvider() {
    override fun execute(dataContext: DataContext, value: String) {
        val workDirectory = dataContext.getData(CommonDataKeys.PROJECT)
            ?.guessProjectDir()
        val executor = dataContext.getData(RunAnythingAction.EXECUTOR_KEY)
        RunAnythingUtil.LOG.assertTrue(workDirectory != null)
        RunAnythingUtil.LOG.assertTrue(executor != null)
        var dataContext1 = dataContext
        val project = CommonDataKeys.PROJECT.getData(dataContext1)
        RunAnythingUtil.LOG.assertTrue(project != null)
        val commands: MutableCollection<String> = RunAnythingCache.getInstance(project).state.commands
        commands.remove(value)
        commands.add(value)
        dataContext1 = RunAnythingCommandCustomizer.customizeContext(dataContext1)
        val initialCommandLine = project!!.service<Spago>()
            .commandLine
            .withParameters(ParametersListUtil.parse(value, false, true).drop(1))
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkDirectory(workDirectory!!.path)
        val commandLine =
            RunAnythingCommandCustomizer.customizeCommandLine(dataContext1, workDirectory!!, initialCommandLine)
        try {
            val runAnythingRunProfile = RunAnythingRunProfile(
                if (Registry.`is`("run.anything.use.pty", false)) PtyCommandLine(commandLine) else commandLine,
                value
            )
            ExecutionEnvironmentBuilder.create(project!!, executor!!, runAnythingRunProfile)
                .dataContext(dataContext1)
                .buildAndExecute()
        } catch (e: ExecutionException) {
            RunAnythingUtil.LOG.warn(e)
            Messages.showInfoMessage(project, e.message, IdeBundle.message("run.anything.console.error.title"))
        }
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