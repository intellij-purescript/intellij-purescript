package org.purescript.psi

import com.intellij.lang.ASTNode

sealed class PSExportedItem(node: ASTNode) : PSPsiElement(node)

class PSExportedClass(node: ASTNode) : PSExportedItem(node)
class PSExportedData(node: ASTNode) : PSExportedItem(node)
class PSExportedKind(node: ASTNode) : PSExportedItem(node)
class PSExportedModule(node: ASTNode) : PSExportedItem(node)
class PSExportedOperator(node: ASTNode) : PSExportedItem(node)
class PSExportedType(node: ASTNode) : PSExportedItem(node)
class PSExportedValue(node: ASTNode) : PSExportedItem(node)
