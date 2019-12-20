package dsl.action

import mu.KLoggable

abstract class Action() : KLoggable {
    override val logger = logger()

    abstract fun execute()
}