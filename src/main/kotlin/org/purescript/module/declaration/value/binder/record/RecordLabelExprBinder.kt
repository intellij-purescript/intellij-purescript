package org.purescript.module.declaration.value.binder.record

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.binder.Binder

class RecordLabelExprBinder(node: ASTNode) : Binder(node)