package org.purescript.run.spago

import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
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
    ) =
        object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler {
                val spago =
                    project.service<Npm>().pathFor("spago")?.toString()
                val commandLine =
                    GeneralCommandLine(spago, "-x", options.config, options.command, "--main", options.moduleName ?: "Main")
                        .withWorkDirectory(project.guessProjectDir()?.path.toString())
                        .withCharset(charset("UTF8"))
                return ColoredProcessHandler(commandLine)
            }
        }

    override fun getConfigurationEditor(): SettingsEditor<SpagoRunConfiguration> {
        return object : SettingsEditor<SpagoRunConfiguration>() {
            private lateinit var panel: DialogPanel
            override fun resetEditorFrom(s: SpagoRunConfiguration) = panel.reset()
            override fun applyEditorTo(s: SpagoRunConfiguration) = panel.apply()
            override fun createEditor(): JComponent {
                panel = panel {
                    row("Module") {
                        comment(options.moduleName ?: "Main")
                    }
                    row("Command") {
                        textField().bindText(
                            {options.command ?: "run"},
                            {options.command = it}
                        )
                    }
                    row("Config") {
                        textField().bindText(
                            {options.config ?: "spago.dhall"},
                            {options.config = it}
                        )
                    }
                }
                return panel
            }
        }
    }
}