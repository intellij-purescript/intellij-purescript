package org.purescript.run.spago.test

import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView
import com.intellij.execution.ui.ConsoleView

class SpagoTestState(
    environment: ExecutionEnvironment,
    val spago: String,
    val workDirectory: String,
    val moduleName: String
) : CommandLineState(environment) {
    override fun startProcess(): ProcessHandler {
        val commandLine =
            GeneralCommandLine(spago, "test", "--main", moduleName)
                .withWorkDirectory(workDirectory)
                .withCharset(charset("UTF8"))
        return ColoredProcessHandler(commandLine)
    }
    override fun createConsole(executor: Executor): ConsoleView {
        val runConfiguration = environment.runProfile as RunConfiguration
        val properties =
            SMTRunnerConsoleProperties(runConfiguration, "SpagoTest", executor)
        return SMTRunnerConsoleView(properties).also {
            SMTestRunnerConnectionUtil.initConsoleView(
                it,
                properties.testFrameworkName
            )
        }
    }

}