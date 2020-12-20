package net.kenro.ji.jin.purescript.psi

import com.intellij.psi.tree.IElementType
import net.kenro.ji.jin.purescript.PSLanguage
import org.jetbrains.annotations.NonNls

class PSElementType(@NonNls debugName: String) :
    IElementType(debugName, PSLanguage.INSTANCE)