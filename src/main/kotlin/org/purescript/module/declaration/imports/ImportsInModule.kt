package org.purescript.module.declaration.imports

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey.createIndexKey

/**
 * Maps Module names to Import Statements that imports that module and also are exported by their own module
 */
object ImportsInModule : StringStubIndexExtension<Import>() {
    val KEY = createIndexKey<String, Import>("purescript.importsInModule")
    override fun getKey() = KEY
}