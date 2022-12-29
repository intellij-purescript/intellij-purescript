package org.purescript.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.ui.dsl.builder.panel
import org.purescript.ide.purs.Npm
import javax.swing.JComponent

class SpagoRunConfiguration(
    private val project: Project,
    factory: SpagoConfigurationFactory
) : LocatableConfigurationBase<SpagoConfigurationType>(project, factory) {
    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ) =
        object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler =
                CapturingProcessHandler(
                    GeneralCommandLine(
                        project
                            .service<Npm>()
                            .pathFor("spago")
                            ?.toString(), "run"
                    )
                        .withWorkDirectory(project.guessProjectDir()?.path.toString())
                )
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return object : SettingsEditor<RunConfiguration>() {
            override fun resetEditorFrom(s: RunConfiguration) = Unit
            override fun applyEditorTo(s: RunConfiguration) = Unit
            override fun createEditor(): JComponent {
                return panel {
                    row {
                        comment("TODO")
                    }
                }
            }
        }
    }
}