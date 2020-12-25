package net.kenro.ji.jin.purescript.icons

import com.intellij.openapi.util.IconLoader

interface PSIcons {
    companion object {
        val FILE = IconLoader.getIcon("/icons/pure.png", PSIcons::class.java)
    }
}