import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import dsl.Changelog
import persist.engine.DefaultEngine
import persist.engine.Engine
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path

class MagicalUpdater(val engine: Engine = DefaultEngine()) {
    val ktsLoader = KtsObjectLoader()

    fun executeChangelog(path: Path) {
        executeChangelog(Files.newBufferedReader(path))
    }

    fun executeChangelog(reader: Reader) {
        with(ktsLoader.load<Changelog>(reader)) {
            this.execute(engine)
        }
    }

    fun executeChangelog(script: String) {
        with(ktsLoader.load<Changelog>(script)) {
            this.execute(engine)
        }
    }

}