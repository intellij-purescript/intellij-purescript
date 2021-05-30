package org.purescript.psi

import com.intellij.psi.tree.IElementType
import org.purescript.PSLanguage
import org.jetbrains.annotations.NonNls

class PSElementType(@NonNls debugName: String) :
    IElementType(debugName, org.purescript.PSLanguage.INSTANCE)