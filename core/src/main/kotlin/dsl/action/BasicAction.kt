package dsl.action

import com.autodsl.annotation.AutoDsl

@AutoDsl
class BasicAction(val f: (basicAction: BasicAction) -> Unit) : Action() {

    override fun execute() {
        f(this)
    }
}