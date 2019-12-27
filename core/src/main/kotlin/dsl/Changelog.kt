package dsl

import com.autodsl.annotation.AutoDsl
import persist.DefaultPersistEngine
import persist.PersistEngine

@AutoDsl
data class Changelog(var changesets: List<ChangeSet> = mutableListOf()) {

    fun execute(engine: PersistEngine = DefaultPersistEngine()) {
        engine.checkConnection()
        changesets.forEach {
            it.execute(engine)
        }
    }
}



