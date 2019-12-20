package dsl.action

import com.autodsl.annotation.AutoDsl
import com.github.kittinunf.fuel.core.FuelManager

@AutoDsl
class HttpScriptAction(val f: (httpScriptAction: HttpScriptAction) -> Unit) : Action() {

    val restClient = FuelManager.instance


    override fun execute() {
        f(this)
    }
}