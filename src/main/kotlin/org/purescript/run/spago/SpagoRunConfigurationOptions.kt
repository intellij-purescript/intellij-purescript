package org.purescript.run.spago

import com.intellij.execution.configurations.LocatableRunConfigurationOptions

class SpagoRunConfigurationOptions: LocatableRunConfigurationOptions() {
    var moduleName by string("Main")
}