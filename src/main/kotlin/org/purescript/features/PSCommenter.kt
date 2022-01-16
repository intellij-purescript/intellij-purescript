package org.purescript.features

import com.intellij.lang.Commenter

class PSCommenter : Commenter {
    override fun getLineCommentPrefix() = "--"
    override fun getBlockCommentPrefix() = "{-"
    override fun getBlockCommentSuffix() = "-}"
    override fun getCommentedBlockCommentPrefix() = "{-"
    override fun getCommentedBlockCommentSuffix() = "-}"
}