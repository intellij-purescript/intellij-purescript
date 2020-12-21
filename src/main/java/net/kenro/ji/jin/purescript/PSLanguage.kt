package net.kenro.ji.jin.purescript

import com.intellij.lang.Language

class PSLanguage : Language("Purescript", "text/purescript", "text/x-purescript", "application/x-purescript") {
    companion object {
        val INSTANCE = PSLanguage()
    }
}