package org.purescript.run.spago

import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogPanel
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import org.purescript.psi.expression.PSArrayLiteral
import org.purescript.psi.expression.PSExpressionIdentifier
import org.purescript.psi.module.ModuleNameIndex
import org.purescript.run.Npm
import javax.swing.JComponent

class SpagoRunConfiguration(
    private val project: Project,
    factory: SpagoConfigurationFactory
) : LocatableConfigurationBase<SpagoRunConfigurationOptions>(project, factory) {

    public override fun getOptions() =
        super.getOptions() as SpagoRunConfigurationOptions


    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ) = object : CommandLineState(environment) {
        override fun startProcess(): ProcessHandler {
            val spago =
                project.service<Npm>().pathFor("spago")?.toString()

            val commandLine =
                GeneralCommandLine(
                    spago,
                    "-x",
                    options.config,
                    options.command,
                    "--main",
                    options.moduleName ?: "Main",
                    "--source-maps",
                    "--purs-args",
                    "-g sourcemaps",
                    "--node-args",
                    "--enable-source-maps"
                )
                    .withEnvironment("NODE_OPTIONS", "--enable-source-maps")
                    .withWorkDirectory(project.guessProjectDir()?.path.toString())
                    .withCharset(charset("UTF8"))
            return ColoredProcessHandler(commandLine)
        }

        override fun createConsole(executor: Executor): ConsoleView? {
            return if (usesTeamcityReporter()) {
                val runConfiguration =
                    environment.runProfile as RunConfiguration
                val properties =
                    SMTRunnerConsoleProperties(
                        project,
                        runConfiguration,
                        "SpagoTest",
                        executor
                    )
                properties.isIdBasedTestTree = true
                SMTRunnerConsoleView(properties).also {
                    SMTestRunnerConnectionUtil.initConsoleView(
                        it,
                        properties.testFrameworkName
                    )
                }
            } else {
                super.createConsole(executor)
            }
        }
    }

    private fun usesTeamcityReporter(): Boolean {
        val scope = GlobalSearchScope.allScope(project)
        val module = options.moduleName?.let {
            ModuleNameIndex().get(it, project, scope)
        }?.firstOrNull() ?: return false
        val main = module.exportedValueDeclarationGroups
            .firstOrNull { it.name == "main" } ?: return false
        val reporters = main.valueDeclarations.single().expressionAtoms
            .filterIsInstance<PSArrayLiteral>()
            .flatMap { it.values.mapNotNull { it.expressionAtoms.singleOrNull() } }
            .filterIsInstance<PSExpressionIdentifier>()
        return reporters.any { it.name == "teamcityReporter" }
    }

    override fun getConfigurationEditor(): SettingsEditor<SpagoRunConfiguration> {
        return object : SettingsEditor<SpagoRunConfiguration>() {
            private lateinit var panel: DialogPanel
            override fun resetEditorFrom(s: SpagoRunConfiguration) =
                panel.reset()

            override fun applyEditorTo(s: SpagoRunConfiguration) = panel.apply()
            override fun createEditor(): JComponent {
                panel = panel {
                    row("Module") {
                        comment(options.moduleName ?: "Main")
                    }
                    row("Command") {
                        textField().bindText(
                            { options.command ?: "run" },
                            { options.command = it }
                        )
                    }
                    row("Config") {
                        textField().bindText(
                            { options.config ?: "spago.dhall" },
                            { options.config = it }
                        )
                    }
                }
                return panel
            }
        }
    }
}