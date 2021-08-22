package org.purescript.ide.purs

import com.intellij.openapi.util.SystemInfo
import java.io.File
import java.nio.file.Path

class Purs {
    fun nodeModulesVersion(root: Path?): Path? {
        val sequence = sequence<Path> {
            var tmp = root
            while (tmp != null) {
                yield(tmp)
                tmp = tmp.parent
            }
        }
        val nodeModules = sequence
            .map { it.resolve("node_modules") }
            .firstOrNull { it.toFile().exists() }
            ?: return null
        val binDir = nodeModules.resolve(".bin")
        return when {
            SystemInfo.isWindows -> binDir.resolve("purs.cmd")
            else -> binDir.resolve("purs")
        }

    }
}