package org.purescript.module.declaration.type

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

object LabeledIndex : StringStubIndexExtension<Labeled>() {
    override fun getKey() = KEY

    val KEY = StubIndexKey.createIndexKey<String, Labeled>(
        "purescript.labeled.index"
    )

    fun getLabeled(
        name: String,
        project: Project,
        scope: GlobalSearchScope
    ): MutableCollection<Labeled> = StubIndex.getElements(KEY, name, project, scope, Labeled::class.java)
}