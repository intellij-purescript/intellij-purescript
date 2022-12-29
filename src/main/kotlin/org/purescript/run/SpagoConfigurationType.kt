package org.purescript.run

import com.intellij.execution.configurations.ConfigurationType
import org.purescript.icons.PSIcons
import javax.swing.Icon

object SpagoConfigurationType : ConfigurationType {
    override fun getDisplayName(): String = "Spago"
    override fun getConfigurationTypeDescription(): String = "Run spago target"
    override fun getIcon(): Icon = PSIcons.FILE
    override fun getId(): String = "purescript.spago"
    override fun getConfigurationFactories() =
        arrayOf(SpagoConfigurationFactory())
}