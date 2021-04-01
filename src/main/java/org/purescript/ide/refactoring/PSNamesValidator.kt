package org.purescript.ide.refactoring

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project

/*
 * TODO
 *  This validator is used every time we rename something,
 *  and I haven't found an easy way to make it know what type
 *  of element is being renamed. As such, if we want to use this,
 *  it needs to be super permissive, so I made it accept everything
 *  for now. It's not ideal, but it's better than only accepting
 *  Java identifiers.
 */
class PSNamesValidator : NamesValidator {
    override fun isKeyword(name: String, project: Project?): Boolean =
        false

    override fun isIdentifier(name: String, project: Project?): Boolean =
        true
}
