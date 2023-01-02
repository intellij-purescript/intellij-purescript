package org.purescript.run.spago.test

import com.intellij.execution.configurations.LocatableRunConfigurationOptions

class SpagoTestRunConfigurationOptions: LocatableRunConfigurationOptions() {
    var moduleName by string("Main")
}