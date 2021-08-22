package org.purescript.ide.purs

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.OSProcessUtil
import com.intellij.execution.process.OSProcessUtil.killProcessTree
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.SystemInfo
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class StartupActivity : StartupActivity.Background {
    override fun runActivity(project: Project) {
        val pursBin = getPursPath(project) ?: return
        // node_modules/.bin/purs <- 3 parents
        val rootDir = pursBin.parent.parent.parent.toFile()
        val command = GeneralCommandLine(pursBin.toString(), "ide", "server")
            .withWorkDirectory(rootDir)
        val task = object : Task.Backgroundable(
            project,
            "purs ide server for " + project.name,
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                CapturingProcessHandler(command)
                    .runProcessWithProgressIndicator(indicator)
            }

        }
        task.queue()
    }

    private fun getPursPath(project: Project): Path? {
        val sequence = sequence<Path> {
            var tmp = project.guessProjectDir()?.toNioPath()
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