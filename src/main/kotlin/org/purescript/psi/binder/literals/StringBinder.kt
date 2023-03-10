package org.purescript.psi.binder.literals

import com.intellij.lang.ASTNode
import org.purescript.psi.binder.Binder

class StringBinder(node: ASTNode) : Binder(node)