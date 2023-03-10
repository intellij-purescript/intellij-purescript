package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import org.purescript.psi.binder.Binder

class NullBinder(node: ASTNode) : Binder(node)