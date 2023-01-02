package org.purescript.run.spago.test

import com.intellij.execution.configurations.ConfigurationType
import org.purescript.icons.PSIcons
import org.purescript.run.spago.SpagoConfigurationFactory
import javax.swing.Icon

object SpagoTestConfigurationType : ConfigurationType {
    override fun getDisplayName(): String = "Spago Test"
    override fun getConfigurationTypeDescription(): String = "Run spago test target"
    override fun getIcon(): Icon = PSIcons.FILE
    override fun getId(): String = "purescript.spago.test"
    override fun getConfigurationFactories() =
        arrayOf(SpagoTestConfigurationFactory())
}