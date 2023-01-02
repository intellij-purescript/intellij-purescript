package org.purescript.run.spago.test

import com.intellij.execution.Executor
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.ui.dsl.builder.panel
import org.purescript.run.Npm
import javax.swing.JComponent

class SpagoTestRunConfiguration(
    private val project: Project,
    factory: SpagoTestConfigurationFactory
) : LocatableConfigurationBase<SpagoTestRunConfigurationOptions>(project, factory) {

    public override fun getOptions(): SpagoTestRunConfigurationOptions {
        return super.getOptions() as SpagoTestRunConfigurationOptions
    }


    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ) =
        SpagoTestState(
            environment,
            project.service<Npm>().pathFor("spago").toString(),
            project.guessProjectDir()?.path.toString(),
            options.moduleName ?: "Main"
        )
    

    override fun getConfigurationEditor(): SettingsEditor<SpagoTestRunConfiguration> {
        return object : SettingsEditor<SpagoTestRunConfiguration>() {
            override fun resetEditorFrom(s: SpagoTestRunConfiguration) = Unit
            override fun applyEditorTo(s: SpagoTestRunConfiguration) = Unit
            override fun createEditor(): JComponent {
                return panel {
                    row("Module") {
                        comment(options.moduleName ?: "Main")
                    }
                }
            }
        }
    }
}

