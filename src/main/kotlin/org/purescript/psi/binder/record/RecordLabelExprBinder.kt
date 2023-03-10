package org.purescript.psi.binder.record

import com.intellij.lang.ASTNode
import org.purescript.psi.binder.Binder

class RecordLabelExprBinder(node: ASTNode) : Binder(node)