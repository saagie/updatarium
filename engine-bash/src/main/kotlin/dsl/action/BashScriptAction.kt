package dsl.action

import com.autodsl.annotation.AutoDsl
import engine.bash.BashEngine
import java.util.concurrent.TimeUnit

@AutoDsl
data class BashScriptAction(
    val script: String,
    val workingDir: String = ".",
    val timeoutAmount: Long = 60,
    val timeoutUnit: TimeUnit = TimeUnit.SECONDS
) : Action() {
    val bashEngine = BashEngine()

    override fun execute() {
        bashEngine.runCommand(this)
    }
}