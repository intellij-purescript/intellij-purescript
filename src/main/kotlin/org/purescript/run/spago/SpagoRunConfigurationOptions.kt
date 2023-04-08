package org.purescript.run.spago

import com.intellij.execution.configurations.LocatableRunConfigurationOptions

class SpagoRunConfigurationOptions: LocatableRunConfigurationOptions() {
    var moduleName by string("Main")
    var command by string("run")
    var config by string("spago.dhall")
    var nodeOptions by string("--enable-source-maps")
//    var environment by map<String,String>()
}