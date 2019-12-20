package engine.bash

import dsl.action.BashScriptAction
import java.io.File

class BashEngine {
    fun runCommand(
        bashScriptAction: BashScriptAction
    ) {
        with(bashScriptAction) {
            val proc = ProcessBuilder(listOf("bash","-c",script))
                .directory(File(workingDir))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start().apply { waitFor(timeoutAmount, timeoutUnit) }
            val out = proc.inputStream.bufferedReader().readText()
            when {
                proc.exitValue() == 0 -> logger.info { out.dropLast(1) }
                else -> throw Exception("Command '$script' execution error $out")
            }
        }
    }
}