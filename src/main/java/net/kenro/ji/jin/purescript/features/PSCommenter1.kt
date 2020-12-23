package net.kenro.ji.jin.purescript.features

import com.intellij.lang.Commenter

class PSCommenter : Commenter {
    override fun getLineCommentPrefix(): String {
        return "--"
    }

    override fun getBlockCommentPrefix(): String {
        return "{-"
    }

    override fun getBlockCommentSuffix(): String {
        return "-}"
    }

    override fun getCommentedBlockCommentPrefix(): String {
        return "{-"
    }

    override fun getCommentedBlockCommentSuffix(): String {
        return "-}"
    }
}