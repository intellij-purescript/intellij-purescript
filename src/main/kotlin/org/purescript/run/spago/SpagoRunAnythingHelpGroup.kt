package org.purescript.run.spago

import com.intellij.ide.actions.runAnything.groups.RunAnythingHelpGroup

class SpagoRunAnythingHelpGroup: 
    RunAnythingHelpGroup<SpagoRunAnythingProvider>("Spago", listOf(SpagoRunAnythingProvider.INSTANCE)) 